package com.symmetrylabs.slstudio.effect;

import heronarts.lx.LX;
import heronarts.lx.LXEffect;
import heronarts.lx.color.LXColor;
import heronarts.lx.color.ColorParameter;
import heronarts.lx.model.LXModel;
import heronarts.lx.modulator.DampedParameter;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.EnumParameter;
import heronarts.lx.parameter.FunctionalParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.transform.LXVector;

/**
 * ColorGradient — overrides the hue and saturation of every pixel on the
 * pattern it is added to, preserving per-pixel brightness.
 *
 * Has the same parameters as the global LXPalette (hue mode, color, spread,
 * offsets, mirror, period, range) but is entirely self-contained and only
 * affects the single pattern it is attached to.
 */
public class ColorGradient extends LXEffect {

    public enum Mode { FIXED, OSCILLATE, CYCLE }

    public final EnumParameter<Mode> hueMode =
        new EnumParameter<Mode>("Mode", Mode.FIXED)
            .setDescription("Hue animation mode: Fixed, Oscillate, or Cycle");

    public final ColorParameter color =
        new ColorParameter("Color", 0xffff0000)
            .setDescription("Base color for the override");

    public final CompoundParameter range =
        new CompoundParameter("Range", 0, 360)
            .setDescription("Hue range for oscillation");

    public final CompoundParameter period = (CompoundParameter)
        new CompoundParameter("Period", 120000, 1000, (120000/2)/2)
            .setDescription("Period of hue oscillation or cycle in ms")
            .setUnits(LXParameter.Units.MILLISECONDS);

    public final CompoundParameter spread = (CompoundParameter)
        new CompoundParameter("Spread", 0, -360, 360)
            .setDescription("Amount of hue spread across axes")
            .setPolarity(LXParameter.Polarity.BIPOLAR);

    public final CompoundParameter spreadX = (CompoundParameter)
        new CompoundParameter("XSprd", 0, -1, 1)
            .setDescription("Hue spread on the X axis")
            .setPolarity(LXParameter.Polarity.BIPOLAR);

    public final CompoundParameter spreadY = (CompoundParameter)
        new CompoundParameter("YSprd", 0, -1, 1)
            .setDescription("Hue spread on the Y axis")
            .setPolarity(LXParameter.Polarity.BIPOLAR);

    public final CompoundParameter spreadZ = (CompoundParameter)
        new CompoundParameter("ZSprd", 0, -1, 1)
            .setDescription("Hue spread on the Z axis")
            .setPolarity(LXParameter.Polarity.BIPOLAR);

    public final CompoundParameter spreadR = (CompoundParameter)
        new CompoundParameter("RSprd", 0, -1, 1)
            .setDescription("Hue spread by radius from center")
            .setPolarity(LXParameter.Polarity.BIPOLAR);

    public final CompoundParameter offsetX = (CompoundParameter)
        new CompoundParameter("XOffs", 0, -1, 1)
            .setDescription("Spread origin offset on X axis")
            .setPolarity(LXParameter.Polarity.BIPOLAR);

    public final CompoundParameter offsetY = (CompoundParameter)
        new CompoundParameter("YOffs", 0, -1, 1)
            .setDescription("Spread origin offset on Y axis")
            .setPolarity(LXParameter.Polarity.BIPOLAR);

    public final CompoundParameter offsetZ = (CompoundParameter)
        new CompoundParameter("ZOffs", 0, -1, 1)
            .setDescription("Spread origin offset on Z axis")
            .setPolarity(LXParameter.Polarity.BIPOLAR);

    public final BooleanParameter mirror =
        new BooleanParameter("Mirror", true)
            .setDescription("Mirror hue spread from center");

    // Internal modulators — same as LXPalette
    private final DampedParameter hueFixed = new DampedParameter(this.color.hue, 1800).setModulus(360.);
    private final SawLFO hueCycle = new SawLFO(0, 360, period);
    private final FunctionalParameter hue2 = new FunctionalParameter() {
        @Override public double getValue() { return color.hue.getValue() + range.getValue(); }
    };
    private final SinLFO hueOscillate = new SinLFO(color.hue, hue2, period);

