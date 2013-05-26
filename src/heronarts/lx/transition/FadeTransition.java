/**
 * ##library.name##
 * ##library.sentence##
 * ##library.url##
 *
 * Copyright ##copyright## ##author##
 * All Rights Reserved
 * 
 * @author      ##author##
 * @modified    ##date##
 * @version     ##library.prettyVersion## (##library.version##)
 */

package heronarts.lx.transition;


import heronarts.lx.HeronLX;

import java.lang.Math;

public class FadeTransition extends LXTransition {
    
    public FadeTransition(HeronLX lx) {
        super(lx);
    }
    
    protected void computeBlend(int[] c1, int[] c2, double progress) {
        int[] c = (progress < 0.5) ? c1 : c2;
        double b = Math.abs(progress - 0.5) * 2.;
        
        for (int i = 0; i < this.colors.length; ++i) {
            this.colors[i] = this.lx.applet.color(
                this.lx.applet.hue(c[i]),
                this.lx.applet.saturation(c[i]),
                (float) (b * this.lx.applet.brightness(c[i]))
                );
        }
    }
}
