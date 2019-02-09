package com.symmetrylabs.slstudio.pattern.TimeCodedSlideshow;

import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.util.Hunk;
import com.symmetrylabs.util.DoubleBuffer;
import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.PolyBuffer;
import heronarts.lx.color.LXColor;
import heronarts.lx.midi.LXMidiInput;
import heronarts.lx.midi.MidiTime;
import heronarts.lx.parameter.*;
import heronarts.lx.transform.LXVector;

import javax.imageio.ImageIO;
import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.function.Supplier;


// Modification of TCSS to host continuous loading...
public class OfflinePlayback extends LXPattern {

    private static final String TAG = "TimeCodedSlideshow";

    protected final StringParameter directory = new StringParameter("dir", null);
    private final BooleanParameter chooseDir = new BooleanParameter("pick", false).setMode(BooleanParameter.Mode.MOMENTARY);
    private final MutableParameter tcStartFrame = new MutableParameter("tcStart", 0);
    private final BooleanParameter freewheel = new BooleanParameter("run", false);
    private final BooleanParameter freewheelReset = new BooleanParameter("runReset", false).setMode(BooleanParameter.Mode.MOMENTARY);
    private final BooleanParameter sweep = new BooleanParameter("sweepFrame", false);
    private final DiscreteParameter sweepSelectFrame = new DiscreteParameter("sweepSelectFrame", 0, 0, 120);

    protected final BooleanParameter concurrent = new BooleanParameter("UseConcurrent", false);

    private MidiTime mt;
    protected int currentFrame;
    private int lastFrameReceived;
    protected boolean stopping = false;
    private long lastLoadLoop = 0;
    private boolean currentFrameLoaded;


//    int nFrames = 0;
    int nFrames = Integer.MAX_VALUE;
    private double freewheelTime = 0;
    private int hunkLengthInFrames = 30; // we don't know untill we read

    DoubleBuffer<Hunk> doubleBuffer;
    protected Thread loaderThread = new Thread();
    private Semaphore loaderSemaphore;
    private int currentHunk;

    private int currentHunk(){
        return currentFrame/hunkLengthInFrames;
    }

    // catchall update the contents of our buffers based on the current frame, and the current contents.
    public void updateBuffers() {
        currentHunk = currentFrame/hunkLengthInFrames;
        if (currentHunk == doubleBuffer.getFront().hunkIndex){
           // we're already where we need to be.
            return;
        }
        // standard transition forward (this should be called once per hunkcycle (i.e. 1Hz if we have 30 frames)
        if (currentHunk() == doubleBuffer.getFront().hunkIndex + 1){
            // ok we need to cycle the double buffer.
            // we're done using the current front buffer.
            // "flip the page"; make current (now exhausted) front buffer, new back buffer,
            // and flip (now clean) back buffer to be new front buffer
            doubleBuffer.flip();
            // reload the expired back buffer

            if (concurrent.getValueb()){
                loaderSemaphore.release();
            }
            else{
                doubleBuffer.supplyBack();
            }
        }
        // jump forward
        else if (currentHunk() > doubleBuffer.getFront().hunkIndex + 1){
            SLStudio.setWarning("PlayerInfo: ", "Jump forward.");
            // reset
            doubleBuffer.dispose();
            doubleBuffer.initialize();
            return;
        }
        // "jump backwards"
        else if(currentHunk() < doubleBuffer.getFront().hunkIndex){
            // need to reset
            doubleBuffer.dispose();
            doubleBuffer.initialize();
            return;
        }
    }

    // this supplier always loads in the next buffer needed
    // how to test this?
    private class MTCBufferFetcher implements Supplier<Hunk>{
        int most_recent_hunk_in_memory = -1;
        private boolean strict = true;

        private void reset() {
            most_recent_hunk_in_memory = -1;
        }