    private LXModulator hueModulator;

    private double xMult, yMult, zMult, rMult;

    public ColorGradient(LX lx) {
        super(lx);
        this.hueModulator = this.hueFixed;

        addParameter("hueMode", this.hueMode);
        addParameter("color", this.color);
        addParameter("range", this.range);
        addParameter("period", this.period);
        addParameter("spread", this.spread);
        addParameter("spreadX", this.spreadX);
        addParameter("spreadY", this.spreadY);
        addParameter("spreadZ", this.spreadZ);
        addParameter("spreadR", this.spreadR);
        addParameter("offsetX", this.offsetX);
        addParameter("offsetY", this.offsetY);
        addParameter("offsetZ", this.offsetZ);
        addParameter("mirror", this.mirror);

        addModulator(this.hueFixed).start();
        addModulator(this.hueCycle);
        addModulator(this.hueOscillate);

        computeMults(lx.model);
        lx.addListener(new LX.Listener() {
            @Override public void modelChanged(LX lx, LXModel model) { computeMults(model); }
        });

        this.hueMode.setOptions(new String[] { "Fixed", "Oscillate", "Cycle" });
    }

    private void computeMults(LXModel model) {
        this.xMult = (model.xRange == 0) ? 1 : (1.0 / model.xRange);
        this.yMult = (model.yRange == 0) ? 1 : (1.0 / model.yRange);
        this.zMult = (model.zRange == 0) ? 1 : (1.0 / model.zRange);
        this.rMult = (model.rRange == 0) ? 1 : (1.0 / model.rRange);
    }

    @Override
    public void onParameterChanged(LXParameter p) {
        super.onParameterChanged(p);
        if (p == this.hueMode) {
            double hueValue = this.hueModulator.getValue();
            this.color.hue.setValue(hueValue);
            switch (this.hueMode.getEnum()) {
                case FIXED:
                    this.hueModulator = this.hueFixed;
                    this.hueFixed.setValue(hueValue).start();
                    this.hueCycle.stop();
                    this.hueOscillate.stop();
                    break;
                case CYCLE:
                    this.hueModulator = this.hueCycle;
                    this.hueFixed.stop();
                    this.hueOscillate.stop();
                    this.hueCycle.setValue(hueValue).start();
                    break;
                case OSCILLATE:
                    this.hueModulator = this.hueOscillate;
                    this.hueFixed.stop();
                    this.hueCycle.stop();
                    this.hueOscillate.setValue(hueValue).start();
                    break;
            }
        }
    }

    private double getHue(float x, float y, float z) {
        LXModel m = lx.model;
        double dx = x - m.cx - this.offsetX.getValue() * m.xRange;
        double dy = y - m.cy - this.offsetY.getValue() * m.yRange;
        double dz = z - m.cz - this.offsetZ.getValue() * m.zRange;
        if (this.mirror.isOn()) {
            dx = Math.abs(dx);
            dy = Math.abs(dy);
            dz = Math.abs(dz);
        }
        double r = Math.sqrt(x * x + y * y + z * z);
        double sp = this.spread.getValue();
        return this.hueModulator.getValue()
            + sp * this.spreadX.getValue() * this.xMult * dx
            + sp * this.spreadY.getValue() * this.yMult * dy
            + sp * this.spreadZ.getValue() * this.zMult * dz
            + sp * this.spreadR.getValue() * this.rMult * (r - m.rMin);
    }

    @Override
    public void run(double deltaMs, double amount) {
        float s = this.color.saturation.getValuef();
        for (LXVector v : getVectors()) {
            float h = (float) getHue(v.x, v.y, v.z);
            float b = LXColor.b(colors[v.index]);
            int overrideColor = lx.hsb(h, s, b);
            colors[v.index] = amount < 1
                ? LXColor.lerp(colors[v.index], overrideColor, (float) amount)
                : overrideColor;
        }
    }
}
