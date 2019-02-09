package com.symmetrylabs.slstudio.output;

import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.util.Hunk;
import com.symmetrylabs.util.DoubleBuffer;
import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import heronarts.lx.midi.LXMidiInput;
import heronarts.lx.midi.MidiTime;
import heronarts.lx.model.LXModel;
import heronarts.lx.parameter.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.function.Supplier;

public class DoubleBufferedOfflineRenderOutput extends OfflineRenderOutput {
    public static final String HEADER = "SLOutput";
    public static final int VERSION = 3;
    private static final int HUNK_SIZE = 30;

    public final BooleanParameter pStart = new BooleanParameter("pStart", false).setMode(BooleanParameter.Mode.MOMENTARY);
    public final DiscreteParameter pFramesToCapture = new DiscreteParameter("frames", 30, 0, 30000);
    public final CompoundParameter pFrameRate = new CompoundParameter("rate", 30, 1, 60);
    public final StringParameter pOutputFile = new StringParameter("output", "");
    public final StringParameter pStatus = new StringParameter("status", "IDLE");
    public final MutableParameter hunkSize = new MutableParameter("hunkSizeFrames", 30);
    public final BooleanParameter externalSync = new BooleanParameter("ext", false);

    public final BooleanParameter concurrent = new BooleanParameter("concurrent", false);

    private LXModel model;
    private long startFrameNanos;
    private int lastFrameWritten;
    private int framesToCapture;
    private double frameRate;

    protected DoubleBuffer<Hunk> doubleBuffer;

    WriterThread writerThread;
    Semaphore writerSemaphore;

    // the currently writing hunk index
    private int currentHunk = 0;
    private MidiTime mt;
    int currentFrame = -1;
    private int nextHunkLimit = -1;
    private boolean running = false;


    // catchall update the contents of our buffers based on the current frame, and the current contents.
    public void updateBuffers() {
        currentHunk = currentFrame/hunkSize.getValuei();
        if (currentHunk == doubleBuffer.getFront().hunkIndex){
            // we're already where we need to be.
            return;
        }
        // standard transition forward (this should be called once per hunkcycle (i.e. 1Hz if we have 30 frames)
        if (currentHunk == doubleBuffer.getFront().hunkIndex + 1){
            // ok we need to cycle the double buffer.
            // we're done using the current front buffer.
            // "flip the page"; make current (now exhausted) front buffer, new back buffer,
            // and flip (now clean) back buffer to be new front buffer
            doubleBuffer.flip();
            // reload the expired back buffer

            if (concurrent.getValueb()){
                writerSemaphore.release();
            }
            else{
//                doubleBuffer.supplyBack();
                writeHunk(doubleBuffer.getFront());
            }
        }
        // jump forward
        else if (currentHunk > doubleBuffer.getFront().hunkIndex + 1){
            SLStudio.setWarning("PlayerInfo: ", "Jump forward.");
            // reset
            doubleBuffer.dispose();
            doubleBuffer.initialize();
            return;
        }
        // "jump backwards"
        else if(currentHunk < doubleBuffer.getFront().hunkIndex){
            // need to reset
            doubleBuffer.dispose();
            doubleBuffer.initialize();
            return;
        }
    }

    public void writeHunk(Hunk data) {
        final File outputToWrite = new File(outputPathFromHunk(data.hunkIndex));
        EventQueue.invokeLater(() -> {
            try {
                ImageIO.write(data.img, "png", outputToWrite);
            } catch (IOException e) {
                System.err.println("couldn't save output image:");
                e.printStackTrace();
            }
        });
    }

