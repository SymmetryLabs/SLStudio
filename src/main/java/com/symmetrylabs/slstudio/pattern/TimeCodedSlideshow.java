package com.symmetrylabs.slstudio.pattern;

import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.midi.LXMidiInput;
import heronarts.lx.midi.MidiTime;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.StringParameter;

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

    private static final int PREFETCH_COUNT = 60; // 2 sec at 30FPS

    private final StringParameter directory = new StringParameter("dir", null);
    private final BooleanParameter chooseDir = new BooleanParameter("pick", false).setMode(BooleanParameter.Mode.MOMENTARY);

    private MidiTime mt;
    private int currentFrame;

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

        BufferedImage get() {
            load();
            return img;
        }
    }

    private Slide[] allFrames;
    private Thread loaderThread;
    private Semaphore loaderSemaphore;

    public TimeCodedSlideshow(LX lx) {
        super(lx);
        addParameter(directory);
        addParameter(chooseDir);

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
        }

        /* start the semaphore with no permits; we fill it up once we've loaded
         * the directory into the allFrames array. */
        loaderSemaphore = new Semaphore(0, false);
        loaderThread = new Thread(() -> {
            while (true) {
                try {
                    loaderSemaphore.acquire();
                } catch (InterruptedException e) {
                    return;
                }
                /* Find the first frame after/including the current frame that
                 * hasn't been loaded, and load it. */
                int i = currentFrame;
                do {
                    if (!allFrames[i].loaded()) {
                        break;
                    }
                    i = (i + 1) % allFrames.length;
                } while (i != currentFrame);
                /* This is a no-op for frames that are already loaded, so it
                 * won't do anything in the case where all frames are already
                 * loaded. */
                allFrames[i].load();
            }
        });
    }

    @Override
    public void onActive() {
        super.onActive();
        loadDirectory();
        loaderThread.start();
    }

    @Override
    public void onInactive() {
        loaderThread.stop();
    }

    @Override
    public String getCaption() {
        return String.format(
            "time %s / %d frames / cur frame %d / loader queue size %d / dir %s",
            mt == null ? "unknown" : mt.toString(),
            allFrames == null ? 0 : allFrames.length,
            currentFrame,
            loaderSemaphore.availablePermits(),
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

        allFrames = new Slide[files.length];
        for (int i = 0; i < files.length; i++) {
            allFrames[i] = new Slide(files[i]);
        }
        loaderSemaphore.release(PREFETCH_COUNT);
    }

    private void goToFrame(int frame) {
        int i = currentFrame;
        int unloaded = 0;
        currentFrame = frame % allFrames.length;
        do {
            if (allFrames[i].loaded()) {
                allFrames[i].unload();
                unloaded++;
            }
            i = (i + 1) % allFrames.length;
        } while (i != currentFrame);
        loaderSemaphore.release(unloaded);
    }
}
