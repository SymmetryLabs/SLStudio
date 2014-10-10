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
import heronarts.lx.color.LXColor;

public class DissolveTransition extends LXTransition {

    private final int[] sb1, sb2;

    public DissolveTransition(LX lx) {
        super(lx);
        this.sb1 = new int[lx.total];
        this.sb2 = new int[lx.total];
    }

    @Override
    protected void computeBlend(int[] c1, int[] c2, double progress) {
        LXColor.scaleBrightness(c1, (float) (1 - progress), this.sb1);
        LXColor.scaleBrightness(c2, (float) progress, this.sb2);
        for (int i = 0; i < this.colors.length; ++i) {
            this.colors[i] = LXColor.add(this.sb1[i], this.sb2[i]);
        }
    }

}
