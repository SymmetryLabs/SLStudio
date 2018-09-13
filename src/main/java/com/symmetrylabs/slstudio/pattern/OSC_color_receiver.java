package com.symmetrylabs.slstudio.pattern;

import com.symmetrylabs.color.Ops16;
import com.symmetrylabs.color.Ops8;
import com.symmetrylabs.color.Spaces;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import com.symmetrylabs.util.CubeMarker;
import com.symmetrylabs.util.Marker;
import com.symmetrylabs.util.Octahedron;
import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import heronarts.lx.audio.LXAudioBuffer;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.transform.LXVector;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.List;

import static heronarts.lx.PolyBuffer.Space.RGB16;
import static heronarts.lx.PolyBuffer.Space.SRGB8;

public class OSC_color_receiver extends SLPattern<SLModel> {
    private final int STREAM_LENGTH = 100;
    private DiscreteParameter rParam = new DiscreteParameter("red", 155, 0, 0xff);
    private DiscreteParameter gParam = new DiscreteParameter("green", 155, 0, 0xff);
    private DiscreteParameter bParam = new DiscreteParameter("blue", 155, 0, 0xff);
    private DiscreteParameter aParam = new DiscreteParameter("alpha", 4, 0, 0xff);

    public OSC_color_receiver(LX lx) {
        super(lx);

        addParameter(rParam);
        addParameter(gParam);
        addParameter(bParam);
        addParameter(aParam);

    }

    public void run(double deltaMs, PolyBuffer.Space space) {
        int[] colors = (int[]) getArray(SRGB8);

        int red = rParam.getValuei();
        int green = gParam.getValuei();
        int blue = bParam.getValuei();
        int alpha = aParam.getValuei();

        for (LXVector v : getVectors()) {
            int c = LXColor.rgba(red, green, blue, alpha);
            colors[v.index] = c;
        }
        markModified(SRGB8);
    }
}
