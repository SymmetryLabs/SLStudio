package com.symmetrylabs.slstudio.pattern;

import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.midi.LXMidiInput;
import heronarts.lx.midi.MidiTime;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.MutableParameter;
import heronarts.lx.parameter.StringParameter;
import heronarts.lx.transform.LXVector;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.Semaphore;

public class TimeCodedSlideshow extends SLPattern<SLModel> {
    private static final String TAG = "TimeCodedSlideshow";

    private static final int BUFFER_COUNT = 150; // 5 sec at 30FPS
    /* Sometimes timecode can jump backwards a little, so we keep a couple
     * frames before the current frame in the buffer just in case. Note
     * that these frames count towards the BUFFER_COUNT limit. */
    private static final int KEEP_TRAILING_FRAMES = 15; // 500ms at 30FPS

    private final StringParameter directory = new StringParameter("dir", null);
    private final BooleanParameter chooseDir = new BooleanParameter("pick", false).setMode(BooleanParameter.Mode.MOMENTARY);
    private final CompoundParameter shrinkParam = new CompoundParameter("shrink", 1, 1.4, 3);
    private final CompoundParameter yOffsetParam = new CompoundParameter("yoff", 0, 0, 1);
    private final CompoundParameter cropLeftParam = new CompoundParameter("cropL", 0, 0, 1);
    private final CompoundParameter cropRightParam = new CompoundParameter("cropR", 0, 0, 1);
    private final CompoundParameter cropTopParam = new CompoundParameter("cropT", 0, 0, 1);
    private final CompoundParameter cropBottomParam = new CompoundParameter("cropB", 0, 0, 1);
    private final MutableParameter tcStartFrame = new MutableParameter("tcStart", 0);

    private MidiTime mt;
    private int currentFrame;
    private boolean stopping = false;
    private long lastLoadLoop = 0;

    private static class Slide {
        File path;
        BufferedImage img;

        Slide(File p) {
            path = p;
            img = null;
        }

        boolean loaded() {
            return img != null;
        }

        void load() {
            if (img == null) {
                try {
                    img = ImageIO.read(path);
                } catch (IOException e) {
                    e.printStackTrace();
                    SLStudio.setWarning(TAG, String.format("failed to load %s", path.toString()));
                }
            }
        }

        void unload() {
            img = null;
        }
    }

    private Slide[] allFrames;
    private Thread loaderThread;
    private Semaphore loaderSemaphore;

    public TimeCodedSlideshow(LX lx) {
        super(lx);
        addParameter(directory);
        addParameter(chooseDir);
        addParameter(shrinkParam);
        addParameter(yOffsetParam);
        addParameter(cropLeftParam);
        addParameter(cropRightParam);
        addParameter(cropTopParam);
        addParameter(cropBottomParam);
        addParameter(tcStartFrame);

        for (LXMidiInput input : lx.engine.midi.inputs) {
            input.addTimeListener(new LXMidiInput.MidiTimeListener() {
                @Override
                public void onBeatClockUpdate(int i, double v) {
                }

                @Override
                public void onMTCUpdate(MidiTime midiTime) {
                    mt = midiTime.clone();
                    int frame = mt.getHour();
                    frame = 60 * frame + mt.getMinute();
                    frame = 60 * frame + mt.getSecond();
                    frame = mt.getRate().fps() * frame + mt.getFrame();
                    goToFrame(frame);
                }
            });
            System.out.println("attached time code listener to " + input.getName());
        }

        /* start the semaphore with no permits; we fill it up once we've loaded
         * the directory into the allFrames array. */
        loaderSemaphore = new Semaphore(0, false);
        lastLoadLoop = System.nanoTime();
        loaderThread = new Thread(() -> {
            while (!stopping) {
                try {
                    loaderSemaphore.acquire();
                } catch (InterruptedException e) {
                    return;
                }
                lastLoadLoop = System.nanoTime();
                /* Find the first frame after/including the current frame that
                 * hasn't been loaded, and load it. */
                for (int i = currentFrame < 0 ? 0 : currentFrame; i < allFrames.length; i++) {
                    if (!allFrames[i].loaded()) {
                        allFrames[i].load();
                        break;
                    }
                }
            }
        });
    }

