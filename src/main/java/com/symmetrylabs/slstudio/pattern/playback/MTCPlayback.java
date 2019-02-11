package com.symmetrylabs.slstudio.pattern.playback;

import ar.com.hjg.pngj.IImageLine;
import ar.com.hjg.pngj.ImageLineInt;
import ar.com.hjg.pngj.PngReader;
import com.jogamp.common.util.Ringbuffer;
import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.PolyBuffer;
import heronarts.lx.midi.LXMidiInput;
import heronarts.lx.midi.MidiTime;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.MutableParameter;
import heronarts.lx.parameter.StringParameter;
import heronarts.lx.transform.LXVector;
import org.apache.commons.collections4.queue.CircularFifoQueue;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.NumberFormat;
import java.util.Iterator;

public class MTCPlayback extends LXPattern {
    private static final int CIRCULAR_BUFFER_SIZE = 16;
    public final StringParameter renderFile = new StringParameter("renderFile");
    public final BooleanParameter filePickerDialogue = new BooleanParameter("choose render").setMode(BooleanParameter.Mode.MOMENTARY);

    public final MutableParameter hunkSize = new MutableParameter("hunkSize", 150);

    CircularFifoQueue<Hunk> hunkRingbuffer;

    CircularFifoQueue<int[]> colorBufRing;

    PngReader pngr;
    private Iterator<int[]> itter;

    CirclularBufferWriterThread writerThread;
    private MidiTime mt;
    private int lastFrameReceived = -1;
    private int lastFrameRendered = -1;

    public MTCPlayback(LX lx){
        super(lx);
        addParameter(filePickerDialogue);
        addParameter(renderFile);


        setupMTCListeners();


        hunkRingbuffer = new CircularFifoQueue<>(CIRCULAR_BUFFER_SIZE);
        colorBufRing = new CircularFifoQueue<>(CIRCULAR_BUFFER_SIZE);

        itter = colorBufRing.iterator();

        writerThread = new CirclularBufferWriterThread();
    }

    private void setupMTCListeners() {
        for (LXMidiInput input : lx.engine.midi.inputs) {
            input.addTimeListener(new LXMidiInput.MidiTimeListener() {
                @Override
                public void onBeatClockUpdate(int i, double v) {
                }

                @Override
                public void onMTCUpdate(MidiTime midiTime) {
                    mt = midiTime.clone();
//                    if (freewheel.getValueb()) {
//                        return;
//                    }
                    int frame = mt.getHour();
                    frame = 60 * frame + mt.getMinute();
                    frame = 60 * frame + mt.getSecond();
                    frame = mt.getRate().fps() * frame + mt.getFrame();
                    goToFrame(frame);
                }
            });
            System.out.println("attached time code listener to " + input.getName());
        }
    }

    @Override
    public void onActive() {
        super.onActive();
        loadDirectory();
        writerThread.start();
    }

    private void loadDirectory() {
        renderFile.setValue("/Users/symmetry/symmetrylabs/software/000_RENDERER/0_2min_30fps_sss.png");
    }

    private void goToFrame(int frame) {
        lastFrameReceived = frame;
    }

    class CirclularBufferWriterThread extends Thread{


        @Override
        public void run() {
            File file = new File(renderFile.getString());

            DebugTimer timer = new DebugTimer("hi");

            PngReader pngr = new PngReader(file);

            // ok this could read in all the rows that we need here in another thread that tries to keep the ring buffer full.
//            for (int row = 0; row < hunkSize.getValuei(); row++) { // also: while(pngr.hasMoreRows())
            while(pngr.hasMoreRows()){
//                timer.start();
                while ( !colorBufRing.isAtFullCapacity() ){
                    IImageLine l1 = pngr.readRow();
                    colorBufRing.add(((ImageLineInt) l1).getScanline());
                }
//                timer.stop("row");
            }

        }
    }


    @Override
    protected void run(double deltaMs, PolyBuffer.Space preferredSpace) {
        super.run(deltaMs, preferredSpace);

        if (lastFrameReceived == lastFrameRendered){
            return;
        }

        int[] ccs = (int[]) getArray(PolyBuffer.Space.SRGB8);

        if (itter.hasNext()){
            ccs = itter.next();
            lastFrameRendered = lastFrameReceived;
        }
        else{
            System.err.println("out of frames");
        }
        markModified(PolyBuffer.Space.SRGB8);
    }
}

class DebugTimer {
    long loadEnd;
    long loadStart;
    String tag;

    DebugTimer(String tag){
        this.tag = tag;
    }

    public void start(){
        loadStart  = System.nanoTime();
    }

    public void stop(String tag){
        loadEnd = System.nanoTime();
        this.tag = tag;
        long deltaNanos = loadEnd - loadStart;
        double elapsedSec = 1e-9 * (double) (deltaNanos);
        System.out.println( tag + " - " + elapsedSec);
    }


    public void memReport(){
        Runtime runtime = Runtime.getRuntime();

        NumberFormat format = NumberFormat.getInstance();

        StringBuilder sb = new StringBuilder();
        long maxMemory = runtime.maxMemory();
        long allocatedMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();

        sb.append("free memory: " + format.format(freeMemory / 1024) + "\n");
        sb.append("allocated memory: " + format.format(allocatedMemory / 1024) + "\n");
        sb.append("max memory: " + format.format(maxMemory / 1024) + "\n");
        sb.append("total free memory: " + format.format((freeMemory + (maxMemory - allocatedMemory)) / 1024) + "\n\n");
        System.out.println(sb);
    }
}
