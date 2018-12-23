package com.symmetrylabs.slstudio.pattern;

import com.symmetrylabs.color.Ops8;
import com.symmetrylabs.util.MathUtils;
import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.PolyBuffer;
import heronarts.lx.audio.GraphicMeter;
import heronarts.lx.audio.LXAudioInput;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.LinearEnvelope;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.transform.LXVector;

import java.util.ArrayList;
import java.util.List;

import static heronarts.lx.PolyBuffer.Space.SRGB8;

public class ViolinWave extends LXPattern {

    private LXAudioInput audioInput = lx.engine.audio.getInput();
    private GraphicMeter eq = new GraphicMeter(audioInput);

    CompoundParameter level = new CompoundParameter("Level", 0.45);
    CompoundParameter range = new CompoundParameter("Range", 0.5);
    CompoundParameter edge = new CompoundParameter("Egde", 0.5);
    CompoundParameter release = new CompoundParameter("Rls", 0.5);
    CompoundParameter speed = new CompoundParameter("Speed", 0.5);
    CompoundParameter amp = new CompoundParameter("Amp", 0.25, 0, 5);
    CompoundParameter period = new CompoundParameter("Wave", 0.5);
    CompoundParameter pSize = new CompoundParameter("pSize", 0.5, 0, 1.08);
    CompoundParameter pSpeed = new CompoundParameter("pSpeed", 0.5);
    CompoundParameter pDensity = new CompoundParameter("pDens", 0.25);

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
            float time = 3000 - 2500 * pSpeed.getValuef();
            x.setRange(xInit, xInit + MathUtils.random(-40, 40), time).trigger();
            y.setRange(model.cy + 10, direction ? model.yMax + 50 : model.yMin - 50, time).trigger();
            return this;
        }

        boolean isActive() {
            return x.isRunning() || y.isRunning();
        }

        public void run(double deltaMs, int[] colors) {
            if (!isActive()) return;
            final float pFalloff = (30 - 27 * pSize.getValuef());

            getVectorList().parallelStream().forEach(p -> {
                float b = 100 - pFalloff * (MathUtils.abs(p.x - x.getValuef()) + MathUtils.abs(p.y - y.getValuef()));
                if (b > 0) {
                    colors[p.index] = Ops8.add(colors[p.index],
                        LXColor.hsb(palette.getHuef(), 20, b));
                }
            });
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

    public void run(double deltaMs, PolyBuffer.Space space) {
        int[] colors = (int[]) getArray(SRGB8);

        accum += deltaMs / (1000. - 900. * speed.getValuef());
        for (int i = 0; i < centers.length; ++i) {
            centers[i] =
                model.cy + 30 * amp.getValuef() * MathUtils.sin((float)(accum + (i - centers.length / 2f) / (1f + 9 * period.getValuef())));
        }
        float zeroDBReference = MathUtils.pow(10, (50 - 190 * level.getValuef()) / 20f);
        float dB = (float) (20 * Math.log((eq.getSquaref()) / zeroDBReference) / LOG_10);
        if (dB > dbValue.getValuef()) {
            rising = true;
            dbValue.setRangeFromHereTo(dB, 10).trigger();
        } else {
            if (rising) {
                for (int j = 0; j < pDensity.getValuef() * 3; ++j) {
                    fireParticle(true);
                    fireParticle(false);
                }
            }
            rising = false;
            dbValue.setRangeFromHereTo(MathUtils.max(dB, -96), 50 + 1000 * release.getValuef()).trigger();
        }
        float edg = 1 + edge.getValuef() * 40;
        float rng = (78 - 64 * range.getValuef()) / (model.yMax - model.cy);
        float val = MathUtils.max(2, dbValue.getValuef());

        for (LXVector v : getVectors()) {
            int ci = (int)MathUtils.lerp(0, centers.length - 1, (v.point.x - model.xMin) / (model.xMax - model.xMin));
            float rFactor = 1.0f - 0.9f * MathUtils.abs(v.x - model.cx) / (model.xMax - model.cx);
            colors[v.index] = LXColor.hsb(
                palette.getHuef() + MathUtils.abs(v.x - model.cx),
                MathUtils.min(100, 20 + 8 * MathUtils.abs(v.y - centers[ci])),
                MathUtils.constrain(edg * (val * rFactor - rng * MathUtils.abs(v.y - centers[ci])), 0, 100)
            );
        }

        for (Particle p : particles) {
            p.run(deltaMs, colors);
        }
        markModified(SRGB8);
    }
}
