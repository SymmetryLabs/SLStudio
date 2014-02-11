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

import heronarts.lx.LX;

import processing.core.PConstants;

public class BlendTransition extends LXTransition {

    private final int blendType;

    public BlendTransition(LX lx, int blendType) {
        super(lx);
        this.blendType = blendType;
    }

    protected void computeBlend(int[] c1, int[] c2, double progress) {
        if (progress < 0.5) {
            for (int i = 0; i < c1.length; ++i) {
                this.colors[i] = this.lx.applet.lerpColor(c1[i],
                        this.lx.applet.blendColor(c1[i], c2[i], this.blendType),
                        (float) (2. * progress), PConstants.RGB);
            }
        } else {
            for (int i = 0; i < c1.length; ++i) {
                this.colors[i] = this.lx.applet.lerpColor(c2[i],
                        this.lx.applet.blendColor(c1[i], c2[i], this.blendType),
                        (float) (2. * (1. - progress)), PConstants.RGB);
            }
        }
    }

}
