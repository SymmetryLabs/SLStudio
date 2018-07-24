package com.symmetrylabs.slstudio.effect;

import heronarts.lx.transform.LXVector;
import org.apache.commons.math3.util.FastMath;

import heronarts.lx.LX;
import heronarts.lx.LXEffect;
import heronarts.lx.model.LXPoint;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;

import com.symmetrylabs.util.ColorUtils;

public class LABShift extends LXEffect {
    public final CompoundParameter Lboost = new CompoundParameter("Lboost", 1, 0, 4);
    public final CompoundParameter Lshift = new CompoundParameter("Lshift", 0, -100, 100);
    public final CompoundParameter Ashift = new CompoundParameter("Ashift", 0, -100, 100);
    public final CompoundParameter Bshift = new CompoundParameter("Bshift", 0, -100, 100);

    public LABShift(LX lx) {
        super(lx);

        addParameter(Lboost);
        addParameter(Lshift);
        addParameter(Ashift);
        addParameter(Bshift);
    }

    private final float[] xyz = new float[3];
    private final float[] lab = new float[3];
    private final float[] rgb = new float[3];

    @Override
    public void run(double deltaMs, double amount) {
        for (LXVector v : getVectors()) {
            int c = colors[v.index];
            rgb[0] = ((c & LXColor.RED_MASK) >>> LXColor.RED_SHIFT) / 255f;
            rgb[1] = ((c & LXColor.GREEN_MASK) >>> LXColor.GREEN_SHIFT) / 255f;
            rgb[2] = (c & LXColor.BLUE_MASK) / 255f;

            ColorUtils.rgb2xyz(rgb, xyz);
            ColorUtils.xyz2lab(xyz, lab);

            lab[0] = Math.max(0, Math.min(100, lab[0] * Lboost.getValuef() + Lshift.getValuef()));
            lab[1] = Math.max(-110, Math.min(110, lab[1] + Ashift.getValuef()));
            lab[2] = Math.max(-110, Math.min(110, lab[2] + Bshift.getValuef()));

            ColorUtils.lab2xyz(lab, xyz);
            ColorUtils.xyz2rgb(xyz, rgb);

            colors[v.index] = LXColor.rgb((int)(255 * rgb[0]), (int)(255 * rgb[1]), (int)(255 * rgb[2]));
        }
    }
}
