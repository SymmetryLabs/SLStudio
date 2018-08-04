package com.symmetrylabs.layouts.cubes.patterns.pilots;

import com.symmetrylabs.color.Ops8;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.midi.MidiNote;
import heronarts.lx.midi.MidiNoteOn;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.transform.LXVector;

public class PilotsSpirits extends SLPattern<SLModel> {
    private CompoundParameter spiritSizeParam = new CompoundParameter("size", 200, 1, 600);
    private CompoundParameter spiritHeartParam = new CompoundParameter("heart", 75, 1, 600);

    private static final float GRAVITATIONAL_CONSTANT = 50;
    private static final float RED_MASS = 2;
    private static final float MAX_VELOCITY = 400; // inches per second
    private static final float RED_CHASE_DELAY = 400; // ms
    private static final float CHASE_PERIOD = 2700; // ms

    SinLFO redRestLfo = new SinLFO(-40, 30, 4500);
    SinLFO yellowRestLfo = new SinLFO(35, -20, 5100);
    double phaseAge = 0;

    enum Phase {
        MOVE_IN,
        RED_STRONGER,
        SHAKING,
        CHASE,
    }

    Phase phase = Phase.MOVE_IN;

    LXVector redBase, yellowBase;
    float redVelocity = 0;

    boolean suppressYellow = false;

    public PilotsSpirits(LX lx) {
        super(lx);

        addParameter(spiritSizeParam);
        addParameter(spiritHeartParam);
        addModulator(redRestLfo);
        addModulator(yellowRestLfo);

        redRestLfo.start();
        yellowRestLfo.start();

        redBase = new LXVector(model.xMin, model.cy, model.cz);
        yellowBase = new LXVector(model.xMax, model.cy, model.cz);
    }

    @Override
    public void run(double elapsedMs) {
        phaseAge += elapsedMs;

        int black = LXColor.gray(0);
        for (int i = 0; i < colors.length; i++)
            colors[i] = black;

        switch (phase) {
            case MOVE_IN: {
                if (yellowBase.x > model.cx) {
                    yellowBase.x -= Math.min(elapsedMs * 60 / 1000, yellowBase.x - model.cx);
                }
                if (redBase.x < model.cx) {
                    redBase.x += Math.min(elapsedMs * 60 / 1000, model.cx - redBase.x);
                }
                break;
            }
            case CHASE: {
                yellowBase.x = model.cx + model.xRange * (float) Math.sin(phaseAge / CHASE_PERIOD * 2 * Math.PI) / 2;
                if (phaseAge > RED_CHASE_DELAY)
                    redBase.x = model.cx + model.xRange * (float) Math.sin((phaseAge - RED_CHASE_DELAY) / CHASE_PERIOD * 2 * Math.PI) / 2;
                break;
            }
        }

        LXVector yLoc = new LXVector(
            -yellowRestLfo.getValuef() / 1.5f,
            yellowRestLfo.getValuef() / 2,
            -yellowRestLfo.getValuef());
        yLoc.add(yellowBase);

        LXVector rLoc = new LXVector(-redRestLfo.getValuef() / 3, redRestLfo.getValuef(), -redRestLfo.getValuef() / 7);
        rLoc.add(redBase);

        float areaEffect = spiritSizeParam.getValuef();
        float heartSize = spiritHeartParam.getValuef();

        float rHue = 0;
        float yHue = suppressYellow ? rHue : 45;

        for (LXVector v : getVectors()) {
            double rDist = rLoc.dist(v) - heartSize;
            double yDist = yLoc.dist(v) - heartSize;

            int rColor = rDist < 0
                ? LXColor.hsb(rHue, 100 * (1 + rDist / heartSize), 100)
                : LXColor.hsb(rHue, 100, 100 * Math.max(0, 1 - rDist / areaEffect));

            int yColor = yDist < 0
                ? LXColor.hsb(yHue, 100 * (1 + yDist / heartSize), 80)
                : LXColor.hsb(yHue, 100, 80 * Math.max(0, 1 - yDist / areaEffect));
            int color = Ops8.screen(rColor, yColor);
            colors[v.index] = color;
        }
    }

    @Override
    public void noteOnReceived(MidiNoteOn note) {
        if (note.getPitch() == 60) {
            switch (phase) {
                case MOVE_IN:
                    phase = Phase.RED_STRONGER;
                    break;
                case RED_STRONGER:
                    phase = Phase.SHAKING;
                    break;
                case SHAKING:
                    phase = Phase.CHASE;
                    break;
            }
            phaseAge = 0;
            System.out.println(phase);
        }
    }

    @Override
    public void noteOffReceived(MidiNote note) {
    }
}