    // this supplier always loads in the next buffer needed
    // how to test this?
//    private class MTCBufferFetcher implements Supplier<Hunk>{
//        int most_recent_hunk_in_memory = -1;
//        private boolean strict = true;
//
//        private void reset() {
//            most_recent_hunk_in_memory = -1;
//        }
//
//        @Override
//        public Hunk get(){
//
//            int get_this_index = -1;
//
//            int current_hunk_index_from_MTC = currentFrame/hunkSize.getValuei();
//
//            if (most_recent_hunk_in_memory == -1){
//                get_this_index = current_hunk_index_from_MTC;
//            }
//
//            // ok we already have the current one in memory. Good we're currently writing to that buffer.
//            // get the next one loaded
//            else if (current_hunk_index_from_MTC == most_recent_hunk_in_memory){
//                SLStudio.setWarning("loader", "look ahead");
//                get_this_index = current_hunk_index_from_MTC +1;
//            }
//
//            else if (current_hunk_index_from_MTC < most_recent_hunk_in_memory){
//                SLStudio.setWarning("loader", "reinitialize load");
//                get_this_index = current_hunk_index_from_MTC;
//            }
//
//            else if (current_hunk_index_from_MTC > most_recent_hunk_in_memory) {
//                SLStudio.setWarning("loader", "snapping forward");
//                get_this_index = current_hunk_index_from_MTC;
////                if (strict){
////                    throw new IllegalStateException("If playing linearly the current hunk index bin should never get ahead of what's already in memory.");
////                }
//            }
//
//            String directoryPath = pOutputDir.getString();
//            String hunkPath = directoryPath + "/" + get_this_index + ".png";
//
//            hunkPath = hunkPath.replaceFirst("^~", System.getProperty("user.home"));
//
//            File hunkFile = new File(hunkPath);
//
//            Hunk curBuffer = null;
//            if (hunkFile.isFile() && hunkFile.getName().endsWith(".png")) {
//                try {
//                    curBuffer = new Hunk(ImageIO.read(hunkFile), get_this_index);
//                } catch (IOException e) {
//                    String error_msg = "couldn't load next cache in sequence";
//                    SLStudio.setWarning("ERROR WRITIN", error_msg);
//                    System.out.println(error_msg);
//                    e.printStackTrace();
//                }
//                // successful read.
//                most_recent_hunk_in_memory = get_this_index;
//            }
//            return curBuffer;
//        }
//
//    }
    // this supplier allocates buffers with the appropriate MTC-correlated index
    // we want a buffer flagged with the currently pending index but also want
    // the next one needed so our writer never blocks
    class IndexedBufferSupplier implements Supplier<Hunk> {
        private int last_hunk_index_leased = -1;

        public void reset(){
            last_hunk_index_leased = -1;
        }

        @Override
        public Hunk get() {
            int most_recent_hunk_in_memory = last_hunk_index_leased;

            int get_this_index = -1;
            int current_hunk_index_from_MTC = currentFrame/hunkSize.getValuei();

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

            if (get_this_index == -1){
                throw new IllegalStateException("'get_this_index' should have triggered");
            }
            last_hunk_index_leased = get_this_index;
            SLStudio.setWarning("TCSS bufferSupply - ", "supplied " + get_this_index);
            return new Hunk(createImage(), get_this_index);


//            if (pOutputDir.getString() == ""){
//                SLStudio.setWarning("TCSS bufferSupply - ", "select output dir to start");
//                return null;
//            }
//            if ((last_hunk_index_leased == -1) || (last_hunk_index_leased < currentHunk)){
//                last_hunk_index_leased = currentHunk;
//                SLStudio.setWarning("TCSS bufferSupply - ", "supplied " + last_hunk_index_leased);
//                return new Hunk(createImage(currentFrame), currentHunk);
//            }
//            if (last_hunk_index_leased == currentHunk){
//                last_hunk_index_leased = currentHunk + 1;
//                SLStudio.setWarning("TCSS bufferSupply - ", "supplied " + last_hunk_index_leased);
//                return new Hunk(createImage(currentFrame), currentHunk + 1);
//            }
//            if (last_hunk_index_leased >= currentHunk + 1){
//                // client should never ask for a new buffer this soon
//                reset();
//                return get();
//            }
//            return null;
        }
    }

    IndexedBufferSupplier bufferSupply = new IndexedBufferSupplier();

