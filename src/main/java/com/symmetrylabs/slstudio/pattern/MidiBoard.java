package com.symmetrylabs.slstudio.pattern;

import com.symmetrylabs.color.Ops8;
import com.symmetrylabs.shows.cubes.CubesModel;
import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.pattern.base.MidiPolyphonicExpressionPattern;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import heronarts.lx.LX;
import heronarts.lx.LXLayer;
import heronarts.lx.LXUtils;
import heronarts.lx.PolyBuffer;
import heronarts.lx.modulator.Accelerator;
import heronarts.lx.modulator.LXRangeModulator;
import heronarts.lx.modulator.LinearEnvelope;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.transform.LXVector;

import static heronarts.lx.PolyBuffer.Space.SRGB8;

public class MidiBoard extends MidiPolyphonicExpressionPattern<CubesModel> {
    protected final Stack<EffectLayer> newLayers = new Stack<>();

    protected final EffectLayerPool<Boom> booms = new EffectLayerPool<Boom>() {
        public Boom create() { return new Boom(); }
    };
    protected final DiscreteParameter boomNoteParam = new DiscreteParameter("BoomNote", 24, 0, 127);
    protected final BooleanParameter boomParam = new BooleanParameter("Boom", false);

    protected final EffectLayerPool<Sparkle> sparkles = new EffectLayerPool<Sparkle>() {
        public Sparkle create() { return new Sparkle(); }
    };
    protected final DiscreteParameter sparkNoteParam = new DiscreteParameter("SparkNote", 26, 0, 127);
    protected final CompoundParameter waveParam = new CompoundParameter("Wave", 0);

    protected final EffectLayerPool<Puff> puffs = new EffectLayerPool<Puff>() {
        public Puff create() { return new Puff(); }
    };
    protected final Puff[] puffsByPitch = new Puff[128];
    protected final DiscreteParameter puffLowParam = new DiscreteParameter("PuffLow", 36, 0, 127);
    protected final DiscreteParameter puffHighParam = new DiscreteParameter("PuffHigh", 72, 0, 127);
    protected final CompoundParameter puffBottomParam = new CompoundParameter("PuffBottom", 0.8);
    protected final CompoundParameter puffTopParam = new CompoundParameter("PuffTop", 0.85);
    protected final CompoundParameter puffSizeParam = new CompoundParameter("PuffSize", 0.55, 0.1, 0.8);
    protected final CompoundParameter puffBendParam = new CompoundParameter("PuffBend", 2, 0, 10);

    protected final CompoundParameter puffStretchParam = new CompoundParameter("PuffStretch", 1, -2, 2);
    protected final CompoundParameter pressLowParam = new CompoundParameter("PressLow", 0.4);
    protected final CompoundParameter pressHighParam = new CompoundParameter("PressHigh", 1);

    public MidiBoard(LX lx) {
        super(lx);
        addParameter(boomNoteParam);
        addParameter(boomParam);
        addParameter(sparkNoteParam);
        addParameter(waveParam);
        addParameter(puffLowParam);
        addParameter(puffHighParam);
        addParameter(puffBottomParam);
        addParameter(puffTopParam);
        addParameter(puffSizeParam);
        addParameter(puffBendParam);
        addParameter(puffStretchParam);
        addParameter(pressLowParam);
        addParameter(pressHighParam);

        boomParam.setMode(BooleanParameter.Mode.MOMENTARY);
        boomParam.addListener(parameter -> {
            if (((BooleanParameter) parameter).isOn()) {
                double velocity = 1.06;
                booms.get().trigger(0.5 + velocity * 0.5, 0.05 + velocity * 0.2);
            }
        });
    }

    public synchronized void run(double deltaMs) {
        int[] colors = (int[]) getArray(SRGB8);
        Arrays.fill(colors, 0);  // effect layers will paint over this
        markModified(SRGB8);

        if (!newLayers.isEmpty()) {
            synchronized(newLayers) {
                while (!newLayers.isEmpty()) {
                    addLayer(newLayers.pop());
                }
            }
        }
    }

    @Override
    public void noteOn(int pitch, double velocity) {
        if (pitch == boomNoteParam.getValuei()) {
            booms.get().trigger(0.5 + velocity * 0.5, 0.05 + velocity * 0.2);
        } else if (pitch == sparkNoteParam.getValuei()) {
            sparkles.get().trigger(velocity*100, pitch == 42);
        } else if (pitch >= puffLowParam.getValuei() && pitch <= puffHighParam.getValuei()) {
            if (puffsByPitch[pitch] != null) {
                puffsByPitch[pitch].off();
            }
            Puff puff = puffs.get();
            puffsByPitch[pitch] = puff;
            float pitchLow = puffLowParam.getValuei();
            float pitchHigh = puffHighParam.getValuei();
            double x = model.xMin + (pitch - pitchLow) / (pitchHigh - pitchLow) * model.xRange;
            puff.on(x, velocityToY(velocity), velocity);

        }
    }

