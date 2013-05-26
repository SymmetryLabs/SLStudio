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

import processing.core.PConstants;
import processing.core.PGraphics;

public abstract class LXGraphicsPattern extends LXPattern {

    private final PGraphics g;
    
    protected LXGraphicsPattern(HeronLX lx) {
        super(lx);
        this.g = lx.applet.createGraphics(lx.width, lx.height, PConstants.P2D);
    }
    
    final protected void run(int deltaMs) {
        this.g.beginDraw();
        this.run(deltaMs, this.g);
        this.g.endDraw();
        this.g.loadPixels();
        for (int i = 0; i < this.lx.total; ++i) {
            this.colors[i] = this.g.pixels[i];
        }
    }
    
    abstract protected void run(int deltaMs, PGraphics g);

}
