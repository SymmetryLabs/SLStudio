package com.symmetrylabs.effect;

import com.symmetrylabs.p3lx.LXStudio;
import com.symmetrylabs.palettes.ZigzagPalette;
import heronarts.lx.LX;
import heronarts.lx.LXEffect;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class PaletteEffect extends LXEffect {
    CompoundParameter amount = new CompoundParameter("amount", 0, 0, 1);
    DiscreteParameter palette = new DiscreteParameter("palette", ((LXStudio) lx).paletteLibrary.getNames());
        // selected colour palette
    CompoundParameter bottom = new CompoundParameter("bottom", 0, 0, 1);  // palette start point (fraction 0 - 1)
    CompoundParameter top = new CompoundParameter("top", 1, 0, 1);  // palette stop point (fraction 0 - 1)
    CompoundParameter bias = new CompoundParameter("bias", 0, -6, 6);  // bias colour palette toward zero (dB)
    CompoundParameter shift = new CompoundParameter("shift", 0, -1, 1);  // shift in colour palette (fraction 0 - 1)
    CompoundParameter cutoff = new CompoundParameter("cutoff", 0, 0, 1);  // palette value cutoff (fraction 0 - 1)

    ZigzagPalette pal = new ZigzagPalette();

    public PaletteEffect(LX lx) {
        super(lx);
        addParameter(amount);
        addParameter(palette);
        addParameter(bottom);
        addParameter(top);
        addParameter(shift);
        addParameter(bias);
        addParameter(cutoff);
    }

    @Override
    public void run(double deltaMs, double amount) {
        if (palette.getOptions().length == 0 && ((LXStudio) lx).paletteLibrary != null) {
            palette.setOptions(((LXStudio) lx).paletteLibrary.getNames());
        }

        double amt = this.amount.getValue();
        if (amt == 0) return;

        pal.setPalette(((LXStudio) lx).paletteLibrary.get(palette.getOption()));
        pal.setBottom(bottom.getValue());
        pal.setTop(top.getValue());
        pal.setBias(bias.getValue());
        pal.setShift(shift.getValue());
        pal.setCutoff(cutoff.getValue());

        for (int i = 0; i < colors.length; i++) {
            int c = colors[i];
            int r = (c >> 16) & 0xff;
            int g = (c >> 8) & 0xff;
            int b = c & 0xff;
            int target = pal.getColor(r * 0.2126 / 255 + g * 0.7152 / 255 + b * 0.0722 / 255);
            colors[i] = (amt == 1) ? target : LXColor.lerp(colors[i], target, amt);
        }
    }
}
