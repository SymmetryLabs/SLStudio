package com.symmetrylabs.slstudio.effect;

import heronarts.lx.LX;
import heronarts.lx.LXEffect;
import heronarts.lx.model.LXPoint;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;

import com.symmetrylabs.util.ColorUtils;
import heronarts.lx.transform.LXVector;

public class LCHShift extends LXEffect {
    public final CompoundParameter Lboost = new CompoundParameter("Lboost", 1, 0, 4);
    public final CompoundParameter Lshift = new CompoundParameter("Lshift", 0, -100, 100);
    public final CompoundParameter Cboost = new CompoundParameter("Cboost", 1, 0, 4);
    public final CompoundParameter Cshift = new CompoundParameter("Cshift", 0, -100, 100);
    public final CompoundParameter Hshift = new CompoundParameter("Hshift", 0, -180, 180);

    public LCHShift(LX lx) {
        super(lx);

        addParameter(Lboost);
        addParameter(Lshift);
        addParameter(Cboost);
        addParameter(Cshift);
        addParameter(Hshift);
    }

    private final float[] xyz = new float[3];
    private final float[] luv = new float[3];
    private final float[] lch = new float[3];
    private final float[] rgb = new float[3];

    @Override
    public void run(double deltaMs, double amount) {
        for (LXVector v : getVectorList()) {
            int c = colors[v.index];
            rgb[0] = ((c & LXColor.RED_MASK) >>> LXColor.RED_SHIFT) / 255f;
            rgb[1] = ((c & LXColor.GREEN_MASK) >>> LXColor.GREEN_SHIFT) / 255f;
            rgb[2] = (c & LXColor.BLUE_MASK) / 255f;

            ColorUtils.rgb2xyz(rgb, xyz);
            ColorUtils.xyz2luv(xyz, luv);
            ColorUtils.luv2lch(luv, lch);

            lch[0] = Math.max(0, Math.min(100, lch[0] * Lboost.getValuef() + Lshift.getValuef()));
            lch[1] = Math.max(0, Math.min(140, lch[1] * Cboost.getValuef() + Cshift.getValuef()));
            lch[2] = (lch[2] + Hshift.getValuef() + 360) % 360;

            ColorUtils.lch2luv(lch, luv);
            ColorUtils.luv2xyz(luv, xyz);
            ColorUtils.xyz2rgb(xyz, rgb);

            colors[v.index] = LXColor.rgb((int)(255 * rgb[0]), (int)(255 * rgb[1]), (int)(255 * rgb[2]));
        }
    }
}
