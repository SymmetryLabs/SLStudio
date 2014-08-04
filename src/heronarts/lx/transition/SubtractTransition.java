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

public class SubtractTransition extends BlendTransition {
    public SubtractTransition(LX lx) {
        super(lx, LXColor.Blend.SUBTRACT);
    }
}
