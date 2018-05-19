package com.symmetrylabs.slstudio.palettes;

import com.symmetrylabs.color.Ops16;
import com.symmetrylabs.color.Spaces;

/** A palette that returns a constant color. */
class ConstantPalette implements ColorPalette {
    final long rgb16;

    ConstantPalette(long rgb16) {
        this.rgb16 = Ops16.rgba(Ops16.red(rgb16), Ops16.green(rgb16), Ops16.blue(rgb16), 0xffff);
    }

    ConstantPalette(int rgb8) {
        this(Spaces.rgb8ToRgb16(rgb8));
    }

    public int getColor(double p) {
        return Spaces.rgb16ToRgb8(rgb16);
    }

    public long getColor16(double p) {
        return rgb16;
    }
}
