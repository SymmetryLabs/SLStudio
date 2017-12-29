package com.symmetrylabs.slstudio.pattern;

import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.model.Slice;
import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.Sun;
import com.symmetrylabs.slstudio.pattern.SLPattern;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.ObjectParameter;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;

public class TestSelectPixlite extends SLPattern {

    public final ObjectParameter selectedPixlite;
    public final CompoundParameter hue = new CompoundParameter("hue", 0, 0, 360);
    public final CompoundParameter saturation = new CompoundParameter("sat", 100, 0, 100);
    public final CompoundParameter brightness = new CompoundParameter("bri", 100, 0, 100);

    public TestSelectPixlite(LX lx) {
        super(lx);
        this.selectedPixlite = new ObjectParameter("pixlite", model.slices.toArray());
        addParameter(selectedPixlite);
        addParameter(hue);
        addParameter(saturation);
        addParameter(brightness);
    }

    public void run(double deltaMs) {
        setColors(0);
        Slice slice = (Slice)selectedPixlite.getObject();

        for (LXPoint p : slice.points) {
            colors[p.index] = lx.hsb(
                hue.getValuef(),
                saturation.getValuef(),
                brightness.getValuef()
            );
        }
    }
}