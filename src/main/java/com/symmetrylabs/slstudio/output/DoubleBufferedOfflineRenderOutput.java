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

    private LXModel model;
    private long startFrameNanos;
    private int lastFrameWritten;
    private int framesToCapture;
    private double frameRate;

    private DoubleBuffer<Hunk> doubleBuffer;

    WriterThread writerThread;
    Semaphore writerSemaphore;

    // the currently writing hunk index
    private int hunkIndex = 0;
    private MidiTime mt;
    private int lastFrameMTC = -1;
    private int nextHunkLimit = -1;
    private boolean running = false;


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
            last_hunk_index_leased = hunkIndex;
            SLStudio.setWarning("TCSS bufferSupply - ", "supplied " + last_hunk_index_leased);
            return new Hunk(createImage(), hunkIndex);
//            if (pOutputDir.getString() == ""){
//                SLStudio.setWarning("TCSS bufferSupply - ", "select output dir to start");
//                return null;
//            }
//            if ((last_hunk_index_leased == -1) || (last_hunk_index_leased < hunkIndex)){
//                last_hunk_index_leased = hunkIndex;
//                SLStudio.setWarning("TCSS bufferSupply - ", "supplied " + last_hunk_index_leased);
//                return new Hunk(createImage(lastFrameMTC), hunkIndex);
//            }
//            if (last_hunk_index_leased == hunkIndex){
//                last_hunk_index_leased = hunkIndex + 1;
//                SLStudio.setWarning("TCSS bufferSupply - ", "supplied " + last_hunk_index_leased);
//                return new Hunk(createImage(lastFrameMTC), hunkIndex + 1);
//            }
//            if (last_hunk_index_leased >= hunkIndex + 1){
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
                hunkIndex = 0;
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
        hunkIndex = 0;

        nextHunkLimit = (hunkIndex + 1)*hunkSize.getValuei();

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
                        pStatus.setValue("" + hunkIndex);
                    }
                }
            });
            System.out.println("attached time code listener to " + input.getName());
        }
    }

    private void updateFrame(int frame) {
        if (frame < lastFrameMTC){
            hunkIndex = frame/hunkSize.getValuei();
        }
        lastFrameMTC = frame;
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
        if (lastFrameMTC == -1){
            lastFrameMTC = 0;
        }
        return pOutputDir.getString() + "/" + hunk + ".png";
    }

    private String outputPathFromFrame() {
        if (lastFrameMTC == -1){
            lastFrameMTC = 0;
        }
        return pOutputDir.getString() + "/" + hunkIndex + ".png";
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
        return new BufferedImage(model.points.length, hunkSize.getValuei(), BufferedImage.TYPE_INT_ARGB);
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
            inFrame = lastFrameMTC;
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
            writerSemaphore.release();

            //start  work on the next hunk
            hunkIndex++;
            doubleBuffer.setBack(bufferSupply.get());

            nextHunkLimit = hunkIndex*hunkSize.getValuei();
            last_hunk_frame = inFrame;
            // write our next value into the image buffer
        }
        int[] carr = (int[]) colors.getArray(PolyBuffer.Space.RGB8);

        // write to the back buffer
        doubleBuffer.getBack().img.setRGB(0, inFrame - last_hunk_frame, model.points.length, 1, carr, 0, model.points.length);
    }
}
