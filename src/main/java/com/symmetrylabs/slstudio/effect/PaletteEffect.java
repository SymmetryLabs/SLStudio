package com.symmetrylabs.slstudio.effect;

import com.google.gson.JsonObject;
import heronarts.lx.LX;
import heronarts.lx.LXEffect;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;

import com.symmetrylabs.slstudio.palettes.PaletteLibrary;
import com.symmetrylabs.slstudio.palettes.ZigzagPalette;

public class PaletteEffect extends SLEffect {
    private static final String KEY_PALETTE_NAME = "paletteName";

    private final PaletteLibrary paletteLibrary = PaletteLibrary.getInstance();

    CompoundParameter amount = new CompoundParameter("Amount", 0, 0, 1);
    DiscreteParameter palette = new DiscreteParameter("Palette", paletteLibrary.getNames());
        // selected colour palette
    CompoundParameter bottom = new CompoundParameter("Bottom", 0, 0, 1);  // palette start point (fraction 0 - 1)
    CompoundParameter top = new CompoundParameter("Top", 1, 0, 1);  // palette stop point (fraction 0 - 1)
    CompoundParameter bias = new CompoundParameter("Bias", 0, -6, 6);  // bias colour palette toward zero (dB)
    CompoundParameter shift = new CompoundParameter("Shift", 0, -1, 1);  // shift in colour palette (fraction 0 - 1)
    CompoundParameter cutoff = new CompoundParameter("Cutoff", 0, 0, 1);  // palette value cutoff (fraction 0 - 1)
    BooleanParameter alpha = new BooleanParameter("Alpha", false).setDescription("Preserve alpha channel");

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
        addParameter(alpha);
    }

    @Override
    public void run(double deltaMs, double amount) {
        if (palette.getOptions().length == 0) {
            palette.setOptions(paletteLibrary.getNames());
        }

        double amt = this.amount.getValue();
        if (amt == 0) return;

        pal.setPalette(paletteLibrary.get(palette.getOption()));
        pal.setBottom(bottom.getValue());
        pal.setTop(top.getValue());
        pal.setBias(bias.getValue());
        pal.setShift(shift.getValue());
        pal.setCutoff(cutoff.getValue());

        for (int i = 0; i < colors.length; i++) {
            int c = colors[i];
            int a = c & 0xff000000;
            int r = (c >> 16) & 0xff;
            int g = (c >> 8) & 0xff;
            int b = c & 0xff;
            int target = pal.getColor(r * 0.2126 / 255 + g * 0.7152 / 255 + b * 0.0722 / 255);
            colors[i] = (amt == 1) ? target : LXColor.lerp(colors[i], target, amt);
            if (alpha.getValueb()) {
                colors[i] = (colors[i] & 0x00ffffff) | a;
            }
        }
    }

    @Override
    public void save(LX lx, JsonObject obj) {
        super.save(lx, obj);
        obj.addProperty(KEY_PALETTE_NAME, palette.getOption());
    }

    @Override
    public void load(LX lx, JsonObject obj) {
        super.load(lx, obj);
        if (obj.has(KEY_PALETTE_NAME)) {
            String pname = obj.get(KEY_PALETTE_NAME).getAsString();
            String[] palettes = paletteLibrary.getNames();
            for (int i = 0; i < palettes.length; i++) {
                if (palettes[i].equals(pname)) {
                    palette.setValue(i);
                    return;
                }
            }
            System.err.println("couldn't find palette '" + pname + "'");
        }
    }
}
