package com.symmetrylabs.slstudio.palettes;

import com.symmetrylabs.color.Ops16;
import com.symmetrylabs.color.Spaces;
import heronarts.lx.color.LXColor;

/**
 * A palette that samples from an array of color values, interpolating between adjacent values in the array.  By
 * default, the palette extends over the entire array, but can be pointed at an adjustable subrange of the array.
 */
public class ArrayPalette implements ColorPalette {
    int[] colors;
    float hueCenter;

    public ArrayPalette(int[] colors) {
        this.colors = new int[colors.length];
        for (int i = 0; i < colors.length; i++) {
            this.colors[i] = 0xff000000 | colors[i];
        }
    }

    public String toString() {
        String result = "";
        for (int i = 0; i < colors.length; i++) {
            result += String.format("0x%06x, ", colors[i] & 0xffffff);
        }
        return "com.symmetrylabs.slstudio.palettes.ArrayPalette(new int[] {" + result.substring(0, result.length() - 2) + "})";
    }

    double clamp(double value, double low, double high) {
        return Math.min(Math.max(value, low), high);
    }

    public int getColor(double p) {
        double index = clamp(p, 0, 1) * (colors.length - 1);
        int low = (int) Math.floor(index);
        int high = (low + 1) < colors.length ? low + 1 : low;
        return LXColor.lerp(colors[low], colors[high], index - low);
    }

    public long getColor16(double p) {
        double index = clamp(p, 0, 1) * (colors.length - 1);
        int low = (int) Math.floor(index);
        int high = (low + 1) < colors.length ? low + 1 : low;
        return Ops16.blend(Spaces.rgb8ToRgb16(colors[low]), Spaces.rgb8ToRgb16(colors[high]), index - low);
    }

    public float getHueCenter() {
        if (hueCenter < 0) {
            float h = 0, s = 0;
            for (int i = 0; i < colors.length; i++) {
                float hue = LXColor.h(colors[i]);
                float sat = LXColor.s(colors[i]);
                if (sat > s) {
                    s = sat;
                    h = hue;
                }
            }
            hueCenter = h/360f;
        }
        return hueCenter;
    }
}
