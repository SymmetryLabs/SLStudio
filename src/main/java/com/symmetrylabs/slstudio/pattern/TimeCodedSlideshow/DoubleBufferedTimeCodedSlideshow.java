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
import heronarts.lx.parameter.*;
import heronarts.lx.transform.LXVector;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Semaphore;


// Modification of TCSS to host continuous loading...
public class DoubleBufferedTimeCodedSlideshow extends SLPattern<SLModel> {
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
    private final BooleanParameter sweep = new BooleanParameter("sweepFrame", false);
    private final DiscreteParameter sweepSelectFrame = new DiscreteParameter("sweepSelectFrame", 0, 0, 120);

    private MidiTime mt;
    int currentFrame;
    private int lastFrameReceived;
    protected boolean stopping = false;
    long lastLoadLoop = 0;
    private boolean currentFrameLoaded;

    protected boolean baked = false;


//    int nFrames = 0;
    int nFrames = Integer.MAX_VALUE;
    private double freewheelTime = 0;
    private int hunkLengthInFrames = -1; // we don't know untill we read
    private int masterHunkIndex = 0;

    /*
    * DoubleBuffer
    * A consumer will read sequentially through it's index [0, length)
    * When one half of the buffer is depleted a worker will fill the buffer as needed
     */
    class DoubleBuffer {

        public boolean stopping;
        Thread loaderThread;
        Semaphore loaderSemaphore;

        private static final int NUM_BUFFER = 2;
        public boolean initialized = false;

        BufferedImage twoBuffers[] = new BufferedImage[2];

        private int prev_buffer_index = 0; // start using buffer 0

        public DoubleBuffer(){
            // no permits initially.. wait untill we have a directory
            loaderSemaphore = new Semaphore(0, false);

            // initialize the loader thread
            loaderThread = new Thread(() -> {
                while (!stopping) {
                    try {
                        SLStudio.setWarning("TCSS loaderThread - ", "waiting...");
                        loaderSemaphore.acquire();
                    } catch (InterruptedException e) {
                        return;
                    }
                    lastLoadLoop = System.nanoTime();

                    SLStudio.setWarning("TCSS loaderThread - ", "loading " + masterHunkIndex + ".png");
                    loadNextHunk(); // load next hunk returns true on success.  Do not stop if success.
                }
            });
            try {
                loaderThread.start();
            } catch (IllegalThreadStateException e) {
                e.printStackTrace();
            }
        }

        boolean loadNextHunk() {
            String directoryPath = directory.getString();
            String hunkPath = directoryPath + "/" + get_current_hunk_index() + ".png";

            File hunkFile = new File(hunkPath);
            if (hunkFile.isFile() && hunkFile.getName().endsWith(".png")) {
                try{
                    BufferedImage curBuffer = ImageIO.read(hunkFile);
                    // if there's nothing in there initialize it
                    if (!this.initialized) {
                        this.initializeBuffers(curBuffer);
                        hunkLengthInFrames = curBuffer.getHeight();
                        masterHunkIndex++;
                    }
                    else{
                        this.load(curBuffer);
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

        public void initializeBuffers(BufferedImage initialBuffer){
            load(initialBuffer);
            initialized = true;
            // load next one by letting the load function work as normal.
            loaderSemaphore.release();
        }

        public void load(BufferedImage bufIn) {
            if (twoBuffers[0] == null){
                twoBuffers[0] = bufIn;
                System.out.println("Loaded 0");
                return;
            }
            else if (twoBuffers[1] == null){
                twoBuffers[1] = bufIn;
                System.out.println("Loaded 1");
                return;
            }
            System.err.println("Neither buffer free on call to DoubleBuffer.load()");
            assert(false);//we should never get here
        }

        public int getRGB(int vec_i, int frame_i) {
            frame_i %= hunkLengthInFrames*2; // modulo 2*hunkLength;
//            int buffer_index = (frame_i)/hunkLengthInFrames;
            int buffer_index = frame_i/hunkLengthInFrames;
            assert(buffer_index == 1 || buffer_index == 0): "Bad buffer index: " + buffer_index;
            if (buffer_index != prev_buffer_index){ // buffer switched.. load a new hunk
                markOpen(prev_buffer_index);
                loaderSemaphore.release();
            }
            prev_buffer_index = buffer_index;
            return twoBuffers[buffer_index].getRGB(vec_i, frame_i%hunkLengthInFrames);
        }

        private void markOpen(int prev_buffer_index) {
            twoBuffers[prev_buffer_index] = null;
        }
    }

    private DoubleBuffer doubleBuffer = new DoubleBuffer();

    public DoubleBufferedTimeCodedSlideshow(LX lx) {
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
        addParameter(sweep);
        addParameter(sweepSelectFrame);

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

        lastLoadLoop = System.nanoTime();
    }

    @Override
    public void onActive() {
        super.onActive();

        doubleBuffer.stopping = stopping;
//        if(directory != null){
//            loaderSemaphore.release(DoubleBuffer.NUM_BUFFER);
//        }

    }

    @Override
    public void onInactive() {
        super.onInactive();
        SLStudio.setWarning(TAG, null);
        stopping = true;
        if (doubleBuffer.loaderThread != null) {
            doubleBuffer.loaderThread.interrupt();
            try {
                doubleBuffer.loaderThread.join();
            } catch (InterruptedException e) {
            }
            doubleBuffer.loaderThread = null;
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

            doubleBuffer.loaderSemaphore.release(1);

        } else if (p == freewheelReset && freewheelReset.getValueb()) {
            currentFrame = -1;
            freewheelTime = 0;
            masterHunkIndex = 0;
        }
    }

    int tc_modulo(){
        // STUB:  ultimately return the offset into the buffer ring
        return 0;
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

        if (sweep.getValueb()){
            currentFrame = (int)sweepSelectFrame.getValuef();
        }

        int[] ccs = (int[]) getArray(PolyBuffer.Space.SRGB8);
        if (currentFrame < 0) {
            Arrays.fill(ccs, LXColor.RED);
        }
        if (currentFrame >= nFrames) {
            Arrays.fill(ccs, LXColor.RED);
            // ok we've depleted the current buffer in double buffer.
            // switch to the other buffer
            // current buffer is now available to load next needed.
            // always baked.
        }
        if (false){

        } else {
            if (doubleBuffer.initialized) {
                /* we can't just pull the colors straight out of the image as
                 * a single array copy because we want to honor warps that turn
                 * off pixels. */
                Arrays.fill(ccs, 0);

                try {
                    for (LXVector v : getVectors()) {
//                        ccs[v.index] = doubleBuffer.get(get_current_hunk_buffer()).getRGB(v.index, currentFrame % hunkLengthInFrames);
                        ccs[v.index] = doubleBuffer.getRGB(v.index, currentFrame);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
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
            doubleBuffer.loaderSemaphore.availablePermits(),
            (int) ((System.nanoTime() - lastLoadLoop) / 1e9),
            directory.getString());
    }
}
