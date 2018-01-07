package com.symmetrylabs.slstudio.pattern;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;

import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.Sun;
import com.symmetrylabs.slstudio.pattern.SunsPattern;

/**
* @author Yona Appletree (yona@concentricsky.com)
*/
public class TestStrips extends SunsPattern {

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
