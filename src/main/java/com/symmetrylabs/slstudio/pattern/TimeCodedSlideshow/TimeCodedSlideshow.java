package com.symmetrylabs.slstudio.pattern.TimeCodedSlideshow;

import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
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
import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    final StringParameter directory = new StringParameter("dir", null);
    private final BooleanParameter chooseDir = new BooleanParameter("pick", false).setMode(BooleanParameter.Mode.MOMENTARY);
    private final BooleanParameter bake = new BooleanParameter("bake", false).setMode(BooleanParameter.Mode.MOMENTARY);
    private final CompoundParameter shrinkParam = new CompoundParameter("shrink", 1, 1.4, 3);
    private final CompoundParameter yOffsetParam = new CompoundParameter("yoff", 0, 0, 1);
    private final CompoundParameter cropLeftParam = new CompoundParameter("cropL", 0, 0, 1);
    private final CompoundParameter cropRightParam = new CompoundParameter("cropR", 0, 0, 1);
    private final CompoundParameter cropTopParam = new CompoundParameter("cropT", 0, 0, 1);
    private final CompoundParameter cropBottomParam = new CompoundParameter("cropB", 0, 0, 1);
    private final MutableParameter tcStartFrame = new MutableParameter("tcStart", 0);
    private final BooleanParameter freewheel = new BooleanParameter("run", false);
    private final BooleanParameter freewheelReset = new BooleanParameter("runReset", false).setMode(BooleanParameter.Mode.MOMENTARY);

    private MidiTime mt;
    int currentFrame;
    private int lastFrameReceived;
    protected boolean stopping = false;
    long lastLoadLoop = 0;
    private boolean currentFrameLoaded;

    protected boolean baked = false;
    BufferedImage bakedImage = null;

    int nFrames = 0;
    private double freewheelTime = 0;

    static class Slide {
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

    Slide[] allFrames;
    Thread loaderThread;
    Semaphore loaderSemaphore;

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
        addParameter(bake);
        addParameter(freewheel);
        addParameter(freewheelReset);

        for (LXMidiInput input : lx.engine.midi.inputs) {
            input.addTimeListener(new LXMidiInput.MidiTimeListener() {
                @Override
                public void onBeatClockUpdate(int i, double v) {
                }

                @Override
                public void onMTCUpdate(MidiTime midiTime) {
                    mt = midiTime.clone();
                    if (freewheel.getValueb()) {
                        return;
                    }
                    int frame = mt.getHour();
                    frame = 60 * frame + mt.getMinute();
                    frame = 60 * frame + mt.getSecond();
                    frame = mt.getRate().fps() * frame + mt.getFrame();
                    lastFrameReceived = frame;
                    goToFrame(frame);
                }
            });
            System.out.println("attached time code listener to " + input.getName());
        }

        /* start the semaphore with no permits; we fill it up once we've loaded
         * the directory into the allFrames array. */
        loaderSemaphore = new Semaphore(0, false);
        lastLoadLoop = System.nanoTime();
    }

    @Override
    public void onActive() {
        super.onActive();
        stopping = false;
        loadDirectory();

        if (!baked) {
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
                        for (int i = currentFrame < 0 ? 0 : currentFrame; i < nFrames; i++) {
                            if (!allFrames[i].loaded()) {
                                allFrames[i].load();
                                break;
                            }
                        }
                    }
            });
            try {
                loaderThread.start();
            } catch (IllegalThreadStateException e) {
                e.printStackTrace();
            }
        } else {
            loaderThread = null;
        }
    }

    @Override
    public void onInactive() {
        super.onInactive();
        SLStudio.setWarning(TAG, null);
        stopping = true;
        if (loaderThread != null) {
            loaderThread.interrupt();
            try {
                loaderThread.join();
            } catch (InterruptedException e) {
            }
            loaderThread = null;
        }
    }

    @Override
    public String getCaption() {
        int offset = lastFrameReceived - tcStartFrame.getValuei();
        if (baked) {
            return String.format(
                "time %s / %d frames / frame %d / offset %d / baked image %s",
                mt == null ? "unknown" : mt.toString(),
                nFrames,
                currentFrame,
                offset,
                directory.getString());
        }
        return String.format(
            "time %s / %d frames / frame %d %s / offset %d / waiting %d / since last load %ds / dir %s",
            mt == null ? "unknown" : mt.toString(),
            nFrames,
            currentFrame,
            currentFrameLoaded ? "ok" : "skip",
            offset,
            loaderSemaphore.availablePermits(),
            (int) ((System.nanoTime() - lastLoadLoop) / 1e9),
            directory.getString());
    }

    @Override
    public void onParameterChanged(LXParameter p) {
        if (p == chooseDir && chooseDir.getValueb()) {
            FileDialog dialog = new FileDialog(
                (Frame) null, "Choose frame directory or baked image:", FileDialog.LOAD);
            dialog.setVisible(true);
            String fname = dialog.getFile();
            if (fname == null) {
                return;
            }

            File load = new File(dialog.getDirectory(), fname);
            Path loadPath = load.toPath().toAbsolutePath();
            Path repoRoot = Paths.get("").toAbsolutePath();
            Path rel = repoRoot.relativize(loadPath);
            directory.setValue(rel.toString());
            loadDirectory();

        } else if (p == bake && bake.getValueb()) {
            FileDialog dialog = new FileDialog(
                (Frame) null, "Save baked video as:", FileDialog.SAVE);
            dialog.setVisible(true);
            String fname = dialog.getFile();
            if (fname == null) {
                return;
            }
            bake(new File(dialog.getDirectory(), fname));

        } else if (p == freewheelReset && freewheelReset.getValueb()) {
            currentFrame = -1;
            freewheelTime = 0;
        }
    }

    void loadDirectory() {
        SLStudio.setWarning(TAG, null);
        String path = directory.getString();
        if (path == null) {
            return;
        }
        File dir = new File(path);

        if (dir.isFile() && dir.getName().endsWith(".png")) {
            baked = true;
            try {
                bakedImage = ImageIO.read(dir);
                nFrames = bakedImage.getHeight();
            } catch (IOException e) {
                SLStudio.setWarning(TAG, "couldn't load baked slideshow");
                System.out.println("could not load baked slideshow:");
                e.printStackTrace();
                bakedImage = null;
            }
            return;
        }

        if (!dir.isDirectory()) {
            SLStudio.setWarning(TAG, "slideshow directory does not exist or no *.png hunk");
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
        nFrames = newAllFrames.length;
        /* make this an atomic mutation, so that goToFrame's view of the frame
         * list is always coherent. */
        allFrames = newAllFrames;
        loaderSemaphore.release(BUFFER_COUNT);
    }

    private void goToFrame(int frame) {
        frame -= tcStartFrame.getValuei();

        /* if we're out of range, set frame to -1. If we were out of range
         * before, we don't have to do anything. */
        if (frame < 0 || frame >= nFrames) {
            if (currentFrame == -1) {
                return;
            }
            frame = -1;
        }

        if (baked) {
            currentFrame = frame;
        }
        if (allFrames == null) {
            return;
        }

        /* if we're going backward in time out of our buffer, we clear the
         * buffer and start again. */
        if (frame == -1 || frame < currentFrame - KEEP_TRAILING_FRAMES) {
            System.out.println(String.format("backwards: %d to %d", currentFrame, frame));
            loaderSemaphore.drainPermits();
            currentFrame = frame;
            for (int i = 0; i < nFrames; i++) {
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

    private void copyFrameToColors(BufferedImage img, int[] ccs) {
        int cropLeft = Math.round(cropLeftParam.getValuef() * img.getWidth());
        int cropRight = Math.round(cropRightParam.getValuef() * img.getWidth());
        int cropTop = Math.round(cropTopParam.getValuef() * img.getHeight());
        int cropBottom = Math.round(cropBottomParam.getValuef() * img.getHeight());

        float shrink = shrinkParam.getValuef();
        int croppedWidth = img.getWidth() - cropLeft - cropRight;
        int croppedHeight = img.getHeight() - cropTop - cropBottom;

        Arrays.fill(ccs, 0);

        for (LXVector v : getVectors()) {
            int i = (int) ((shrink * (model.yMax - v.y)) + yOffsetParam.getValue() * img.getHeight());
            int j = (int) (shrink * (v.x - model.xMin));
            int color;
            if (i >= croppedHeight || j >= croppedWidth || i < 0 || j < 0) {
                color = 0;
            } else {
                int vcolor = img.getRGB(j + cropLeft, i + cropTop);
                color = LXColor.rgb(
                    (vcolor >> 16) & 0xFF,
                    (vcolor >> 8) & 0xFF,
                    vcolor & 0xFF);
            }
            ccs[v.index] = color;
        }
    }

    @Override
    public void run(double elapsedMs, PolyBuffer.Space preferredSpace) {
        if (freewheel.getValueb()) {
            freewheelTime += elapsedMs;
            if (freewheelTime > 1000 / 30) {
                freewheelTime = 0;
                currentFrame++;
            }
        }

        int[] ccs = (int[]) getArray(PolyBuffer.Space.SRGB8);
        if (currentFrame >= nFrames || currentFrame < 0) {
            Arrays.fill(ccs, 0);
        } else if (baked) {
            if (bakedImage != null) {
                /* we can't just pull the colors straight out of the image as
                 * a single array copy because we want to honor warps that turn
                 * off pixels. */
                Arrays.fill(ccs, 0);

                try {
                    for (LXVector v : getVectors()) {
                        ccs[v.index] = bakedImage.getRGB(v.index, currentFrame);
                    }
                }
                catch (Exception e) {
                    System.out.println("bad index");
                }
            } else {
                Arrays.fill(ccs, 0);
            }
        } else {
            Slide slide = allFrames[currentFrame];
            currentFrameLoaded = slide.loaded();
            if (!currentFrameLoaded) {
                return;
            }
            copyFrameToColors(slide.img, ccs);
        }
        markModified(PolyBuffer.Space.SRGB8);
    }

    private void bake(File output) {
        if (baked) {
            System.err.println("slideshow is already baked");
            return;
        }
        final int PN = model.points.length;
        final int FN = nFrames;
        BufferedImage res = new BufferedImage(PN, FN, BufferedImage.TYPE_INT_ARGB);
        LXVector[] vectors = getVectorArray();
        for (int frame = 0; frame < FN; frame++) {
            Slide s = allFrames[frame];
            int[] frameColors = new int[PN];
            s.load();
            copyFrameToColors(s.img, frameColors);
            s.unload();
            res.setRGB(0, frame, PN, 1, frameColors, 0, PN);
            if (frame % 100 == 0) {
                System.out.println(
                    String.format("baked frame %d for %s", frame, directory.getString()));
            }
        }
        try {
            ImageIO.write(res, "png", output);
            System.out.println(
                String.format("baked %d frames for %s", FN, directory.getString()));
        } catch (IOException e) {
            System.err.println("couldn't write baked output:");
            e.printStackTrace();
        }
    }
}
