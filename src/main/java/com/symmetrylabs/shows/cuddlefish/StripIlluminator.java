package com.symmetrylabs.shows.cuddlefish;

import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.DiscreteParameter;

import java.util.List;

public class StripIlluminator extends SLPattern<StripsModel> {

    /** 0-based strip index to illuminate, or -1 for all black. */
    public final DiscreteParameter stripIndex =
        new DiscreteParameter("strip", -1, -1, 10000);

    public StripIlluminator(LX lx) {
        super(lx);
        addParameter(stripIndex);
    }

    @Override
    public void run(double deltaMs) {
        setColors(LXColor.BLACK);
        int idx = stripIndex.getValuei();
        if (idx < 0) return;
        List<Strip> strips = model.getStrips();
        if (idx < strips.size()) {
            for (LXPoint p : strips.get(idx).getPoints()) {
                colors[p.index] = LXColor.WHITE;
            }
        }
    }
}
