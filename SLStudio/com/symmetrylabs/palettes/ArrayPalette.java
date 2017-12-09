package com.symmetrylabs.palettes;

import heronarts.lx.color.LXColor;

/**
 * A palette that samples from an array of color values, interpolating between adjacent values in the array.  By
 * default, the palette extends over the entire array, but can be pointed at an adjustable subrange of the array.
 */
class ArrayPalette implements ColorPalette {
    int[] colors;

    ArrayPalette(int[] colors) {
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
        return "com.symmetrylabs.palettes.ArrayPalette(new int[] {" + result.substring(0, result.length() - 2) + "})";
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
}
