package com.symmetrylabs.slstudio.pattern.playback;

import ar.com.hjg.pngj.IImageLine;
import ar.com.hjg.pngj.ImageLineInt;
import ar.com.hjg.pngj.PngReader;
import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import heronarts.lx.midi.LXMidiInput;
import heronarts.lx.midi.MidiTime;
import heronarts.lx.parameter.*;
import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Iterator;

public class MTCPlayback extends SLPattern<SLModel> {
    private static final String TAG = "MTC";

    private static final int CIRCULAR_BUFFER_SIZE = 16;

    private final StringParameter renderFile = new StringParameter("renderFile", "");
    private final BooleanParameter filePickerDialogue = new BooleanParameter("choose render", false).setMode(BooleanParameter.Mode.MOMENTARY);

    public final MutableParameter hunkSize = new MutableParameter("hunkSize", 150);

    CircularFifoQueue<Hunk> hunkRingbuffer;

    CircularFifoQueue<int[]> colorBufRing;

    PngReader pngr;
    private Iterator<int[]> itter;

    CirclularBufferWriterThread writerThread;
    private MidiTime mt;
    protected int lastFrameReceived = -1;
    private int lastFrameRendered = -1;

    File currentSong;

    // this thread keeps the circular buffer stocked at the current MTC offset with some "lookahead"
    class CirclularBufferWriterThread extends Thread{


        @Override
        public void run() {

//            File file = new File(renderFile.getString());

            DebugTimer timer = new DebugTimer("hi");

            while(!ready()){}

            int frame_index = 0;
            while(pngr.hasMoreRows()){
//                timer.start();
                while ( !colorBufRing.isAtFullCapacity() ){
                    IImageLine l1 = pngr.readRow(frame_index++);
                    colorBufRing.add(((ImageLineInt) l1).getScanline());
                }
//                timer.stop("row");
            }

        }
    }

    public MTCPlayback(LX lx){
        super(lx);
        addParameter(filePickerDialogue);
        addParameter(renderFile);


        setupMTCListeners();


        hunkRingbuffer = new CircularFifoQueue<>(CIRCULAR_BUFFER_SIZE);
        colorBufRing = new CircularFifoQueue<>(CIRCULAR_BUFFER_SIZE);

        itter = colorBufRing.iterator();

        writerThread = new CirclularBufferWriterThread();

        if (renderFile.getString() != ""){
            loadSong();
            SLStudio.setWarning("FILE", "file: " + currentSong.getAbsolutePath());
        }
        else {
            SLStudio.setWarning("FILE", "nofile");
        }

//        writerThread.start();

        // turns out the PNG reader can retrieve lines from the PNG faster than 30Hz..
        // do it all in the main thread i guess..
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
    }

    @Override
    public void onParameterChanged(LXParameter p) {
        if (p == filePickerDialogue && filePickerDialogue.getValueb()) {
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
            renderFile.setValue(rel.toString());
            loadSong();
            SLStudio.setWarning("FILE", currentSong.getAbsolutePath());
        }
    }

    private void goToFrame(int frame) {
        lastFrameReceived = frame;
    }

    public File loadSong() {
        String path = renderFile.getString();
        if (path == null) {
            System.err.println("invalid path");
            return null;
        }
        File dir = new File(path);
        if (dir.isFile() && dir.getName().endsWith(".png")) {
            currentSong = dir;
            pngr = new PngReader(currentSong);
            return dir;
        }
        else{
            System.err.println("must be png");
            return null;
        }
    }

    public boolean ready() {
        if (pngr == null){
            return false;
        }
        else{
            return true;
        }
    }



    @Override
    public void run(double deltaMs, PolyBuffer.Space preferredSpace) {
        super.run(deltaMs, preferredSpace);

        int frameIn = lastFrameReceived;
        if (pngr == null){
            if (renderFile.getString() != ""){
                loadSong();
            }
            return;
        }
        if (frameIn == lastFrameRendered){
            return;
        }
        if (frameIn < lastFrameRendered){
            recyclePNGReader();
            lastFrameReceived = -1;
            lastFrameRendered = -1;
        }

        int[] ccs = (int[]) getArray(PolyBuffer.Space.SRGB8);
        Arrays.fill(ccs, 0x80);

        try {
            IImageLine l1 = pngr.readRow(frameIn);
            int[] fourValArray = ((ImageLineInt) l1).getScanline();

            for (int i = 0; i < fourValArray.length; i += 4){
                ccs[i/4] = ((fourValArray[i+3] & 0xff) << 24) | ((fourValArray[i+2] & 0xff) << 16) | ((fourValArray[i+1] & 0xff) << 8) | (fourValArray[i] & 0xff);
            }
        } catch (ar.com.hjg.pngj.PngjInputException e){
            System.err.println(e);
        }

        lastFrameRendered = frameIn;
        markModified(PolyBuffer.Space.SRGB8);
    }

    private void recyclePNGReader() {
        pngr = null;
        loadSong();
    }

    @Override
    public String getCaption() {
        return String.format(
            "time %s / frame %d / file %s / current song: %s",
            mt == null ? "unknown" : mt.toString(),
            lastFrameRendered,
            renderFile.getString(),
            getCurrentSongName());
    }

    private String getCurrentSongName() {
        return "nico";
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
