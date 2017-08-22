package com.symmetrylabs.patterns;

import java.util.List;
import java.util.ArrayList;

import heronarts.lx.LX;
import heronarts.lx.audio.*;
import heronarts.lx.parameter.*;
import heronarts.lx.modulator.*;
import heronarts.lx.model.LXPoint;
import heronarts.lx.color.LXColor;

import com.symmetrylabs.util.MathUtils;

public class ViolinWave extends CubesPattern {

    private LXAudioInput audioInput = lx.engine.audio.getInput();
    private GraphicMeter eq = new GraphicMeter(audioInput);

    CompoundParameter level = new CompoundParameter("LVL", 0.45);
    CompoundParameter range = new CompoundParameter("RNG", 0.5);
    CompoundParameter edge = new CompoundParameter("EDG", 0.5);
    CompoundParameter release = new CompoundParameter("RLS", 0.5);
    CompoundParameter speed = new CompoundParameter("SPD", 0.5);
    CompoundParameter amp = new CompoundParameter("AMP", 0.25, 0, 3);
    CompoundParameter period = new CompoundParameter("WAVE", 0.5);
    CompoundParameter pSize = new CompoundParameter("PSIZE", 0.5);
    CompoundParameter pSpeed = new CompoundParameter("PSPD", 0.5);
    CompoundParameter pDensity = new CompoundParameter("PDENS", 0.25);

    LinearEnvelope dbValue = new LinearEnvelope(0, 0, 10);

    public ViolinWave(LX lx) {
        super(lx);
        addParameter(level);
        addParameter(edge);
        addParameter(range);
        addParameter(release);
        addParameter(speed);
        addParameter(amp);
        addParameter(period);
        addParameter(pSize);
        addParameter(pSpeed);
        addParameter(pDensity);
        addModulator(dbValue);
        addModulator(eq).start();
    }

    final List<Particle> particles = new ArrayList<Particle>();

    class Particle {

        LinearEnvelope x = new LinearEnvelope(0, 0, 0);
        LinearEnvelope y = new LinearEnvelope(0, 0, 0);

        Particle() {
            addModulator(x);
            addModulator(y);
        }

        Particle trigger(boolean direction) {
            float xInit = MathUtils.random(model.xMin, model.xMax);
            float time = 3000 - 2500*pSpeed.getValuef();
            x.setRange(xInit, xInit + MathUtils.random(-40, 40), time).trigger();
            y.setRange(model.cy + 10, direction ? model.yMax + 50 : model.yMin - 50, time).trigger();
            return this;
        }

        boolean isActive() {
            return x.isRunning() || y.isRunning();
        }

        public void run(double deltaMs) {
            if (!isActive()) {
                return;
            }

            float pFalloff = (30 - 27*pSize.getValuef());
            for (LXPoint p : model.points) {
                float b = 100 - pFalloff * (Math.abs(p.x - x.getValuef()) + Math.abs(p.y - y.getValuef()));
                if (b > 0) {
                    blendColor(p.index, lx.hsb(
                        palette.getHuef(), 20, b
                    ), LXColor.Blend.ADD);
                }
            }
        }
    }

    float[] centers = new float[30];
    double accum = 0;
    boolean rising = true;

    void fireParticle(boolean direction) {
        boolean gotOne = false;
        for (Particle p : particles) {
            if (!p.isActive()) {
             p.trigger(direction);
             return;
            }
        }
        particles.add(new Particle().trigger(direction));
    }

    final double LOG_10 = Math.log(10);

    public void run(double deltaMs) {
        accum += deltaMs / (1000. - 900.*speed.getValuef());
        for (int i = 0; i < centers.length; ++i) {
            centers[i] = model.cy + 30 * amp.getValuef() * (float)Math.sin(accum + (i-centers.length/2.)/(1. + 9.*period.getValuef()));
        }
        float zeroDBReference = (float)Math.pow(10, (50 - 190*level.getValuef())/20.);
        float dB = (float) (20*Math.log((eq.getSquaref()) / zeroDBReference) / LOG_10);
        if (dB > dbValue.getValuef()) {
            rising = true;
            dbValue.setRangeFromHereTo(dB, 10).trigger();
        } else {
            if (rising) {
                for (int j = 0; j < pDensity.getValuef()*3; ++j) {
                    fireParticle(true);
                    fireParticle(false);
                }
            }
            rising = false;
            dbValue.setRangeFromHereTo(Math.max(dB, -96), 50 + 1000*release.getValuef()).trigger();
        }
        float edg = 1 + edge.getValuef() * 40;
        float rng = (78 - 64 * range.getValuef()) / (model.yMax - model.cy);
        float val = Math.max(2, dbValue.getValuef());

        for (LXPoint p : model.points) {
            int ci = (int)MathUtils.lerp(0, centers.length - 1, (p.x - model.xMin) / (model.xMax - model.xMin));
            float rFactor = 1.0f -  0.9f * (float)Math.abs(p.x - model.cx) / (model.xMax - model.cx);
            colors[p.index] = lx.hsb(
                palette.getHuef() + (float)Math.abs(p.x - model.cx),
                Math.min(100, 20 + 8 * (float)Math.abs(p.y - centers[ci])),
                MathUtils.constrain(edg * (val * rFactor - rng * (float)Math.abs(p.y - centers[ci])), 0, 100)
            );
        }

        for (Particle p : particles) {
            p.run(deltaMs);
        }
    }
}
