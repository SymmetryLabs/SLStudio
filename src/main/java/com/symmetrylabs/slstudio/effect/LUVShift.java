package com.symmetrylabs.slstudio.effect;

import heronarts.lx.transform.LXVector;
import org.apache.commons.math3.util.FastMath;

import heronarts.lx.LX;
import heronarts.lx.LXEffect;
import heronarts.lx.model.LXPoint;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;

import com.symmetrylabs.util.ColorUtils;

public class LUVShift extends LXEffect {
    public final CompoundParameter Lboost = new CompoundParameter("Lboost", 1, 0, 4);
    public final CompoundParameter Lshift = new CompoundParameter("Lshift", 0, -100, 100);
    public final CompoundParameter Ushift = new CompoundParameter("Ushift", 0, -100, 100);
    public final CompoundParameter Vshift = new CompoundParameter("Vshift", 0, -100, 100);

    public LUVShift(LX lx) {
        super(lx);

        addParameter(Lboost);
        addParameter(Lshift);
        addParameter(Ushift);
        addParameter(Vshift);
    }

    private final float[] xyz = new float[3];
    private final float[] luv = new float[3];
    private final float[] rgb = new float[3];

    @Override
    public void run(double deltaMs, double amount) {
        for (LXVector p : getVectorList()) {
            int c = colors[p.index];
            rgb[0] = ((c & LXColor.RED_MASK) >>> LXColor.RED_SHIFT) / 255f;
            rgb[1] = ((c & LXColor.GREEN_MASK) >>> LXColor.GREEN_SHIFT) / 255f;
            rgb[2] = (c & LXColor.BLUE_MASK) / 255f;

            ColorUtils.rgb2xyz(rgb, xyz);
            ColorUtils.xyz2luv(xyz, luv);

            luv[0] = Math.max(0, Math.min(100, luv[0] * Lboost.getValuef() + Lshift.getValuef()));
            luv[1] = Math.max(-150, Math.min(150, luv[1] + Ushift.getValuef()));
            luv[2] = Math.max(-150, Math.min(150, luv[2] + Vshift.getValuef()));

            ColorUtils.luv2xyz(luv, xyz);
            ColorUtils.xyz2rgb(xyz, rgb);

            colors[p.index] = LXColor.rgb((int)(255 * rgb[0]), (int)(255 * rgb[1]), (int)(255 * rgb[2]));
        }
    }
}
