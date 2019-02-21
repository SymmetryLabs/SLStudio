package com.symmetrylabs.slstudio.pattern.playback;

import ar.com.hjg.pngj.IImageLine;
import ar.com.hjg.pngj.ImageLineInt;
import ar.com.hjg.pngj.PngReader;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import heronarts.lx.midi.LXMidiInput;
import heronarts.lx.midi.MidiTime;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.MutableParameter;
import heronarts.lx.parameter.StringParameter;
import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Iterator;

public class MTCPlayback extends SLPattern<SLModel> {
//public class MTCPlayback extends LXPattern {
    private static final String TAG = "MTC";

    private static final int CIRCULAR_BUFFER_SIZE = 16;
    private static final int JUMP_FRAME_SENSITIVITY = 300; // 10 sec should be good

    protected final StringParameter renderFile = new StringParameter("renderFile", "");
    protected final BooleanParameter filePickerDialogue = new BooleanParameter("choose render", false).setMode(BooleanParameter.Mode.MOMENTARY);
    private final MutableParameter MTCOffset = new MutableParameter("MTC_OFFSET", 0);
    private final BooleanParameter freewheel = new BooleanParameter("run", false);


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
    private long startFrameNanos = -1;
    private double frameRate = 30;

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
        addParameter(freewheel);


        setupMTCListeners();


        hunkRingbuffer = new CircularFifoQueue<>(CIRCULAR_BUFFER_SIZE);
        colorBufRing = new CircularFifoQueue<>(CIRCULAR_BUFFER_SIZE);

        itter = colorBufRing.iterator();

        writerThread = new CirclularBufferWriterThread();

        prepareSongMaps();

        SongIndex.allSongIndicesByBin.get(12);

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

    protected void goToFrame(int frame) {
        lastFrameReceived = frame;
        decodeSongOffset(frame);
    }

    private void prepareSongMaps(){

        final int BIN_PER_HOUR = 12;
        // these "keys" for each song are the LTC time for the song integer divide by 5 minutes.
        // 5 minutes because no songs are greater than 5 minutes, and because each falls on 5 min interval.
        // i.e. for a song from 0h_30m_0s_0f - 0h_33m_10s_0f the index is 6
        final int JUMPSUIT = 6;
        final int LEVITATE = 12;
        final int FAIRLY_LOCAL_1 = 18;
        final int FAIRLY_LOCAL_2 = 21;
        final int HEATHENS = 36;
        final int JUDGE = 48;
        final int CUT = 222;
        final int LANE_BOY1 = 54;
        final int LANE_BOY2 = 54;
        final int NICO = 60;
        final int CHEETAH = (int)((14 + 31.0/60 + 3.0/(60*60) )*BIN_PER_HOUR);
        final int HOLDING = 126;
        final int RIDE = (int)(9*BIN_PER_HOUR);
        final int RIDE2 = (int)(9.25*BIN_PER_HOUR);
        final int MY_BLOOD = (int)(8.5*BIN_PER_HOUR);
        final int MORPH1 = (int)(8*BIN_PER_HOUR);
//        final int MORPH2 = (int)((8 + 3.0/60 + 36.0/(60*60) )*BIN_PER_HOUR);
        final int CAR_RADIO = (int) (7*BIN_PER_HOUR);
        final int CHLORINE = (int) (18*BIN_PER_HOUR);
        final int LEAVE_THE_CITY = (int) (11*BIN_PER_HOUR);
        final int TREES = (int) (11.5*BIN_PER_HOUR);


        // we don't need to assign these to any local variables. The SongIndex constructor
        // stores them in 'allSongIndicesByBin' HashMap
        new SongIndex("Jumpsuit", JUMPSUIT);
        new SongIndex("Levitate", LEVITATE);
        new SongIndex("FairlyLocal1", FAIRLY_LOCAL_1);
        new SongIndex("FairlyLocal2", FAIRLY_LOCAL_2);
        new SongIndex("Heathens", HEATHENS);
        new SongIndex("Judge", JUDGE);
        new SongIndex("CutMyLip", CUT);
        new SongIndex("LaneBoy1", LANE_BOY1);
        new SongIndex("LaneBoy2", LANE_BOY2);
        new SongIndex("nico", NICO).addOffset(0, 1, 4);
        new SongIndex("PetCheetah", CHEETAH).addOffset(1,3,7);
        new SongIndex("HoldingOntoYou", HOLDING);
        new SongIndex("Ride1", RIDE);
        new SongIndex("Ride2", RIDE2);
        new SongIndex("MyBlood", MY_BLOOD);
        new SongIndex("Morph1", MORPH1);
//        new SongIndex("Morph2", MORPH2);
        new SongIndex("CarRadio", CAR_RADIO);
        new SongIndex("Chlorine", CHLORINE);
        new SongIndex("LeaveTheCity", LEAVE_THE_CITY);
        new SongIndex("Trees", TREES);
    }

