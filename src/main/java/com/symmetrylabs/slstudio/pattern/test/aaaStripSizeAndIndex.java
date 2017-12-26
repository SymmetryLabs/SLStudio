package com.symmetrylabs.slstudio.pattern.test;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.DiscreteParameter;

import com.symmetrylabs.slstudio.model.Slice;
import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.Sun;
import com.symmetrylabs.slstudio.pattern.SunsPattern;

/**
* @author Yona Appletree (yona@concentricsky.com)
*/
public class aaaStripSizeAndIndex extends SunsPattern implements SLTestPattern {
    final DiscreteParameter indexp = new DiscreteParameter("index", 100);
    final DiscreteParameter sizep = new DiscreteParameter("length", 35);

    public aaaStripSizeAndIndex (LX lx) {
        super(lx);
        addParameter(indexp);
        addParameter(sizep);
    }

    public void run(double deltaMs) {
        // convert parameters to int values
        int index = indexp.getValuei();
        int  size = sizep.getValuei();
        setColors(0);
        for (Sun sun : model.getSuns()) {
            float hue = 0;
            for (Slice slice : sun.getSlices()) {
                int compare_index = 0;
                for (Strip strip : slice.getStrips()) {
                    for (LXPoint p : strip.points) {
                        if ( (compare_index++ > index) && (compare_index < index + size) ){
                            colors[p.index] = lx.hsb(120, 100, 100);
                        }
                    }
                }
            }
        }
    }
}