    @Override
    public void onActive() {
        super.onActive();
        stopping = false;
        loadDirectory();
        try {
            loaderThread.start();
        } catch (IllegalThreadStateException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onInactive() {
        stopping = true;
        loaderThread.interrupt();
        try {
            loaderThread.join();
        } catch (InterruptedException e) {
        }
    }

    @Override
    public String getCaption() {
        return String.format(
            "time %s / %d frames / frame %d / waiting %d / since last load %ds / dir %s",
            mt == null ? "unknown" : mt.toString(),
            allFrames == null ? 0 : allFrames.length,
            currentFrame,
            loaderSemaphore.availablePermits(),
            (int) ((System.nanoTime() - lastLoadLoop) / 1e9),
            directory.getString());
    }

    @Override
    public void onParameterChanged(LXParameter p) {
        if (p == chooseDir && chooseDir.getValueb()) {
            JFileChooser jfc = new JFileChooser();
            jfc.setDialogType(JFileChooser.OPEN_DIALOG);
            jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int res = jfc.showOpenDialog(null);
            if (res == JFileChooser.APPROVE_OPTION) {
                directory.setValue(jfc.getSelectedFile().getAbsolutePath());
                loadDirectory();
            }
        }
    }

    private void loadDirectory() {
        SLStudio.setWarning(TAG, null);
        String path = directory.getString();
        if (path == null) {
            return;
        }
        File dir = new File(path);
        if (!dir.isDirectory()) {
            SLStudio.setWarning(TAG, "slideshow directory does not exist");
            return;
        }
        File[] files = dir.listFiles(fn -> fn.getName().endsWith(".bmp"));
        if (files == null) {
            SLStudio.setWarning(TAG, "no files in directory");
            return;
        }

        Arrays.sort(files, Comparator.comparing(File::getName));

        Slide[] newAllFrames = new Slide[files.length];
        for (int i = 0; i < files.length; i++) {
            newAllFrames[i] = new Slide(files[i]);
        }
        /* make this an atomic mutation, so that goToFrame's view of the frame
         * list is always coherent. */
        allFrames = newAllFrames;
        loaderSemaphore.release(BUFFER_COUNT);
    }

    private void goToFrame(int frame) {
        if (allFrames == null) {
            return;
        }
        frame -= tcStartFrame.getValuei();
        if (frame < 0 || frame >= allFrames.length) {
            currentFrame = -1;
            return;
        }
        /* if we're going backward in time out of our buffer, we clear the
         * buffer and start again. */
        if (frame < currentFrame - KEEP_TRAILING_FRAMES) {
            System.out.println(String.format("backwards: %d to %d", currentFrame, frame));
            loaderSemaphore.drainPermits();
            currentFrame = frame;
            for (int i = 0; i < allFrames.length; i++) {
                allFrames[i].unload();
            }
            loaderSemaphore.release(BUFFER_COUNT);
        } else {
            /* otherwise, we release the frames that we've buffered before our
             * new current (minus our padding) and then queue up that many
             * frames to be loaded. */
            currentFrame = frame;
            for (int i = 0; i < currentFrame - KEEP_TRAILING_FRAMES; i++) {
                if (allFrames[i].loaded()) {
                    allFrames[i].unload();
                    loaderSemaphore.release();
                }
            }
        }
    }

    @Override
    public void run(double elapsedMs) {
        int black = 0;
        for (int i = 0; i < colors.length; i++) {
            colors[i] = black;
        }

        if (allFrames == null || currentFrame >= allFrames.length || currentFrame < 0) {
            return;
        }
        Slide slide = allFrames[currentFrame];
        if (!slide.loaded()) {
            return;
        }
        BufferedImage img = slide.img;

        int cropLeft = Math.round(cropLeftParam.getValuef() * img.getWidth());
        int cropRight = Math.round(cropRightParam.getValuef() * img.getWidth());
        int cropTop = Math.round(cropTopParam.getValuef() * img.getHeight());
        int cropBottom = Math.round(cropBottomParam.getValuef() * img.getHeight());

        float shrink = shrinkParam.getValuef();
        int croppedWidth = img.getWidth() - cropLeft - cropRight;
        int croppedHeight = img.getHeight() - cropTop - cropBottom;

        for (LXVector v : getVectors()) {
            int i = (int) ((shrink * (model.yMax - v.y)) + yOffsetParam.getValue() * img.getHeight());
            int j = (int) (shrink * (v.x - model.xMin));
            int color;
            if (i >= croppedHeight || j >= croppedWidth || i < 0 || j < 0) {
                color = black;
            } else {
                int vcolor = img.getRGB(j + cropLeft, i + cropTop);
                color = LXColor.rgb(
                    (vcolor >> 16) & 0xFF,
                    (vcolor >> 8) & 0xFF,
                    vcolor & 0xFF);
            }
            colors[v.index] = color;
        }
    }
}