    private void decodeSongOffset(int frame) {
        final int BIN_SIZE = 9000;
        int songIndex = frame/BIN_SIZE;

        SongIndex thisSong = SongIndex.allSongIndicesByBin.get(songIndex);

        if (thisSong == null){
            return;
        }

        if (thisSong.isNew()){
            if (loadByName(thisSong)){
                return;
            }
            else{
                String dataPath = "/Users/symmetry/symmetrylabs/software/SLStudio/shows/pilots/render/";
                currentSongName = thisSong.song_name;
                renderFile.setValue( dataPath + thisSong.name_png + ".png");
                MTCOffset.setValue(songIndex*BIN_SIZE + thisSong.extra_MTC_offset);
                loadSong();
            }
        }
    }

    private boolean loadByName(SongIndex thisSong) {
        String dataPath = "/Users/symmetry/symmetrylabs/software/SLStudio/shows/pilots/render/";
        renderFile.setValue( dataPath + thisSong.song_name + ".png");
        String path = renderFile.getString();
        File dir = new File(path);
        if (dir.isFile() && dir.getName().endsWith(".png")) {
            currentSong = dir;
            pngr = new PngReader(currentSong);
            return true;
        }
        return false;
    }


    public void loadSong() {
        String path = renderFile.getString();
        if (path == null) {
            System.err.println("invalid path");
            // trying the song name
        }
        File dir = new File(path);
        if (dir.isFile() && dir.getName().endsWith(".png")) {
            currentSong = dir;
            pngr = new PngReader(currentSong);
        }
        else{
            System.err.println("must be png");
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



    long last_frame_rendered_time = 0;
    @Override
    public void run(double deltaMs, PolyBuffer.Space preferredSpace) {
        super.run(deltaMs, preferredSpace);

        int frameIn = -1;
        if (freewheel.getValueb()) {
            if (startFrameNanos == -1) {
                startFrameNanos = System.nanoTime();
            }
            double elapsedSec = 1e-9 * (double) (System.nanoTime() - startFrameNanos);
            int inFrame = (int) Math.floor(frameRate * elapsedSec);
            frameIn = inFrame;
        }
        else {
            startFrameNanos = -1; // reset for freewheel
            frameIn = lastFrameReceived - MTCOffset.getValuei();
        }
        if (pngr == null){
            if (renderFile.getString() != ""){
                loadSong();
            }
            return;
        }
        if (frameIn == lastFrameRendered){
            return;
        }




        // if either of the following two are true then we need a reset.
        if (frameIn < lastFrameRendered){
            recyclePNGReader();
            lastFrameReceived = -1;
            lastFrameRendered = -1;
            return;
        }

        if ( (lastFrameRendered != -1) && (frameIn > lastFrameRendered + JUMP_FRAME_SENSITIVITY)){
            recyclePNGReader();
            lastFrameReceived = -1;
            lastFrameRendered = -1;
            return;
        }

        int[] ccs = (int[]) getArray(PolyBuffer.Space.SRGB8);
        try {
            IImageLine l1 = pngr.readRow(frameIn);
            int[] fourValArray = ((ImageLineInt) l1).getScanline();

            for (int i = 0; i < fourValArray.length; i += 4){
//                ccs[i/4] = ((fourValArray[i+3] & 0xff) << 24) | ((fourValArray[i] & 0xff) << 16) | ((fourValArray[i+1] & 0xff) << 8) | (fourValArray[i + 2] & 0xff);
                //hmm... seems like alpha was fucked above.  Let's just always make it fully opaque.
                ccs[i/4] = ((0xff) << 24) | ((fourValArray[i] & 0xff) << 16) | ((fourValArray[i+1] & 0xff) << 8) | (fourValArray[i + 2] & 0xff);
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
            currentSong == null ? "null" : currentSong.getName()
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

class SongIndex {
    static HashMap<Integer, SongIndex> allSongIndicesByBin = new HashMap<>();

    final int index;
    final String name_png;
    final String song_name;

    int extra_MTC_offset = 0;

    static SongIndex currentSong;
    public boolean isNew() {
        if (this == currentSong){
            return false;
        }
        currentSong = this;
        return true;
    }

    private String formatName(int hunk_in) {
        int hours = hunk_in/12;
        int min = (hunk_in%12)*5;
        return String.format("%dh_%dm",
            hours,
            min
        );
    }

    // add an additional offset
    void addOffset(int m, int s, int f){
        extra_MTC_offset = (m*60 + s) * 30 + f;
    }

    SongIndex(String song_name, int i){
        index = i;
        name_png = formatName(i);
        this.song_name = song_name;
        allSongIndicesByBin.put(i, this);
    }
}
