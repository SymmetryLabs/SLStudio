package com.symmetrylabs.slstudio.pattern.test;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;

import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.suns.Sun;
import com.symmetrylabs.slstudio.pattern.SunsPattern;

/**
* @author Yona Appletree (yona@concentricsky.com)
*/
public class TestLowPowerStrips extends SunsPattern implements SLTestPattern {

    public TestLowPowerStrips(LX lx) {
        super(lx);
    }

    public void run(double deltaMs) {
        setColors(0);

        for (Sun sun : model.getSuns()) {
            for (Strip strip : sun.getStrips()) {
                int si = 0;
                for (LXPoint p : strip.points) {
                    if (si < 3) {
                        colors[p.index] =lx.hsb(LXColor.RED, 100, 100);
                    }
                    if (si > strip.points.length - 3) {
                        colors[p.index] =lx.hsb(LXColor.BLUE, 100, 100);
                    }
                    si++;
                }
            }
        }
    }
}
