package com.symmetrylabs.shows.pilots;

import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.color.ColorParameter;
import heronarts.lx.transform.LXVector;

import static heronarts.lx.PolyBuffer.Space.SRGB8;

public class WarpCompatibleSolidColor extends SLPattern<SLModel> {
    public static final String GROUP_NAME = PilotsShow.SHOW_NAME;
    public final ColorParameter color = new ColorParameter("Color");

    public WarpCompatibleSolidColor(LX lx) {
        super(lx);
        addParameter(color);
    }

    @Override
    public void run(double deltaMs) {
        for (int i = 0; i < colors.length; i++) {
            colors[i] = 0x00000000;
        }
        int c = color.getColor();
        int[] colors = (int[]) getArray(SRGB8);
        for (LXVector v : getVectors()) {
            colors[v.index] = c;
        }
        markModified(SRGB8);
    }
}
