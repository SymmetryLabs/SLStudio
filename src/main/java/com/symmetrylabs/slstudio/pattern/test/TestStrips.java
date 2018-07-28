package com.symmetrylabs.slstudio.pattern.test;

import com.symmetrylabs.slstudio.model.StripsModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;

import com.symmetrylabs.slstudio.model.Strip;
import heronarts.lx.transform.LXVector;

import java.util.List;

/**
* @author Yona Appletree (yona@concentricsky.com)
*/
public class TestStrips extends SLPattern<StripsModel<Strip>> implements SLTestPattern {

    public TestStrips(LX lx) {
        super(lx);
    }

    public void run(double deltaMs) {
        List<Strip> strips = model.getStrips();
        for (Strip strip : strips) {
            float hue = 0;

            for (LXVector v : getVectors(strip.points)) {
                colors[v.index] = lx.hsb(hue, 100, 100);
            }

            hue += 180;
        }
    }
}
