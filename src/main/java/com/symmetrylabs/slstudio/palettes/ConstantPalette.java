package com.symmetrylabs.slstudio.palettes;

/**
 * A palette that returns a constant color.
 */
class ConstantPalette implements ColorPalette {
    final int c;

    ConstantPalette(int c) {
        this.c = 0xff000000 | c;
    }

    public int getColor(double p) {
        return c;
    }
}
