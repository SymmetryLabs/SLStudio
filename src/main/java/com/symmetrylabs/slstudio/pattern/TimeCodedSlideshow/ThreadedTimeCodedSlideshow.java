package com.symmetrylabs.slstudio.pattern.TimeCodedSlideshow;

import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.Buffer;
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
import java.awt.image.BufferedImage;
import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;


// Modification of TCSS to host continuous loading...
public class ThreadedTimeCodedSlideshow extends SLPattern<SLModel> {
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


    int nFrames = 0;
    private double freewheelTime = 0;
    private int hunkLengthInFrames = -1; // we don't know untill we read
    private int masterHunkIndex = 0;

    /*
    * DoubleBuffer
    * If DoubleBuffer is valid then it..
    * Always has one buffer loaded which the main thread can use.
    * Always has one buffer ready to be loaded by it's own thread so that the main thread is not interrupted.
     */
    class DoubleBuffer {
        public boolean valid = false;
        private static final int NUM_BUFFER = 2;
        public boolean initialized = false;
        List<BufferedImage> twoBuffers = new ArrayList<>();
        private int pending_circular_buffer_index = 0;

        private int nextToLoad = 0;

        public BufferedImage activeBuffer;
        public BufferedImage inactiveBuffer;

        public DoubleBuffer(){}

        public void initializeBuffers(BufferedImage initialBuffer){
            for (int i = 0; i < NUM_BUFFER; i++){
                twoBuffers.add(initialBuffer);
            }
            activeBuffer = twoBuffers.get(0);
            inactiveBuffer = twoBuffers.get(1);
            nextToLoad = 1;
            valid = true;
            initialized = true;
        }

        public void load(BufferedImage bufIn) {
            twoBuffers.set(nextToLoad, bufIn);
            inactiveBuffer = bufIn;
            nextToLoad = (nextToLoad==1?0:1); // toggle nextToLoad
            System.out.println("Next IDX: " + nextToLoad);
        }

        public void swapActive() {
            activeBuffer = inactiveBuffer;
        }
    }


    Thread loaderThread;
    Semaphore loaderSemaphore;

    DoubleBuffer doubleBuffer = new DoubleBuffer();

