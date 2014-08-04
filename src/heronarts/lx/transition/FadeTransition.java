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

public class FadeTransition extends LXTransition {

    public FadeTransition(LX lx) {
        super(lx);
    }

    @Override
    protected void computeBlend(int[] c1, int[] c2, double progress) {
        int[] c = (progress < 0.5) ? c1 : c2;
        double b = Math.abs(progress - 0.5) * 2.;

        for (int i = 0; i < this.colors.length; ++i) {
            this.colors[i] = LXColor.hsb(
                    LXColor.h(c[i]),
                    LXColor.s(c[i]),
                    (float) (b * LXColor.b(c[i])));
        }
    }
}
