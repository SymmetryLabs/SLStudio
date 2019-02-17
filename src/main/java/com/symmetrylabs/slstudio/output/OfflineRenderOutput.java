package com.symmetrylabs.slstudio.output;

import com.symmetrylabs.slstudio.SLStudio;
import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import heronarts.lx.midi.LXMidiInput;
import heronarts.lx.midi.MidiTime;
import heronarts.lx.model.LXModel;
import heronarts.lx.output.LXOutput;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.StringParameter;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class OfflineRenderOutput extends LXOutput {
    public static final String HEADER = "SLOutput";
    public static final int VERSION = 3;

    private File output = null;
    private LXModel model;
    private long startFrameNanos = -1;
    private int lastFrameWritten;
    private BufferedImage img = null;
    private int framesToCapture;
    private double frameRate;

    public final BooleanParameter pStart = new BooleanParameter("pStart", false).setMode(BooleanParameter.Mode.MOMENTARY);
    public final DiscreteParameter pFramesToCapture = new DiscreteParameter("frames", 1200, 0, 30000);
    public final CompoundParameter pFrameRate = new CompoundParameter("rate", 30, 1, 60);
    public final StringParameter pOutputFile = new StringParameter("output", "");
    public final StringParameter pStatus = new StringParameter("status", "IDLE");
    public final BooleanParameter externalSync = new BooleanParameter("extern");
    private final BooleanParameter extTrigger = new BooleanParameter("trigger");
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
            if (!externalSync.getValueb()) {
                if (img != null && extTrigger.isOn()){
                    dispose();
                }
            }
        });

        arm_MTC_listeners(lx);
    }

    private void arm_MTC_listeners(LX lx){
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
                    triggerRecord(frame);
                }
            });
            System.out.println("attached time code listener to " + input.getName());
        }
    }

    private void triggerRecord(int frame) {
        // vezer sends frame zero when jump to start of project, ignore this.
        if (externalSync.isOn()){
            if (frame == 0){ return; }
            extTrigger.setValue(true);
        }
    }

    public void dispose() {
        img = null;
        // reset the trigger.
        extTrigger.setValue(false);
        pStatus.setValue("IDLE");
        startFrameNanos = -1;
    }

    private void createImage() {
        if (externalSync.isOn()){
            startFrameNanos = -1;
        }
        else{
            startFrameNanos = System.nanoTime();
        }
        lastFrameWritten = -1;
        framesToCapture = pFramesToCapture.getValuei();
        frameRate = pFrameRate.getValue();
        img = new BufferedImage(model.points.length, pFramesToCapture.getValuei(), BufferedImage.TYPE_INT_ARGB);
    }

    @Override
    protected void onSend(PolyBuffer colors) {
        if (img == null) {
            return;
        }
        else if (externalSync.isOn()){
            if (!extTrigger.isOn()){
                pStatus.setValue("WAIT");
                return;
            }
            if (startFrameNanos == -1){
                startFrameNanos = System.nanoTime();
            }
        }

        double elapsedSec = 1e-9 * (double) (System.nanoTime() - startFrameNanos);
        int inFrame = (int) Math.floor(frameRate * elapsedSec);
        if (inFrame <= lastFrameWritten) {
            return;
        }

        // if we have skipped any frames keep track of how many.
        // this will happen if the engine framerate falls below the framerate
        // specified in the header of the file we are writing to.
        // There is not a "real" header however I usually "pseudo-encode" this as follows
        // i.e. "chlorine.fps30.png"
        int framesSkipped = -1;
        if (inFrame > (lastFrameWritten + 1)){
            framesSkipped = inFrame - (lastFrameWritten + 1);
            SLStudio.setWarning("OFFLINERENDER", "Skipped Frames: " + framesSkipped);
        }


        lastFrameWritten = inFrame;
        pStatus.setValue("REC:"+ lastFrameWritten);
        if (lastFrameWritten >= framesToCapture) {
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
        } else {
            int[] carr = (int[]) colors.getArray(PolyBuffer.Space.RGB8);


            // we have missed some frames copy this frame in to those frames so that we don't have any blackout frames.
            if (framesSkipped != -1){
                for (int i = 1; i <= framesSkipped; i++){
                    assert inFrame - i >= 0;
                    img.setRGB(0, inFrame - i, model.points.length, 1, carr, 0, model.points.length);
                }
            }

            img.setRGB(0, inFrame, model.points.length, 1, carr, 0, model.points.length);
        }
    }
}
