package com.symmetrylabs.slstudio.pattern;

import com.symmetrylabs.slstudio.model.suns.Slice;
import com.symmetrylabs.slstudio.model.suns.SunsModel;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.ObjectParameter;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;

public class TestSelectPixlite extends SLPattern {

    public final ObjectParameter selectedPixlite;
    public final CompoundParameter hue = new CompoundParameter("hue", 0, 0, 360);
    public final CompoundParameter saturation = new CompoundParameter("sat", 100, 0, 100);
    public final CompoundParameter brightness = new CompoundParameter("bri", 100, 0, 100);

    public TestSelectPixlite(LX lx) {
        super(lx);

        this.selectedPixlite = new ObjectParameter("pixlite", ((SunsModel)model).getSlices().toArray());

        System.out.println("NUM SLICES: " + ((SunsModel)model).getSlices().size());

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
