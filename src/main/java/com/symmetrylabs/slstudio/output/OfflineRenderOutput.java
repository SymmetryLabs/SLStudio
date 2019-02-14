package com.symmetrylabs.slstudio.output;

import com.symmetrylabs.util.CaptionSource;
import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import heronarts.lx.midi.LXMidiInput;
import heronarts.lx.midi.MidiTime;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.output.LXOutput;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.StringParameter;
import java.awt.EventQueue;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class OfflineRenderOutput extends LXOutput implements CaptionSource {
    public static final String HEADER = "SLOutput";
    public static final int VERSION = 3;

    private File output = null;
    private LXModel model;
    private long startFrameNanos;
    private int lastFrameWritten;
    private BufferedImage img = null;
    private int framesToCapture;
    private double frameRate;

    protected int currentFrame = -1;

    public final BooleanParameter pStart = new BooleanParameter("pStart", false).setMode(BooleanParameter.Mode.MOMENTARY);
    public final DiscreteParameter pFramesToCapture = new DiscreteParameter("frames", 60, 0, 30000);
    public final CompoundParameter pFrameRate = new CompoundParameter("rate", 30, 1, 60);
    public final StringParameter pOutputFile = new StringParameter("output", "");
    public final StringParameter pStatus = new StringParameter("status", "IDLE");

    public final BooleanParameter externalSync = new BooleanParameter("ext", false);

    // only used in continuous offline output
    public StringParameter pOutputDir = new StringParameter("HunkDataDir", "");
    private MidiTime mt;

    public OfflineRenderOutput(LX lx) {
        super(lx);
        this.model = lx.model;
        pOutputFile.addListener(p -> {
                dispose();
                if (!pOutputFile.getString().equals("")) {
                    output = new File(pOutputFile.getString());
                }
            });
        pStart.addListener(p -> {
                if (pStart.getValueb()) {
                    createImage();
                }
            });
        externalSync.addListener(p -> {
            if (externalSync.getValueb()) {
                createImage();
            }
            else{
                if (img != null){
                    writeAndDispose();
                }
                dispose();
            }
        });

        addMTCListeners(lx);
    }

    private void addMTCListeners(LX lx){
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
//                    updateFrame(frame);
                    currentFrame = frame;
                }
            });
            System.out.println("attached time code listener to " + input.getName());
        }
    }
    public void dispose() {
        img = null;
        currentFrame = -1;
        offset_frame = -1;
        debounce_frames = 20;
        pStatus.setValue("IDLE");
    }

    private void createImage() {
        startFrameNanos = System.nanoTime();
        lastFrameWritten = -1;
        framesToCapture = pFramesToCapture.getValuei();
        frameRate = pFrameRate.getValue();
        img = new BufferedImage(model.points.length, pFramesToCapture.getValuei(), BufferedImage.TYPE_INT_ARGB);
    }


    static int debounce_frames = 10;
    int offset_frame = -1;
    @Override
    protected void onSend(PolyBuffer colors) {
        if (img == null) {
            return;
        }
        int inFrame;
        if (externalSync.getValueb()){
            if (debounce_frames > 0){
                debounce_frames--;
                return;
            }
            if (offset_frame == -1){
                offset_frame = currentFrame;
            }
            inFrame = currentFrame - offset_frame;
            if (inFrame <= 0){
                pStatus.setValue("WAIT_EXT");
                return;
            }
            pStatus.setValue("REC:" + inFrame);
        }
        else {
            pStatus.setValue("REC");
            double elapsedSec = 1e-9 * (double) (System.nanoTime() - startFrameNanos);
            inFrame = (int) Math.floor(frameRate * elapsedSec);
            if (inFrame <= lastFrameWritten) {
                return;
            }
        }

        if (inFrame >= framesToCapture) {
            // this is where we save the file.
            writeAndDispose();
        } else {
            int[] carr = (int[]) colors.getArray(PolyBuffer.Space.RGB8);
            img.setRGB(0, inFrame, model.points.length, 1, carr, 0, model.points.length);
            lastFrameWritten = inFrame;
        }
    }

    private void writeAndDispose() {
            final BufferedImage imgToWrite = img;
            final File outputToWrite = output;
            EventQueue.invokeLater(() -> {
                try {
                    ImageIO.write(imgToWrite, "png", outputToWrite);
                } catch (IOException e) {
                    System.err.println("couldn't save output image:");
                    e.printStackTrace();
                }
            });
            dispose();
    }

    @Override
    public String getCaption() {
        return "test caption";
    }
}