    protected double velocityToY(double velocity) {
        float yBottom = model.yMin + puffBottomParam.getValuef() * model.yRange;
        float yTop = model.yMin + puffTopParam.getValuef() * model.yRange;
        return LXUtils.lerpf(yBottom, yTop, (float) velocity);
    }

    @Override
    public void notePressure(int pitch, double pressure) {
        if (puffsByPitch[pitch] != null) {
            puffsByPitch[pitch].lift(velocityToY(pressure));
            puffsByPitch[pitch].bright(pressLowParam.getValuef() +
                pressure * (pressHighParam.getValuef() - pressLowParam.getValuef()));
        }
    }

    @Override
    public void noteBend(int pitch, double bend) {
        System.out.println(String.format("%d %d bend %.04f", System.currentTimeMillis(), pitch, bend));
        if (puffsByPitch[pitch] != null) {
            if (Math.abs(bend) > 0.002) {
                puffsByPitch[pitch].shift(bend * model.xRange * puffBendParam.getValuef());
            }
        }
    }

    @Override
    public void noteSlide(int pitch, double slide) {
        if (puffsByPitch[pitch] != null) {
            double amount = puffStretchParam.getValuef() * (slide - 0.5) * 2;
            puffsByPitch[pitch].stretch(Math.pow(2, amount));
        }
    }

    @Override
    public void noteOff(int pitch) {
        if (puffsByPitch[pitch] != null) {
            puffsByPitch[pitch].off();
            puffsByPitch[pitch] = null;
        }
    }

    protected abstract class EffectLayer extends LXLayer {
        protected CubesModel model;

        public EffectLayer() {
            super(MidiBoard.this.lx, MidiBoard.this);
            model = MidiBoard.this.model;
        }

        public void run(double deltaMs, PolyBuffer.Space preferredSpace) {
            if (isActive()) {
                int[] colors = (int[]) getArray(SRGB8);
                runEffect(deltaMs/1000, colors);
                markModified(SRGB8);
            }
        }

        public abstract boolean isActive();
        public abstract void runEffect(double deltaSec, int[] colors);
    }

    protected abstract class EffectLayerPool<L extends EffectLayer> {
        List<L> layers = new ArrayList<>();

        public EffectLayerPool() { }

        public abstract L create();

        public L get() {
            for (L layer : layers) {
                if (!layer.isActive()) {
                    return layer;
                }
            }
            L newLayer = create();
            layers.add(newLayer);
            synchronized (newLayers) {
                newLayers.push(newLayer);
            }
            return newLayer;
        }

        public int countActive() {
            int numActive = 0;
            for (EffectLayer layer : layers) {
                if (layer.isActive()) {
                    numActive++;
                }
            }
            return numActive;
        }

        public int count() {
            return layers.size();
        }
    }

    protected class Boom extends EffectLayer {
        final LXRangeModulator position = new LinearEnvelope(0, 1, 1000);
        double intensity = 1;
        double size = 0.1;

        public Boom() {
            super();
            addModulator(position);
        }

        public boolean isActive() {
            return position.isRunning();
        }

        public void trigger(double intensity, double size) {
            this.intensity = intensity;
            this.size = size;
            position.trigger();
        }

        public void runEffect(double deltaSec, int[] colors) {
            float posf = position.getValuef();
            for (LXVector p : getVectors()) {
                double hue = palette.getHuef()
                    + 0.2 * Math.abs(p.x - model.cx)
                    + 0.2 * Math.abs(p.y - model.cy);
                double brt = Math.max(0, intensity - posf - Math.abs(p.y - posf*model.yMax) / (size * model.yRange));
                colors[p.index] = Ops8.add(colors[p.index], lx.hsb((float) hue, 100, (float) (brt * 100)));
            }
        }
    }

    protected class Sparkle extends EffectLayer {
        protected final LinearEnvelope position = new LinearEnvelope(0, 1, 500);
        double intensity = 100;
        boolean direction = true;
        float[] wval = new float[16];
        float wavoff = 0;

        public Sparkle() {
            super();
            addModulator(position).setValue(1);
        }

        public boolean isActive() {
            return position.isRunning();
        }

        public void trigger(double intensity, boolean direction) {
            this.intensity = intensity;
            this.direction = direction;
            position.trigger();
        }

