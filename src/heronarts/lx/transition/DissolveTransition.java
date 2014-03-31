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

public class DissolveTransition extends LXTransition {

    private final int[] sb1, sb2;

    public DissolveTransition(LX lx) {
        super(lx);
        this.sb1 = new int[lx.total];
        this.sb2 = new int[lx.total];
    }

    @Override
    protected void computeBlend(int[] c1, int[] c2, double progress) {
        LX.scaleBrightness(c1, (float) (1 - progress), this.sb1);
        LX.scaleBrightness(c2, (float) progress, this.sb2);
        for (int i = 0; i < this.colors.length; ++i) {
            this.colors[i] = PApplet.blendColor(this.sb1[i], this.sb2[i],
                    PConstants.ADD);
        }
    }

}
