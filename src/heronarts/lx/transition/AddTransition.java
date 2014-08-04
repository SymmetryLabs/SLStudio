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

public class AddTransition extends BlendTransition {
    public AddTransition(LX lx) {
        super(lx, LXColor.Blend.ADD);
    }
}
