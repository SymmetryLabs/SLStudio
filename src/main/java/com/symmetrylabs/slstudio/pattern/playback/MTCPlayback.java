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
    private final MutableParameter MTCOffset = new MutableParameter("MTC_OFFSET", 0);

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
    String currentSongName;

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
//            SLStudio.setWarning("FILE", currentSong.getAbsolutePath());
        }
    }

    private void goToFrame(int frame) {
        lastFrameReceived = frame;
        decodeSongOffset(frame);
    }

    private void decodeSongOffset(int frame) {
        final int BIN_SIZE = 9000;
        int songIndex = frame/BIN_SIZE;


        final int LEVITATE = 12;
        final String sLEVITATE = "1h_0m";
        final int FAIRLY_LOCAL_1 = 18;
        final String sFAIRLY_LOCAL_1 = "1h_30m";
        final int FAIRLY_LOCAL_2 = 21;
        final String sFAIRLY_LOCAL_2 = "1h_45m";

        final int HEATHENS = 36;
        final String sHEATHENS = "3h_0m";

        switch(songIndex){
            case LEVITATE: // levitate
                if (currentSongName == sLEVITATE){ break; }
                currentSongName = sLEVITATE;
                renderFile.setValue("/Users/symmetry/symmetrylabs/software/SLStudio/shows/pilots/render/" + currentSongName + ".png");
                MTCOffset.setValue(songIndex*BIN_SIZE);
                loadSong();
                break;
            case FAIRLY_LOCAL_1:
                if (currentSongName == sFAIRLY_LOCAL_1){ break; }
                currentSongName = sFAIRLY_LOCAL_1;
                renderFile.setValue("/Users/symmetry/symmetrylabs/software/SLStudio/shows/pilots/render/" + currentSongName + ".png");
                MTCOffset.setValue(songIndex*BIN_SIZE);
                loadSong();
                break;
            case FAIRLY_LOCAL_2:
                if (currentSongName == sFAIRLY_LOCAL_2){ break; }
                currentSongName = sFAIRLY_LOCAL_2;
                renderFile.setValue("/Users/symmetry/symmetrylabs/software/SLStudio/shows/pilots/render/" + currentSongName + ".png");
                MTCOffset.setValue(songIndex*BIN_SIZE);
                loadSong();
                break;
            case HEATHENS:
                if (currentSongName == sHEATHENS){ break; }
                currentSongName = sHEATHENS;
                renderFile.setValue("/Users/symmetry/symmetrylabs/software/SLStudio/shows/pilots/render/" + currentSongName + ".png");
                MTCOffset.setValue(songIndex*BIN_SIZE);
                loadSong();
                break;
        }
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

        int frameIn = lastFrameReceived - MTCOffset.getValuei();
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
                ccs[i/4] = ((fourValArray[i+3] & 0xff) << 24) | ((fourValArray[i] & 0xff) << 16) | ((fourValArray[i+1] & 0xff) << 8) | (fourValArray[i + 2] & 0xff);
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
            "time %s / frame %d / current song: %s / file %s ",
            mt == null ? "unknown" : mt.toString(),
            lastFrameRendered,
            getCurrentSongName(),
            renderFile.getString()
        );
    }

    private String getCurrentSongName() {
        return currentSongName;
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
