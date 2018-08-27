package com.symmetrylabs.shows.usopen;

import com.symmetrylabs.color.Ops16;
import com.symmetrylabs.util.ColorUtils;

public class ScaleLuminosity16 {
    public static long scaleLuminosity(long color, float scale) {
        float rgb[] = new float[3];
        rgb[0] = (float) Ops16.red(color) / (float) Ops16.MAX;
        rgb[1] = (float) Ops16.green(color) / (float) Ops16.MAX;
        rgb[2] = (float) Ops16.blue(color) / (float) Ops16.MAX;
        float xyz[] = new float[3];
        float lab[] = new float[3];
        ColorUtils.rgb2xyz(rgb, xyz);
        ColorUtils.xyz2lab(xyz, lab);
        lab[0] = scale * lab[0];
        ColorUtils.lab2xyz(lab, xyz);
        ColorUtils.xyz2rgb(xyz, rgb);
        return Ops16.rgba(
            (int) rgb[0] * Ops16.MAX,
            (int) rgb[1] * Ops16.MAX,
            (int) rgb[2] * Ops16.MAX,
            Ops16.alpha(color));
    }
}
