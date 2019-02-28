package com.symmetrylabs.slstudio.effect;

import com.symmetrylabs.util.ColorUtils;
import heronarts.lx.LX;
import heronarts.lx.LXEffect;
import heronarts.lx.color.ColorParameter;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import org.apache.commons.math3.util.FastMath;

import java.util.stream.IntStream;

public class Threshold extends LXEffect {
    CompoundParameter cutR = new CompoundParameter("cutR", 0, 0, 1);
    CompoundParameter cutG = new CompoundParameter("cutG", 0, 0, 1);
    CompoundParameter cutB = new CompoundParameter("cutB", 0, 0, 1);
    BooleanParameter alpha = new BooleanParameter("alpha", false);

    public Threshold(LX lx) {
        super(lx);
        addParameter(cutR);
        addParameter(cutG);
        addParameter(cutB);
        addParameter(alpha);
    }

    @Override
    public void run(double deltaMs, double amount) {
        int cr = (int) (255.f * cutR.getValue());
        int cg = (int) (255.f * cutG.getValue());
        int cb = (int) (255.f * cutB.getValue());
        int off = alpha.getValueb() ? 0 : LXColor.BLACK;
        for (int i = 0; i < colors.length; i++) {
            int c = colors[i];
            boolean on = true;
            on = on && ((c >> 16) & 0xFF) >= cr;
            on = on && ((c >>  8) & 0xFF) >= cg;
            on = on && ((c >>  0) & 0xFF) >= cb;
            if (!on) {
                colors[i] = off;
            }
        }
    }
}
