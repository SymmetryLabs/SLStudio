package com.symmetrylabs.slstudio.effect;

import heronarts.lx.LX;
import heronarts.lx.LXEffect;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.transform.LXVector;

public class SOS_strobeNoMore extends LXEffect {
    public final CompoundParameter Bscale = new CompoundParameter("Bscale", 1, 0, 4);
    public final CompoundParameter Bshift = new CompoundParameter("Bshift", 0, -100, 100);

    public SOS_strobeNoMore(LX lx) {
        super(lx);
    }

    @Override
    public void run(double deltaMs, double amount) {
        float shift = (float)-10;
        float scale = (float) 0.7;

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
            br = scale * br + shift;
            br = br > 100 ? 100 : br < 0 ? 0 : br;

            colors[p.index] = LXColor.hsba(h, s, br, a / 255.f);
        }
    }
}
