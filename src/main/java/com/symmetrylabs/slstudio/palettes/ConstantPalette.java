package com.symmetrylabs.slstudio.palettes;

import heronarts.lx.color.LXColor;
import heronarts.lx.color.LXColor16;
import heronarts.lx.color.Spaces;

/**
 * A palette that returns a constant color.
 */
class ConstantPalette implements ColorPalette {
    final long c;

    ConstantPalette(long c) {
        this.c = 0xffff000000000000L | c;
    }

    ConstantPalette(int c) {
        this(Spaces.rgb8ToRgb16(c));
    }

    public int getColor(double p) {
        return Spaces.rgb16ToRgb8(c);
    }

    public long getColor16(double p) {
        return c;
    }
}
