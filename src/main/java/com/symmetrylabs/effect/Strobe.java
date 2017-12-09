package com.symmetrylabs.effect;

import heronarts.lx.LX;
import heronarts.lx.LXEffect;
import heronarts.lx.color.LXColor;
import heronarts.lx.modulator.LXWaveshape;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.EnumParameter;
import heronarts.lx.parameter.FunctionalParameter;
import heronarts.lx.parameter.LXParameter;

import java.awt.Color;

import static processing.core.PApplet.lerp;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class Strobe extends LXEffect {

    public enum Waveshape {
        TRI,
        SIN,
        SQUARE,
        UP,
        DOWN
    }

    public final EnumParameter<Waveshape> mode = new EnumParameter<Waveshape>("Shape", Waveshape.TRI);

    public final CompoundParameter frequency = (CompoundParameter)
        new CompoundParameter("Freq", 1, .05, 10).setUnits(LXParameter.Units.HERTZ);

    private final SawLFO basis = new SawLFO(1, 0, new FunctionalParameter() {
        public double getValue() {
            return 1000 / frequency.getValue();
        }
    });

    public Strobe(LX lx) {
        super(lx);
        addParameter(mode);
        addParameter(frequency);
        startModulator(basis);
    }

    @Override
    protected void onEnable() {
        basis.setBasis(0).start();
    }

    private LXWaveshape getWaveshape() {
        switch (this.mode.getEnum()) {
            case SIN:
                return LXWaveshape.SIN;
            case TRI:
                return LXWaveshape.TRI;
            case UP:
                return LXWaveshape.UP;
            case DOWN:
                return LXWaveshape.DOWN;
            case SQUARE:
                return LXWaveshape.SQUARE;
        }
        return LXWaveshape.SIN;
    }

    private final float[] hsb = new float[3];

    @Override
    public void run(double deltaMs, double amount) {
        float amt = this.enabledDamped.getValuef();
        if (amt > 0) {
            float strobef = basis.getValuef();
            strobef = (float) getWaveshape().compute(strobef);
            strobef = lerp(1, strobef, amt);
            if (strobef < 1) {
                if (strobef == 0) {
                    for (int i = 0; i < colors.length; ++i) {
                        colors[i] = LXColor.BLACK;
                    }
                } else {
                    for (int i = 0; i < colors.length; ++i) {
                        LXColor.RGBtoHSB(colors[i], hsb);
                        hsb[2] *= strobef;
                        colors[i] = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
                    }
                }
            }
        }
    }
}
