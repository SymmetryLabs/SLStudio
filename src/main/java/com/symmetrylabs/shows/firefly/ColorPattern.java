package com.symmetrylabs.shows.firefly;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;

import java.util.concurrent.ThreadLocalRandom;

abstract public class ColorPattern extends FPSPattern {

    public DiscreteParameter paletteKnob = new DiscreteParameter("palette", 0, 0, LUColors.ALL_PALETTES.length);
    public final BooleanParameter randomPaletteKnob =
        new BooleanParameter("RandPlt", false);
    public CompoundParameter saturation = new CompoundParameter("sat", 1.0f, 0.0, 1.0);
    public CompoundParameter bright = new CompoundParameter("bright", 1.0, 0.0, 1.0);
    public CompoundParameter hue = new CompoundParameter("hue", 0f, 0f, 1.0f);
    public int[] palette;
    public int randomPalette = 0;

    public ColorPattern(LX lx) {
        super(lx);
    }

    public int getNewRGB() {
        return getNewRGB(-1);
    }

    public int getNewRGB(int whichIndex) {
        float[] hsb = {0f, 0f, 0f};
        getNewHSB(hsb, whichIndex);
        int color =  LXColor.hsb(360f * hsb[0], 100f * hsb[1], 100f * hsb[2]);
        return color;
    }

    public void getNewHSB(float[] hsb) {
        getNewHSB(hsb, -1);
    }

    public void getNewHSB(float[] hsb, int whichIndex) {
        int whichPalette = paletteKnob.getValuei();
        if (randomPaletteKnob.getValueb())
            whichPalette = randomPalette;

        if (whichPalette == 0) {
            hsb[0] = (float) Math.random();
            hsb[1] = saturation.getValuef();
            hsb[2] = bright.getValuef();
        } else if (whichPalette == 1) {
            hsb[0] = hue.getValuef();
            hsb[1] = saturation.getValuef();
            hsb[2] = bright.getValuef();
        } else {
            int[] palette = LUColors.ALL_PALETTES[whichPalette - 2];
            int index = whichIndex;
            if (index == -1)
                index = ThreadLocalRandom.current().nextInt(0, palette.length);
            else if (index >= palette.length)
                index -= palette.length;
            int color = palette[index];
            LUColors.RGBtoHSB(color, hsb);
        }
    }

    @Override
    public void onActive()
    {
        super.onActive();
        if (randomPaletteKnob.getValueb()) {
            int paletteNumber = ThreadLocalRandom.current().nextInt(0, LUColors.ALL_PALETTES.length);
            palette = LUColors.ALL_PALETTES[paletteNumber];
            randomPalette = paletteNumber;
        } else {
            if (paletteKnob.getValuei() >= 2) {
                palette = LUColors.ALL_PALETTES[paletteKnob.getValuei() - 2];
            } else {
                // Palette 0 is random hue. Palette 1 is manual hue. For those, just assign 0th palette to
                // palette.  It shouldn't be used but this is less likely to crash.
                palette = LUColors.ALL_PALETTES[0];
            }
        }
    }
}
