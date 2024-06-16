package com.symmetrylabs.slstudio.pattern;

import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.transform.LXVector;

public class Spectrum extends SLPattern<SLModel> {
    private final CompoundParameter degPerSec =
        new CompoundParameter("degs", 30, 720);
    private final CompoundParameter sat =
        new CompoundParameter("sat", 100, 100);
    private final CompoundParameter bright =
        new CompoundParameter("bright", 100, 100);
    private double hue;
    private final CompoundParameter vectorCount = new CompoundParameter("vectorCount", 0, 300);

    public Spectrum(LX lx) {
        super(lx);
        addParameter(degPerSec);
        hue = 0;
    }

    @Override
    public void run(double elapsedMs, PolyBuffer.Space preferredSpace) {
        hue += degPerSec.getValue() * elapsedMs / 1000.;
        final int color = LXColor.hsb((float) hue, sat.getValuef(), bright.getValuef());
        int[] colors = (int[]) getArray(PolyBuffer.Space.RGB8);
        int count = 0;

        for (LXVector v : getVectors())
        {
            if (count < degPerSec.getValuef())
            {
                colors[v.index] = color;
                // System.out.println(v.index);
            }else
            {
                colors[v.index] = 0;
            }
            count += 1;
            // System.out.println(color);
        }

        markModified(PolyBuffer.Space.RGB8);
    }
}
