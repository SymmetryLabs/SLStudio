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

import java.util.Random;

public class PilotsSpirits extends SLPattern<SLModel> {
    private CompoundParameter spiritSizeParam = new CompoundParameter("size", 200, 1, 600);
    private CompoundParameter spiritHeartParam = new CompoundParameter("heart", 75, 1, 600);

    private static final float RED_CHASE_DELAY = 400; // ms
    private static final float CHASE_PERIOD = 2700; // ms
    private static final float RED_STRONGER_TIME = 2200; // ms
    private static final float SHAKING_AMP_TIME = 4000; // ms
    private static final float YELLOW_WINS_TIME = 1500; // ms

    SinLFO redRestLfo = new SinLFO(-40, 30, 4500);
    SinLFO yellowRestLfo = new SinLFO(35, -20, 5100);
    float phaseAge = 0;
    float chaseAge = 0;

    enum Phase {
        IDLE,
        MOVE_IN,
        RED_STRONGER,
        SHAKING,
        CHASE,
        YELLOW_WINS,
        OUT,
    }

    Phase phase = Phase.IDLE;
    LXVector redBase, yellowBase;
    Random random = new Random();
    boolean jerking = false;

    public PilotsSpirits(LX lx) {
        super(lx);

        addParameter(spiritSizeParam);
        addParameter(spiritHeartParam);
        addModulator(redRestLfo);
        addModulator(yellowRestLfo);

        redRestLfo.start();
        yellowRestLfo.start();

        resetLocation();
    }

    private void resetLocation() {
        redBase = new LXVector(model.xMin, model.cy, model.cz);
        yellowBase = new LXVector(model.xMax, model.cy, model.cz);
    }

    private LXVector randomVector() {
        /* This doesn't create uniformly distributed vectors on the 2-sphere, but it sure is a lot simpler than the ways that do. */
        LXVector v = new LXVector(
            random.nextFloat() - 0.5f,
            random.nextFloat() - 0.5f,
            random.nextFloat() - 0.5f);
        return v;
    }

    @Override
    public void run(double elapsedMs) {
        phaseAge += elapsedMs;
        chaseAge += elapsedMs;

        int black = LXColor.gray(0);
        for (int i = 0; i < colors.length; i++)
            colors[i] = black;

        if (phase == Phase.OUT) {
            if (phaseAge < 30) {
                int white = LXColor.gray(100);
                for (int i = 0; i < colors.length; i++)
                    colors[i] = white;
            }
            return;
        }

        /* Update positions */
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
            case YELLOW_WINS:
            case CHASE: {
                yellowBase.x = model.cx + model.xRange * (float) Math.sin(chaseAge / CHASE_PERIOD * 2 * Math.PI) / 2;
                if (chaseAge > RED_CHASE_DELAY)
                    redBase.x = model.cx + model.xRange * (float) Math.sin((chaseAge - RED_CHASE_DELAY) / CHASE_PERIOD * 2 * Math.PI) / 2;
                break;
            }
        }

        /* Add idle animations */
        LXVector yLoc = new LXVector(
            -yellowRestLfo.getValuef() / 1.5f,
            yellowRestLfo.getValuef() / 2,
            -yellowRestLfo.getValuef());
        yLoc.add(yellowBase);

        LXVector rLoc = new LXVector(-redRestLfo.getValuef() / 3, redRestLfo.getValuef(), -redRestLfo.getValuef() / 7);
        rLoc.add(redBase);

        float areaEffect = spiritSizeParam.getValuef();
        float heartSize = spiritHeartParam.getValuef();

        /* Change colors and sizes */
        float rHue = 0;
        float yHue = 51;
        float rAlpha = 1;
        float yAlpha = 1;

        if (phase == Phase.RED_STRONGER) {
            yAlpha = (float) Math.max(1.0 - phaseAge / RED_STRONGER_TIME, 0.0);
            float sizeScale = 1f + Math.min(phaseAge / RED_STRONGER_TIME, 1f);
            heartSize *= sizeScale;
            areaEffect *= sizeScale;
        } else if (phase == Phase.SHAKING) {
            yAlpha = 0;
            heartSize *= 2;
            areaEffect *= 2;
            rLoc.add(randomVector().mult(80 * Math.min(phaseAge / SHAKING_AMP_TIME, 1)));
        }
        if (jerking) {
            rLoc.add(randomVector().mult(80));
        }

        /* Apply colors */
        if (phase == Phase.YELLOW_WINS) {
            int bg = LXColor.hsb(yHue, 100, 100 * Math.min(phaseAge / YELLOW_WINS_TIME, 1f));
            for (int i = 0; i < colors.length; i++)
                colors[i] = bg;
            if (phaseAge > YELLOW_WINS_TIME) {
                float sizeScale = 1f - Math.min(1f, (phaseAge - YELLOW_WINS_TIME) / YELLOW_WINS_TIME);
                heartSize *= sizeScale;
                areaEffect *= sizeScale;
            }
        }

        float rHeartSize = heartSize;
        float yHeartSize = heartSize;
        if (random.nextFloat() < 0.015) {
            rHeartSize *= 1.2;
        }
        if (random.nextFloat() < 0.015) {
            yHeartSize *= 1.2;
        }

        if (phase == Phase.CHASE && phaseAge < 50) {
            int white = LXColor.gray(100);
            for (int i = 0; i < colors.length; i++)
                colors[i] = white;
        } else {
            for (LXVector v : getVectors()) {
                double rDist = rLoc.dist(v) - rHeartSize;
                double yDist = yLoc.dist(v) - yHeartSize;

                int rColor = rDist < 0
                    ? LXColor.hsba(rHue, 100 * (1 + rDist / rHeartSize), 100, rAlpha)
                    : LXColor.hsba(rHue, 100, 100 * Math.max(0, 1 - rDist / areaEffect), rAlpha);

                int yColor = yDist < 0
                    ? LXColor.hsba(yHue, 100 * (1 + yDist / yHeartSize), 85, yAlpha)
                    : LXColor.hsba(yHue, 100, 85 * Math.max(0, 1 - yDist / areaEffect), yAlpha);
                int color = Ops8.screen(rColor, yColor);
                colors[v.index] = Ops8.add(color, colors[v.index]);
            }
        }
    }

    @Override
    public void noteOnReceived(MidiNoteOn note) {
        if (note.getPitch() == 60) {
            switch (phase) {
                case IDLE:
                    phase = Phase.MOVE_IN;
                    break;
                case MOVE_IN:
                    phase = Phase.RED_STRONGER;
                    break;
                case RED_STRONGER:
                    phase = Phase.SHAKING;
                    break;
                case SHAKING:
                    phase = Phase.CHASE;
                    chaseAge = 0;
                    break;
                case CHASE:
                    phase = Phase.YELLOW_WINS;
                    break;
                case YELLOW_WINS:
                    phase = Phase.OUT;
                    break;
                case OUT:
                    resetLocation();
                    phase = Phase.IDLE;
                    break;
            }
            phaseAge = 0;
            System.out.println(phase);
        }
        if (note.getPitch() == 62)
            jerking = true;
    }

    @Override
    public void noteOffReceived(MidiNote note) {
        if (note.getPitch() == 62)
            jerking = false;
    }
}
