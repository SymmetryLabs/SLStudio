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
import heronarts.lx.LXColor;

public class BlendTransition extends LXTransition {

    private final LXColor.Blend blendMode;

    public BlendTransition(LX lx, LXColor.Blend blendMode) {
        this(lx, blendMode, Mode.FULL);
    }

    public BlendTransition(LX lx, LXColor.Blend blendMode, Mode mode) {
        super(lx);
        this.blendMode = blendMode;
        setMode(mode);
    }

    @Override
    protected void computeBlend(int[] c1, int[] c2, double progress) {
        if (progress == 0.5) {
            for (int i = 0; i < c1.length; ++i) {
                this.colors[i] = LXColor.blend(c1[i], c2[i], this.blendMode);
            }
        } else if (progress < 0.5) {
            for (int i = 0; i < c1.length; ++i) {
                this.colors[i] = LXColor.lerp(c1[i],
                    LXColor.blend(c1[i], c2[i], this.blendMode),
                    2. * progress
                );
            }
        } else {
            for (int i = 0; i < c1.length; ++i) {
                this.colors[i] = LXColor.lerp(
                    c2[i],
                    LXColor.blend(c1[i], c2[i], this.blendMode),
                    2. * (1. - progress)
                );
            }
        }
    }

}
