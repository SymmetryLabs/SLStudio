package com.symmetrylabs.slstudio.effect;

import com.symmetrylabs.slstudio.model.SLModel;
import heronarts.lx.LX;
import heronarts.lx.LXEffect;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BoundedParameter;

import static com.symmetrylabs.slstudio.util.NoiseUtils.noise;


public class LSD extends LXEffect {

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

        ((SLModel) model).forEachPoint((start, end) -> {
            final float[] hsb = new float[3];

            for (int i=start; i<end; i++) {
                LXPoint p = model.points[i];

                LXColor.RGBtoHSB(colors[p.index], hsb);
                float h = rf * noise(sf * p.x, sf * p.y, sf * p.z + accum);
                int c2 = LX.hsb(h * 360, 100, hsb[2] * 100);
                if (amount < 1) {
                    colors[p.index] = LXColor.lerp(colors[p.index], c2, amount);
                } else {
                    colors[p.index] = c2;
                }
            }
        });
    }
}
