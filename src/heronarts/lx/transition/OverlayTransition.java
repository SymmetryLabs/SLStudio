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
import processing.core.PConstants;

public class OverlayTransition extends BlendTransition {
    public OverlayTransition(LX lx) {
        super(lx, PConstants.OVERLAY);
    }
}
