package com.symmetrylabs.slstudio.pattern.test;

import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.Sun;
import com.symmetrylabs.slstudio.pattern.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;

/**
* @author Yona Appletree (yona@concentricsky.com)
*/
public class TestStrips extends SLPattern implements SLTestPattern {

    public TestStrips(LX lx) {
        super(lx);
    }

    public void run(double deltaMs) {
        for (Sun sun : model.getSuns()) {
            float hue = 0;

            for (Strip strip : sun.getStrips()) {
                for (LXPoint p : strip.points) {
                    colors[p.index] = lx.hsb(hue, 100, 100);
                }

                hue += 180;
            }
        }
    }
}
