package com.symmetrylabs.slstudio.pattern.test;

import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.Sun;
import com.symmetrylabs.slstudio.pattern.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.DiscreteParameter;

/**
* @author Yona Appletree (yona@concentricsky.com)
*/
public class selectColumn extends SLPattern implements SLTestPattern {

    final DiscreteParameter xp = new DiscreteParameter("Column", 161);

    public selectColumn(LX lx) {
        super(lx);
        addParameter(xp);
    }

    public void run(double deltaMs) {
        int x = xp.getValuei();
        setColors(0);
        for (Sun sun : model.getSuns()) {
            float hue = 0;

            for (Strip strip : sun.getStrips()) {
                int counter = 0;
                for (LXPoint p : strip.points) {
                    if (counter++ == x){
                        colors[p.index] = lx.hsb(hue+120, 50, 100);
                    }
                }

                // hue += 180;
            }
        }
    }
}
