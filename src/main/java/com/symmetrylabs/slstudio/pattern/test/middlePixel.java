package com.symmetrylabs.slstudio.pattern.test;

import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.Sun;
import com.symmetrylabs.slstudio.pattern.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;

/**
* @author Yona Appletree (yona@concentricsky.com)
*/
public class middlePixel extends SLPattern implements SLTestPattern {

    public middlePixel(LX lx) {
        super(lx);
    }

    public void run(double deltaMs) {
        for (Sun sun : model.getSuns()) {
            float hue = 0;

            for (Strip strip : sun.getStrips()) {
                int counter = 0;
                for (LXPoint p : strip.points) {
                    colors[p.index] = lx.hsb(hue, 100, 15);
                    // white if middle pixel
                    if ( counter < 2 ) {
                        colors[p.index] = lx.hsb(0, 100, 100);
                    }
                    else if (counter > strip.metrics.numPoints - 3){
                        colors[p.index] = lx.hsb(120, 100, 100);
                    }
                    if ( counter++ == (strip.metrics.numPoints/2) ){
                        // println("middle index:" + strip.metrics.numPoints/2 );
                        colors[p.index] = lx.hsb(240, 0, 100);
                    }
                }

                hue += 180;
            }
        }
    }
}