        public void runEffect(double deltaSec, int[] colors) {
            wavoff += deltaSec;
            for (int i = 0; i < wval.length; ++i) {
                wval[i] = model.cy + 0.2f * model.yMax / 2.0f * (float) Math.sin(wavoff + i / 1.9f);
            }
            for (Strip strip : model.getStrips()) {
                int length = strip.points.length;

                float sparklePos = (direction ? position.getValuef()
                    : (1 - position.getValuef()))*length/2f;
                double maxBright = intensity*(1 - position.getValuef());

                int i = 0;
                for (LXVector p : getVectors(strip.points)) {
                    int wavi = (int) LXUtils.constrain(p.x/model.xMax*wval.length, 0, wval.length - 1);
                    float wavb = (float) Math.max(0, waveParam.getValuef()*100. - 8.*Math.abs(p.y -
                        wval[wavi]));

                    double hue = palette.getHuef()
                            + 0.2*Math.abs(p.x - model.cx)
                            + 0.2*Math.abs(p.y - model.cy);
                    double brt = LXUtils.constrain(
                            wavb + Math.max(0, maxBright - 40*Math.abs(sparklePos - Math.abs(i - (length - 1)/2.0))),
                            0, 100);
                    colors[p.index] = Ops8.add(colors[p.index], lx.hsb((float) hue, 100, (float) brt));
                    ++i;
                }
            }
        }
    }

    protected class Puff extends EffectLayer {
        protected final LinearEnvelope brtMod = new LinearEnvelope(0, 0, 0);
        protected final Accelerator yMod = new Accelerator(0, 0, 0);
        protected float xPos;
        protected long lastShiftMillis = -1;
        protected float xShift;
        protected float xScale = 1f;
        protected float yScale = 1f;
        protected float vx = 0;
        protected boolean drifting = true;

        public Puff() {
            super();
            addModulator(brtMod);
            addModulator(yMod);
        }

        public boolean isActive() {
            return brtMod.getValuef() > 0;
        }

        public void on(double x, double y, double intensity) {
            xPos = (float) x;
            lastShiftMillis = -1;
            xShift = 0;
            xScale = 1;
            yScale = 1;
            vx = 0;
            drifting = false;
            yMod.setValue(y).stop();
            brtMod.setRangeFromHereTo(LXUtils.lerpf(60, 100, (float) intensity), 20).start();
        }

        public void off() {
            drifting = true;
            yMod.setVelocity(0).setAcceleration(-380).start();
            brtMod.setRangeFromHereTo(0, 1000).start();
        }

        public void shift(double amount) {
            long now = System.currentTimeMillis();
            long delta = now - lastShiftMillis;
            if (delta > 0 && delta < 100) {
                double newVx = (amount - xShift)/delta;
                double f = Math.abs(newVx) < Math.abs(vx) ? 0.5 : 0.05;
                vx = (float) ((1 - f) * vx + f * newVx);
            }
            xShift = (float) amount;
            lastShiftMillis = now;
            System.out.println(String.format("[%d] %.0f shift = %.1f", now, xPos, xShift));
        }

        public void lift(double y) {
            if (yMod.getValue() < y) {
                yMod.setValue(y).stop();
            }
        }

        public void bright(double amount) {
            brtMod.setValue(amount * 100);
        }

        public void stretch(double factor) {
            xScale = (float) factor;
            yScale = 1/xScale;
        }

        public void runEffect(double deltaSec, int[] colors) {
            float brt = brtMod.getValuef();
            float yVal = yMod.getValuef();
            float cx = xPos + xShift;
            if (drifting) {
                long now = System.currentTimeMillis();
                long delta = now - lastShiftMillis;
                if (delta > 0 && delta < 100) {
                    xShift += vx * (now - lastShiftMillis);
                    lastShiftMillis = now;
                }
            }
            for (LXVector p : getVectors()) {
                double falloff = 6 - 7*puffSizeParam.getValue();
                double b = Math.max(0, brt - falloff * LXUtils.distance(p.x * xScale, p.y * yScale, cx * xScale, yVal * yScale));
                if (b > 0) {
                    float hue = palette.getHuef()
                            + 0.2f * Math.abs(p.x - model.cx)
                          + 0.2f * Math.abs(p.y - model.cy);
                    colors[p.index] = Ops8.add(colors[p.index], lx.hsb(hue, 100, (float) b));
                }
            }
        }
    }

    public String getCaption() {
        return String.format("booms: %d/%d - sparkles: %d/%d - puffs: %d/%d\n",
            booms.countActive(), booms.count(),
            sparkles.countActive(), sparkles.count(),
            puffs.countActive(), puffs.count()
        ) + super.getCaption();
    }
}
