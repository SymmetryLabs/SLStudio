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
import processing.core.PGraphics;
import processing.core.PConstants;


public class DissolveTransition extends LXTransition {
    
    private final int lerpMode;
    
    public DissolveTransition(HeronLX lx) {
        this(lx, PConstants.RGB);
    }
    
    public DissolveTransition(HeronLX lx, int lerpMode) {
        super(lx);
        this.lerpMode = lerpMode;
    }

    protected void computeBlend(int[] c1, int[] c2, double progress) {
        for (int i = 0; i < this.colors.length; ++i) {
            this.colors[i] = PGraphics.blendColor(
                this.lx.applet.color(
                    this.lx.applet.hue(c1[i]),
                    this.lx.applet.saturation(c1[i]),
                    (float) (this.lx.applet.brightness(c1[i]) * (1-progress))
                ),
                this.lx.applet.color(
                    this.lx.applet.hue(c2[i]),
                    this.lx.applet.saturation(c2[i]),
                        (float) (this.lx.applet.brightness(c2[i]) * progress)
                ),
                PConstants.ADD
            );
        }
    }

}
