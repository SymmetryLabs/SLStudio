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

package heronarts.lx.effect;

import heronarts.lx.LX;
import heronarts.lx.parameter.BasicParameter;

import processing.core.PConstants;

public class BlurEffect extends LXEffect {
    
    public final BasicParameter amount = new BasicParameter("BLUR", 0);
    
    private final int[] blurBuffer = new int[this.lx.total];
    
    public BlurEffect(LX lx) {
        super(lx);
        for (int i = 0; i < blurBuffer.length; ++i) {
            this.blurBuffer[i] = 0xff000000;
        }
        addParameter(this.amount);
    }
    
    public void apply(int[] colors) {
        float blurf = this.amount.getValuef();
        if (blurf > 0) {
            blurf = 1 - (1-blurf)*(1-blurf)*(1-blurf);
            for (int i = 0; i < colors.length; ++i) {
                int blend = this.lx.applet.blendColor(colors[i], this.blurBuffer[i], PConstants.SCREEN);
                this.blurBuffer[i] = colors[i] = this.lx.applet.lerpColor(colors[i], blend, blurf, PConstants.RGB);
            }
        }
    }
}
