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
    private final int[] sb1, sb2;
    
    public DissolveTransition(HeronLX lx) {
        this(lx, PConstants.RGB);
    }
    
    public DissolveTransition(HeronLX lx, int lerpMode) {
        super(lx);
        this.lerpMode = lerpMode;
        this.sb1 = new int[lx.total];
        this.sb2 = new int[lx.total];
    }

    protected void computeBlend(int[] c1, int[] c2, double progress) {
        lx.scaleBrightness(c1, (float) (1-progress), this.sb1);
        lx.scaleBrightness(c2, (float) progress, this.sb2);
        for (int i = 0; i < this.colors.length; ++i) {
            this.colors[i] = PGraphics.blendColor(this.sb1[i], this.sb2[i], PConstants.ADD);
        }
    }

}
