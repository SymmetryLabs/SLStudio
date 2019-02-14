package com.symmetrylabs.slstudio.output;

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
    private long startFrameNanos;
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
                    goToFrame(frame);
                }
            });
            System.out.println("attached time code listener to " + input.getName());
        }
    }

    private void goToFrame(int frame) {
        extTrigger.setValue(true);
    }

    public void dispose() {
        img = null;
        pStatus.setValue("IDLE");
    }

    private void createImage() {
        startFrameNanos = System.nanoTime();
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
        }
        pStatus.setValue("REC");

        double elapsedSec = 1e-9 * (double) (System.nanoTime() - startFrameNanos);
        int inFrame = (int) Math.floor(frameRate * elapsedSec);
        if (inFrame <= lastFrameWritten) {
            return;
        }
        lastFrameWritten = inFrame;
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
            img.setRGB(0, lastFrameWritten, model.points.length, 1, carr, 0, model.points.length);
        }
    }
}
