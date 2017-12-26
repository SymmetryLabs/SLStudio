package com.symmetrylabs.slstudio.pattern.test;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;

import com.symmetrylabs.slstudio.model.Sun;
import com.symmetrylabs.slstudio.pattern.SunsPattern;

/**
* @author Yona Appletree (yona@concentricsky.com)
*/
public class TestSuns extends SunsPattern implements SLTestPattern {

    public TestSuns(LX lx) {
        super(lx);
    }

    public void run(double deltaMs) {
        float hue = 0;

        for (Sun sun : model.getSuns()) {
            for (LXPoint p : sun.points) {
                colors[p.index] = lx.hsb(hue, 100, 100);
            }

            hue += 70;
        }
    }
}
