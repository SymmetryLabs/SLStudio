package com.symmetrylabs.slstudio.pattern.test;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;

import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.pattern.base.StripsPattern;

/**
* @author Yona Appletree (yona@concentricsky.com)
*/
public class TestStrips extends StripsPattern implements SLTestPattern {

    public TestStrips(LX lx) {
        super(lx);
    }

    public void run(double deltaMs) {
        for (Strip strip : model.getStrips()) {
            float hue = 0;

            for (LXPoint p : strip.points) {
                colors[p.index] = lx.hsb(hue, 100, 100);
            }

            hue += 180;
        }
    }
}
