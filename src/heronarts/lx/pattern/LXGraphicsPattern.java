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
import processing.core.PConstants;
import processing.core.PGraphics;

public abstract class LXGraphicsPattern extends LXPattern {

    private final PGraphics pg;

    protected LXGraphicsPattern(LX lx) {
        super(lx);
        this.pg = lx.applet.createGraphics(lx.width, lx.height, PConstants.P2D);
    }

    final protected void run(double deltaMs) {
        this.pg.beginDraw();
        this.run(deltaMs, this.pg);
        this.pg.endDraw();
        this.pg.loadPixels();
        for (int i = 0; i < this.lx.total; ++i) {
            this.colors[i] = this.pg.pixels[i];
        }
    }

    abstract protected void run(double deltaMs, PGraphics pg);

}
