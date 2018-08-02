package com.symmetrylabs.slstudio.effect;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.transform.LXVector;

import static com.symmetrylabs.util.NoiseUtils.noise;


public class LSD extends SLModelEffect {

    public final BoundedParameter scale = new BoundedParameter("Scale", 10, 5, 40);
    public final BoundedParameter speed = new BoundedParameter("Speed", 4, 1f, 6);
    public final BoundedParameter range = new BoundedParameter("Range", 1, .7, 2);

    public LSD(LX lx) {
        super(lx);
        addParameter(scale);
        addParameter(speed);
        addParameter(range);
        this.enabledDampingAttack.setValue(500);
        this.enabledDampingRelease.setValue(500);
    }

    private float accum = 0;
    private int equalCount = 0;
    private float sign = 1;

    @Override
    public void run(final double deltaMs, final double amount) {
        float newAccum = (float) (accum + sign * deltaMs * speed.getValuef() / 4000.);
        if (newAccum == accum) {
            if (++equalCount >= 5) {
                equalCount = 0;
                sign = -sign;
                newAccum = accum + sign * .01f;
            }
        }
        accum = newAccum;
        final float sf = scale.getValuef() / 1000f;
        final float rf = range.getValuef();

        model.forEachPoint((start, end) -> {
            final float[] hsb = new float[3];
            for (LXVector v : getVectors(start, end)) {
                LXColor.RGBtoHSB(colors[v.index], hsb);
                float h = rf * noise(sf * v.x, sf * v.y, sf * v.z + accum);
                int c2 = LX.hsb(h * 360, 100, hsb[2] * 100);
                if (amount < 1) {
                    colors[v.index] = LXColor.lerp(colors[v.index], c2, amount);
                } else {
                    colors[v.index] = c2;
                }
            }
        });
    }
}
