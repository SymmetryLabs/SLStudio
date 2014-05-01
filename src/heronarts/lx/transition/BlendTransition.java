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
import processing.core.PApplet;
import processing.core.PConstants;

public class BlendTransition extends LXTransition {

    private final int blendType;

    public BlendTransition(LX lx, int blendType) {
        this(lx, blendType, Mode.FULL);
    }

    public BlendTransition(LX lx, int blendType, Mode mode) {
        super(lx);
        this.blendType = blendType;
        setMode(mode);
    }

    @Override
    protected void computeBlend(int[] c1, int[] c2, double progress) {
        if (progress == 0.5) {
            for (int i = 0; i < c1.length; ++i) {
                this.colors[i] = PApplet.blendColor(c1[i], c2[i], this.blendType);
            }
        } else if (progress < 0.5) {
            for (int i = 0; i < c1.length; ++i) {
                this.colors[i] = PApplet.lerpColor(c1[i],
                        PApplet.blendColor(c1[i], c2[i], this.blendType),
                        (float) (2. * progress), PConstants.RGB);
            }
        } else {
            for (int i = 0; i < c1.length; ++i) {
                this.colors[i] = PApplet.lerpColor(c2[i],
                        PApplet.blendColor(c1[i], c2[i], this.blendType),
                        (float) (2. * (1. - progress)), PConstants.RGB);
            }
        }
    }

}
