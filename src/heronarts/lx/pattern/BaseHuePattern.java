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

import heronarts.lx.LX;
import heronarts.lx.LXColor;

public class BaseHuePattern extends LXPattern {
    public BaseHuePattern(LX lx) {
        super(lx);
    }

    @Override
    public void run(double deltaMs) {
        for (int i = 0; i < this.colors.length; ++i) {
            this.colors[i] = LXColor.hsb(this.lx.getBaseHue(), 100, 100);
        }
    }
}
