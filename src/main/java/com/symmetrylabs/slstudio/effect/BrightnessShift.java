package com.symmetrylabs.slstudio.effect;

import heronarts.lx.transform.LXVector;
import org.apache.commons.math3.util.FastMath;

import heronarts.lx.LX;
import heronarts.lx.LXEffect;
import heronarts.lx.model.LXPoint;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;

import com.symmetrylabs.util.ColorUtils;

public class BrightnessShift extends LXEffect {
    public final CompoundParameter Bscale = new CompoundParameter("Bscale", 1, 0, 4);
    public final CompoundParameter Bshift = new CompoundParameter("Bshift", 0, -100, 100);
    public final CompoundParameter Bexp = new CompoundParameter("Bexp", 1, 0.01, 10);

    public BrightnessShift(LX lx) {
        super(lx);

        addParameter(Bscale);
        addParameter(Bshift);
        addParameter(Bexp);
    }

    @Override
    public void run(double deltaMs, double amount) {
        float shift = Bshift.getValuef();
        float scale = Bscale.getValuef();
        float exp = Bexp.getValuef();

        for (LXVector p : getVectors()) {
            int rgb = colors[p.index];

            int a = (rgb & LXColor.ALPHA_MASK) >>> LXColor.ALPHA_SHIFT;
            int r = (rgb & LXColor.RED_MASK) >>> LXColor.RED_SHIFT;
            int g = (rgb & LXColor.GREEN_MASK) >>> LXColor.GREEN_SHIFT;
            int b = rgb & LXColor.BLUE_MASK;
            int max = (r > g) ? r : g;
            if (b > max) {
                max = b;
            }
            int min = (r < g) ? r : g;
            if (b < min) {
                min = b;
            }
            float range = max - min;
            float h;
            if (max == 0 || range == 0) {
                h = 0;
            } else {
                float rc = (max - r) / range;
                float gc = (max - g) / range;
                float bc = (max - b) / range;
                if (r == max) {
                    h = bc - gc;
                } else if (g == max) {
                    h = 2.f + rc - bc;
                } else {
                    h = 4.f + gc - rc;
                }
                h /= 6.f;
                if (h < 0) {
                    h += 1.f;
                }
            }
            h *= 360.f;
            float s = (max == 0) ? 0 : (max - min) * 100.f / max;
            float br = 100.f * max / 255.f;
            br = scale * 100.f * (float) Math.pow(br / 100.f, exp) + shift;
            br = br > 100 ? 100 : br < 0 ? 0 : br;

            colors[p.index] = LXColor.hsba(h, s, br, a / 255.f);
        }
    }
}