    public ThreadedTimeCodedSlideshow(LX lx) {
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

//        if(directory != null){
//            loaderSemaphore.release(DoubleBuffer.NUM_BUFFER);
//        }

        loaderThread = new Thread(() -> {
            while (!stopping) {
                try {
                    SLStudio.setWarning("TCSS loaderThread - ", "waiting... next to load: " + masterHunkIndex + ".png");
                    loaderSemaphore.acquire();
                    doubleBuffer.swapActive();
                } catch (InterruptedException e) {
                    return;
                }
                lastLoadLoop = System.nanoTime();

                loadNextHunk(); // load next hunk returns true on success.  Do not stop if success.
                nFrames += doubleBuffer.activeBuffer.getHeight();
            }
        });
        try {
            loaderThread.start();
        } catch (IllegalThreadStateException e) {
            e.printStackTrace();
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
    public void onParameterChanged(LXParameter p) {
        if (p == chooseDir && chooseDir.getValueb()) {
            FileDialog dialog = new FileDialog(
                (Frame) null, "Choose frame directory or baked image:", FileDialog.LOAD);
            dialog.setVisible(true);
            String fname = dialog.getFile();
            if (fname == null) {
                return;
            }

//            File load = new File(dialog.getDirectory(), fname);
            File load = new File(dialog.getDirectory(), "");
            Path loadPath = load.toPath().toAbsolutePath();
            Path repoRoot = Paths.get("").toAbsolutePath();
            Path rel = repoRoot.relativize(loadPath);
            directory.setValue(rel.toString());
//            directory.setValue(dialog.getDirectory());

            System.out.println("Directory is: " + directory.getString());
//            Tell the loaderThread load the first buffers
            loaderSemaphore.release(DoubleBuffer.NUM_BUFFER);

        } else if (p == freewheelReset && freewheelReset.getValueb()) {
            currentFrame = -1;
            freewheelTime = 0;
        }
    }

    int tc_modulo(){
        // STUB:  ultimately return the offset into the buffer ring
        return 0;
    }

    boolean loadNextHunk() {
        String directoryPath = directory.getString();
        String hunkPath = directoryPath + "/" + masterHunkIndex + ".png";

        File hunkFile = new File(hunkPath);
        if (hunkFile.isFile() && hunkFile.getName().endsWith(".png")) {
            try{
                BufferedImage curBuffer = ImageIO.read(hunkFile);
                // if there's nothing in there initialize it
                if (!doubleBuffer.initialized) {
                    doubleBuffer.initializeBuffers(curBuffer);
                    hunkLengthInFrames = doubleBuffer.activeBuffer.getHeight();
                    masterHunkIndex++;
                }
                else{
                    doubleBuffer.load(curBuffer);
                    masterHunkIndex++;
                }
            } catch (IOException e) {
                String error_msg = "couldn't load next cache in sequence";
                SLStudio.setWarning(TAG, error_msg);
                System.out.println(error_msg);
                e.printStackTrace();
                return false;
            }
            return true;
        }
        else {
            String error_msg = "bad path: " + hunkPath;
            SLStudio.setWarning(TAG, error_msg);
            System.out.println(error_msg);
        }
        return false;
    }

//    void loadDirectory() {
//        SLStudio.setWarning(TAG, null);
//        String path = directory.getString();
//        if (path == null) {
//            return;
//        }
//        File dir = new File(path);
//
//        if (dir.isFile() && dir.getName().endsWith(".png")) {
//            baked = true;
//            try {
//                doubleBuffer.set(pending_circular_buffer_index, ImageIO.read(dir));
//                nFrames = doubleBuffer.get(pending_circular_buffer_index).getHeight();
//            } catch (IOException e) {
//                SLStudio.setWarning(TAG, "couldn't load baked slideshow");
//                System.out.println("could not load baked slideshow:");
//                e.printStackTrace();
//                doubleBuffer = null;
//            }
//            return;
//        }
//
//        if (!dir.isDirectory()) {
//            SLStudio.setWarning(TAG, "slideshow directory does not exist or no *.png hunk");
//            return;
//        }
//        File[] files = dir.listFiles(fn -> fn.getName().endsWith(".png"));
//        if (files == null) {
//            SLStudio.setWarning(TAG, "no files in directory");
//            return;
//        }
//
//
//        loaderSemaphore.release(NUM_BUFFER);
//    }

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

        currentFrame = frame;

//        /* if we're going backward in time out of our buffer, we clear the
//         * buffer and start again. */
//        if (frame == -1 || frame < currentFrame - KEEP_TRAILING_FRAMES) {
//            System.out.println(String.format("backwards: %d to %d", currentFrame, frame));
//            loaderSemaphore.drainPermits();
//            currentFrame = frame;
//            for (int i = 0; i < nFrames; i++) {
//                allFrames[i].unload();
//            }
//            loaderSemaphore.release(BUFFER_COUNT);
//        } else {
//            /* otherwise, we release the frames that we've buffered before our
//             * new current (minus our padding) and then queue up that many
//             * frames to be loaded. */
//            currentFrame = frame;
//            for (int i = 0; i < currentFrame - KEEP_TRAILING_FRAMES; i++) {
//                if (allFrames[i].loaded()) {
//                    allFrames[i].unload();
//                    loaderSemaphore.release();
//                }
//            }
//        }
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
        if (currentFrame < 0) {
            Arrays.fill(ccs, LXColor.RED);
        }
        if (currentFrame >= nFrames) {
            // ok we've depleted the current buffer in double buffer.
            // switch to the other buffer
            // current buffer is now available to load next needed.
            loaderSemaphore.release();
            // always baked.
        } else {
            if (doubleBuffer != null) {
                /* we can't just pull the colors straight out of the image as
                 * a single array copy because we want to honor warps that turn
                 * off pixels. */
                Arrays.fill(ccs, 0);

                try {
                    for (LXVector v : getVectors()) {
//                        ccs[v.index] = doubleBuffer.get(get_current_hunk_buffer()).getRGB(v.index, currentFrame % hunkLengthInFrames);
                        ccs[v.index] = doubleBuffer.activeBuffer.getRGB(v.index, currentFrame % hunkLengthInFrames);
                    }
                } catch (Exception e) {
                    System.out.println("bad index");
                }
            } else {
                Arrays.fill(ccs, 0);
            }
        }
        markModified(PolyBuffer.Space.SRGB8);
    }

    // the index of the file we want.
    private int get_current_hunk_index() {
//        return get_current_batch_index()*NUM_BUFFER + pending_circular_buffer_index;
        return masterHunkIndex;
    }
    private int get_current_hunk_offset() {
        return currentFrame%hunkLengthInFrames;
    }
    private int get_current_batch_index() {
        if (hunkLengthInFrames == -1){
            return 0;
        }
        int batch_index = currentFrame/(hunkLengthInFrames*DoubleBuffer.NUM_BUFFER);
        System.out.println(batch_index);
        return batch_index;
    }

    int last_circular_buffer_index = 0;
    private int get_current_hunk_buffer() {
        int hunk_buffer_index = (currentFrame/hunkLengthInFrames)%2;
        if (hunk_buffer_index != last_circular_buffer_index){
            loaderSemaphore.release(); // we've depleted this buffer, tell the loader to get the next one.
        }
        last_circular_buffer_index = hunk_buffer_index;
        return hunk_buffer_index;
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
}
