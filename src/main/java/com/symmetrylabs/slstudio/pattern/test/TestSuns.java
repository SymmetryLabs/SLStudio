package com.symmetrylabs.slstudio.pattern.test;

import com.symmetrylabs.slstudio.model.Sun;
import com.symmetrylabs.slstudio.pattern.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;

/**
* @author Yona Appletree (yona@concentricsky.com)
*/
public class TestSuns extends SLPattern implements SLTestPattern {

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
