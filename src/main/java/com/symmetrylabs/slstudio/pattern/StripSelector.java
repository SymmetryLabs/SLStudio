package com.symmetrylabs.slstudio.pattern;

import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.transform.LXVector;

public class StripSelector extends SLPattern<StripsModel> {
    private DiscreteParameter selectedstrip;

    public StripSelector(LX lx){
        super(lx);
        int N = model.getStrips().size();
        addParameter(selectedstrip = new DiscreteParameter("strip", N));
    }

    public void run(double deltaMs){
        setColors(LXColor.BLACK);
        for (LXPoint v : model.getStripByIndex(selectedstrip.getValuei()).getPoints()) {
            colors[v.index] = LXColor.BLUE;
        }
    }
}
