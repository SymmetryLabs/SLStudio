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

public class MultiplyTransition extends BlendTransition {
    public MultiplyTransition(LX lx) {
        super(lx, LXColor.Blend.MULTIPLY);
    }
}
