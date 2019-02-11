package com.symmetrylabs.slstudio.pattern.playback;

import ar.com.hjg.pngj.IImageLine;
import ar.com.hjg.pngj.PngReader;
import com.jogamp.common.util.Ringbuffer;
import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.MutableParameter;
import heronarts.lx.parameter.StringParameter;
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

public class MTCPlayback extends LXPattern {
    private static final int CIRCULAR_BUFFER_SIZE = 4;
    public final StringParameter renderFile = new StringParameter("renderFile");
    public final BooleanParameter filePickerDialogue = new BooleanParameter("choose render").setMode(BooleanParameter.Mode.MOMENTARY);

    public final MutableParameter hunkSize = new MutableParameter("hunkSize", 150);

    CircularFifoQueue<Hunk> hunkRingbuffer;

    CircularFifoQueue<int[]> colorBufRing;

    PngReader pngr;

    public MTCPlayback(LX lx){
        super(lx);
        addParameter(filePickerDialogue);
        addParameter(renderFile);

        hunkRingbuffer = new CircularFifoQueue<>(CIRCULAR_BUFFER_SIZE);
    }

    public void fillBuffers(){


        File file = new File(renderFile.getString());
        ImageInputStream iis = null;

        DebugTimer timer = new DebugTimer("hi");

        /*
        READ ROI ONLY FOR EFFICIENCY WITH LARGE PNG HUNK
         */


        PngReader pngr = new PngReader(file);


        byte[] tmpReadIn = new byte[pngr.imgInfo.cols];


        int channels = pngr.imgInfo.channels;
        int imgRows = pngr.imgInfo.rows;
        int imgCols = pngr.imgInfo.cols;
        System.out.println(pngr.toString() + " --- numChannel: " + channels + " --- rows: " + imgRows);


        // ok this could read in all the rows that we need here in another thread that tries to keep the ring buffer full.
        for (int row = 0; row < hunkSize.getValuei(); row++) { // also: while(pngr.hasMoreRows())
            timer.start();
            IImageLine l1 = pngr.readRow();
            timer.stop("row");
        }


        try {
            iis = ImageIO.createImageInputStream(file);
            ImageReader reader = ImageIO.getImageReaders(iis).next();
            reader.setInput(iis);

            ImageReadParam param = reader.getDefaultReadParam();

            int roiW = reader.getWidth(0);
            int roiH = hunkSize.getValuei();
//            int roiH = reader.getHeight(0);

            int offset = 0;

            System.out.println("Capacity: " + hunkRingbuffer.maxSize());
            while (! hunkRingbuffer.isFull()){

                Rectangle srcROI = new Rectangle(0, offset, roiW, offset + roiH);
                offset += roiH;

                param.setSourceRegion(srcROI);

                System.out.println('\n');
                timer.start();
                BufferedImage dataBufIn = reader.read(0, param);
                timer.stop("read");


                System.out.println("dataBufIn: w" + dataBufIn.getWidth() + " h" + dataBufIn.getHeight());

                timer.start();
                hunkRingbuffer.add(new Hunk(dataBufIn, offset));
                timer.stop("add");
            }
            iis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        this.tag = tag;
        loadEnd = System.nanoTime();
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
