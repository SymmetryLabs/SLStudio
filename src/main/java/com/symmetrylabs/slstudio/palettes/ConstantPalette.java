package com.symmetrylabs.slstudio.palettes;

import heronarts.lx.color.LXColor;
import heronarts.lx.color.LXColor16;

/**
 * A palette that returns a constant color.
 */
class ConstantPalette implements ColorPalette {
    final long c;

    ConstantPalette(long c) {
        this.c = 0xffff000000000000L | c;
    }

    ConstantPalette(int c) {
        this(LXColor.toLong(c));
    }

    public int getColor(double p) {
        return LXColor16.toInt(c);
    }

    public long getColor16(double p) {
        return c;
    }
}
