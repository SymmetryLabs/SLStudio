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

package heronarts.lx.ui;

import processing.core.PConstants;
import processing.core.PFont;
import processing.core.PGraphics;

/**
 * A simple text label object. Draws a string aligned top-left to its x-y position.
 */
public class UILabel extends UIObject {

    /**
     * Label font
     */
    private PFont font;
    
    /**
     * Label color
     */
    private int color = 0xFFCCCCCC;
    
    /**
     * Label text
     */
    private String label = "";

    public UILabel(float x, float y, float w, float h) {
        super(x, y, w, h);
    }

    protected void onDraw(UI ui, PGraphics pg) {
        pg.textAlign(PConstants.LEFT, PConstants.TOP);
        pg.textFont(this.font);
        pg.fill(this.color);
        pg.text(this.label, 0, 0);
    }

    public UILabel setFont(PFont font) {
        this.font = font;
        redraw();
        return this;
    }

    public UILabel setColor(int color) {
        this.color = color;
        redraw();
        return this;
    }

    public UILabel setLabel(String label) {
        this.label = label;
        redraw();
        return this;
    }
}