    public DoubleBufferedOfflineRenderOutput(LX lx) {
        super(lx);
        this.externalSync.setDescription("Sync output to an external MTC source");
        this.model = lx.model;
        pOutputDir.addListener(p -> {
            dispose();
            if (!pOutputDir.getString().equals("")) {
                // reset
                doubleBuffer.dispose();
                doubleBuffer = new DoubleBuffer<>(new IndexedBufferSupplier());
                doubleBuffer.initialize();
                currentHunk = 0;
            }
        });
        pStart.addListener(p -> {
            if (pStart.getValueb()) {
                createImage();
                if (externalSync.getValueb()){
                    pStatus.setValue("WAIT");
                }
            }
        });

        doubleBuffer = new DoubleBuffer<>(bufferSupply);

//        currentFrameLimit = hunkSize.getValuei(); // initialize our first write trigger
        currentHunk = 0;

        nextHunkLimit = (currentHunk + 1)*hunkSize.getValuei();

        writerSemaphore = new Semaphore(0);
        writerThread = new WriterThread("writer");
        writerThread.start();

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
                    updateFrame(frame);
                    if (externalSync.getValueb()){
                        pStatus.setValue("" + currentHunk);
                    }
                }
            });
            System.out.println("attached time code listener to " + input.getName());
        }
    }

    protected void updateFrame(int frame) {
        if (frame < currentFrame){
            currentHunk = frame/hunkSize.getValuei();
        }
        currentFrame = frame;
        // update hunk index as needed.
        running = true;
    }

    class WriterThread extends Thread {
        WriterThread(String name) {
            super(name);
        }

        public void run() {
            while (!isInterrupted()) {
                try {
                    writerSemaphore.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                writeHunk(doubleBuffer.getFront());
            }
        }

        public void writeHunk(Hunk data) {
            final File outputToWrite = new File(outputPathFromHunk(data.hunkIndex));
            EventQueue.invokeLater(() -> {
                try {
                    ImageIO.write(data.img, "png", outputToWrite);
                } catch (IOException e) {
                    System.err.println("couldn't save output image:");
                    e.printStackTrace();
                }
            });
        }
    }

    private String outputPathFromHunk(int hunk) {
        if (currentFrame == -1){
            currentFrame = 0;
        }
        return pOutputDir.getString() + "/" + hunk + ".png";
    }

    private String outputPathFromFrame() {
        if (currentFrame == -1){
            currentFrame = 0;
        }
        return pOutputDir.getString() + "/" + currentHunk + ".png";
    }
//    private String outputPathFromMTC(MidiTime mt) {
//        return pOutputDir.getString() + "/" + mt.getFrame()/HUNK_SIZE + ".png";
//    }

    public void dispose() {
        doubleBuffer.dispose();
        pStatus.setValue("IDLE");
    }

    private BufferedImage createImage() {
//        return createImage(-1);
        BufferedImage create = new BufferedImage(model.points.length, hunkSize.getValuei(), BufferedImage.TYPE_INT_ARGB);
//        int w = create.getWidth(null);
//        int h = create.getHeight(null);
//        int[] rgbs = new int[w];
////        int[] rgbs = new int[w];
//        int rgb = 0xFF00FF00; // green
//        for (int val : rgbs){
//            val = rgb;
//        }
//        for (int i = 0; i < h; i++){
//            create.setRGB(0, i, w, 1, rgbs, 0, w);
//        }
        return create;
    }

    private BufferedImage createImage(int lastFrame) {
        startFrameNanos = System.nanoTime();
        lastFrameWritten = lastFrame;
        framesToCapture = hunkSize.getValuei();
        frameRate = pFrameRate.getValue();
        return new BufferedImage(model.points.length, hunkSize.getValuei(), BufferedImage.TYPE_INT_ARGB);
    }

    int last_hunk_frame = 0;

    @Override
    protected void onSend(PolyBuffer colors) {
        if (doubleBuffer.getFront() == null) {
            // we're not locked and loaded yet..
            return;
        }

        int inFrame;
        if (externalSync.getValueb()){
            if (!running){
                pStatus.setValue("WAIT_SYNC");
                return;
            }
            inFrame = currentFrame;
        }
        else{
            pStatus.setValue("REC");
            double elapsedSec = 1e-9 * (double) (System.nanoTime() - startFrameNanos);
            inFrame = (int) Math.floor(frameRate * elapsedSec);
        }
        if (inFrame <= lastFrameWritten) {
            return;
        }
        lastFrameWritten = inFrame;
        // if our current frame is part of the next hunk we need to write..
        if (inFrame >= nextHunkLimit) {
//        if (lastFrameWritten >= currentFrameLimit) {

            // pass the back buffer forward to be written out by the writerThread
            doubleBuffer.flip();
            if (concurrent.getValueb()){
                writerSemaphore.release();
            }
            else{
                writeHunk(doubleBuffer.getFront());
            }

            //start  work on the next hunk
            currentHunk++;
            doubleBuffer.setBack(bufferSupply.get());

            nextHunkLimit = currentHunk *hunkSize.getValuei();
            last_hunk_frame = inFrame;
            // write our next value into the image buffer
        }
        int[] carr = (int[]) colors.getArray(PolyBuffer.Space.RGB8);

        if (inFrame - lastFrameWritten < 15){
            return;
        }
        // write to the back buffer
        doubleBuffer.getBack().img.setRGB(0, inFrame - last_hunk_frame, model.points.length, 1, carr, 0, model.points.length);
    }
}
