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

package heronarts.lx.pattern;

import heronarts.lx.HeronLX;

public class BaseHuePattern extends LXPattern {
    public BaseHuePattern(HeronLX lx) {
        super(lx);
    }
    
    public void run(int deltaMs) {
        for (int i = 0; i < this.colors.length; ++i) {
            this.colors[i] = this.lx.colord(this.lx.getBaseHue(), 100, 100);
        }
    }
}
