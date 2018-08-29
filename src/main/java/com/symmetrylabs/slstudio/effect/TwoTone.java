package com.symmetrylabs.slstudio.effect;

import com.symmetrylabs.util.ColorUtils;
import heronarts.lx.LX;
import heronarts.lx.LXEffect;
import heronarts.lx.color.ColorParameter;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;

public class TwoTone extends LXEffect {
    CompoundParameter cutoff1 = new CompoundParameter("Lcut1", 1, 0, 100);
    CompoundParameter cutoff2 = new CompoundParameter("Lcut2", 50, 0, 100);
    ColorParameter c2 = new ColorParameter("c2", LXColor.WHITE);

    public TwoTone(LX lx) {
        super(lx);
        addParameter(cutoff1);
        addParameter(cutoff2);
        addParameter(c2);
    }

    @Override
    public void run(double deltaMs, double amount) {
        final float[] xyz = new float[3];
        final float[] luv = new float[3];
        final float[] rgb = new float[3];

        for (int i = 0; i < colors.length; i++) {
            int c = colors[i];
            rgb[0] = ((c & LXColor.RED_MASK) >>> LXColor.RED_SHIFT) / 255f;
            rgb[1] = ((c & LXColor.GREEN_MASK) >>> LXColor.GREEN_SHIFT) / 255f;
            rgb[2] = (c & LXColor.BLUE_MASK) / 255f;

            ColorUtils.rgb2xyz(rgb, xyz);
            ColorUtils.xyz2luv(xyz, luv);

            if (luv[0] < cutoff1.getValue()) {
                colors[i] = 0;
            } else if (luv[0] < cutoff2.getValue()) {
                colors[i] = palette.getColor();
            } else {
                colors[i] = c2.getColor();
            }
        }
    }
}