        @Override
        public Hunk get(){

            int get_this_index = -1;

            int current_hunk_index_from_MTC = currentFrame/hunkLengthInFrames;


            if (most_recent_hunk_in_memory == -1){
                get_this_index = current_hunk_index_from_MTC;
            }

            // ok we already have the current one in memory. Good we're currently writing to that buffer.
            // get the next one loaded
            else if (current_hunk_index_from_MTC == most_recent_hunk_in_memory){
                SLStudio.setWarning("loader", "look ahead");
                get_this_index = current_hunk_index_from_MTC +1;
            }

            else if (current_hunk_index_from_MTC < most_recent_hunk_in_memory){
                SLStudio.setWarning("loader", "reinitialize load");
                get_this_index = current_hunk_index_from_MTC;
            }

            else if (current_hunk_index_from_MTC > most_recent_hunk_in_memory) {
                SLStudio.setWarning("loader", "snapping forward");
                get_this_index = current_hunk_index_from_MTC;
//                if (strict){
//                    throw new IllegalStateException("If playing linearly the current hunk index bin should never get ahead of what's already in memory.");
//                }
            }

            String directoryPath = directory.getString();
            String hunkPath = directoryPath + "/" + get_this_index + ".png";

            hunkPath = hunkPath.replaceFirst("^~", System.getProperty("user.home"));

            File hunkFile = new File(hunkPath);

            SLStudio.setWarning(TAG, "loading: " + get_this_index + ".png" + " --- already have: " + most_recent_hunk_in_memory + ".png");

            Hunk curBuffer = null;
            if (hunkFile.isFile() && hunkFile.getName().endsWith(".png")) {
                try {
                    curBuffer = new Hunk(ImageIO.read(hunkFile), get_this_index);
                } catch (IOException e) {
                    String error_msg = "couldn't load next cache in sequence";
                    SLStudio.setWarning(TAG, error_msg);
                    System.out.println(error_msg);
                    e.printStackTrace();
                }
                // successful read.
                most_recent_hunk_in_memory = get_this_index;
            }
            return curBuffer;
        }

    }

    private void arm_MTC_listeners(){
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
    }

    // every time we update we should be loading some data to the output.
    private void goToFrame(int frame) {
        currentFrame = frame;
        currentHunk = currentFrame/hunkLengthInFrames;
    }

    public MTCBufferFetcher buffSupply;
    public OfflinePlayback(LX lx) {
        super(lx);
        addParameter(concurrent);
        addParameter(directory);
        addParameter(chooseDir);
        addParameter(tcStartFrame);
        addParameter(freewheel);
        addParameter(freewheelReset);
        addParameter(sweep);
        addParameter(sweepSelectFrame);

        arm_MTC_listeners();

        buffSupply = new MTCBufferFetcher();
        doubleBuffer = new DoubleBuffer<>(buffSupply);

        loaderSemaphore = new Semaphore(0);

        lastLoadLoop = System.nanoTime();

        loaderThread = new Thread( () -> {
            synchronized (this.doubleBuffer){
                while(!stopping){
                    try {
                        loaderSemaphore.acquire();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (!doubleBuffer.initialized){
                        doubleBuffer.initialize();
                    }
                    else {
                        doubleBuffer.supplyBack();
                    }
                    notify();
                }
            }
        });
        loaderThread.start();
    }

    @Override
    public void onActive() {
        super.onActive();
        stopping = false;
    }

    @Override
    public void onInactive() {
        super.onInactive();
        SLStudio.setWarning(TAG, null);
        stopping = true;
        // gracefully destroy loader thread
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

            doubleBuffer.initialize();
//            loaderSemaphore.release(1);

        } else if (p == freewheelReset && freewheelReset.getValueb()) {
            currentFrame = -1;
            freewheelTime = 0;
        }
    }


    @Override
    public void run(double elapsedMs, PolyBuffer.Space preferredSpace) {
        //update frame
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

        // update hunk based on frame
        currentHunk = currentFrame/hunkLengthInFrames;

        if (doubleBuffer.initialized){
            // "standard" transition
            updateBuffers();
            /* we can't just pull the colors straight out of the image as
             * a single array copy because we want to honor warps that turn
             * off pixels. */
            Arrays.fill(ccs, 0);

            try {
                for (LXVector v : getVectors()) {
                    ccs[v.index] = doubleBuffer.getFront().img.getRGB(v.index, currentFrame%hunkLengthInFrames);
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("bad index");
            }
        }
        markModified(PolyBuffer.Space.SRGB8);
    }

//    @Override
//    public String getCaption() {
//        int offset = lastFrameReceived - tcStartFrame.getValuei();
//            return String.format(
//                "time %s / hunksize %d / frame %d / directory %s / current %d",
//                mt == null ? "unknown" : mt.toString(),
//                hunkLengthInFrames,
//                currentFrame,
//                directory.getString(),
//                currentHunk);
//    }
}
