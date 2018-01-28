package com.symmetrylabs.slstudio.pattern.test;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.DiscreteParameter;

import com.symmetrylabs.slstudio.model.Slice;
import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.Sun;
import com.symmetrylabs.slstudio.pattern.SunsPattern;

public class SelectedStrip extends SunsPattern implements SLTestPattern {

    public DiscreteParameter selectedStrip = new DiscreteParameter("selectedStrip", 1, 70);

    public SelectedStrip(LX lx) {
        super(lx);

        addParameter(selectedStrip);
    }

    public void run(double deltaMs) {
        setColors(0);
        for (Sun sun : this.model.getSuns()) {
            for (Slice slice : sun.getSlices()) {
                int stripIndex = selectedStrip.getValuei();

                if (stripIndex > slice.getStrips().size()) {
                    break;
                }

                Strip strip = slice.getStrips().get(selectedStrip.getValuei() - 1);

                for (LXPoint p : strip.points) {
                    colors[p.index] = LXColor.RED;
                }
            }
        }
    }
}


