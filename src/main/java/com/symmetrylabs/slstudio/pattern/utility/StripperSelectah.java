package com.symmetrylabs.slstudio.pattern.utility;

import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.transform.LXVector;

import static heronarts.lx.PolyBuffer.Space.SRGB8;

public class StripperSelectah extends SLPattern<StripsModel> {
    private DiscreteParameter selectedstrip;

    public StripperSelectah(LX lx){
        super(lx);
        int N = model.getStrips().size();
        addParameter(selectedstrip = new DiscreteParameter("StripIndex", N));
    }

    public void run(double deltaMs){
        setColors(LXColor.BLACK);
        for(LXVector v: model.getStripByIndex(selectedstrip.getValuei()).getVectorArray()){
            colors[v.index] = LXColor.BLUE;
        }
    }

}
