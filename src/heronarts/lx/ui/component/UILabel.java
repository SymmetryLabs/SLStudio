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

package heronarts.lx.ui.component;

import heronarts.lx.ui.UI;
import heronarts.lx.ui.UIObject;
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
    
    private int horizontalAlignment = PConstants.LEFT;
    
    private int verticalAlignment = PConstants.TOP;
    
    private boolean hasBackground = false;
    
    private int backgroundColor = 0xFF000000;
    
    private boolean hasBorder = false;
    
    private int borderColor = 0xFF000000;
    
    private int padding = 0;
    
    /**
     * Label color
     */
    private int color = 0xFFCCCCCC;
    
    /**
     * Label text
     */
    private String label = "";

    public UILabel() {
        this(0, 0, 0, 0);
    }
    
    public UILabel(float x, float y, float w, float h) {
        super(x, y, w, h);
    }

    public UILabel setPadding(int padding) {
        this.padding = padding;
        return this;
    }
    
    public UILabel setBackground(boolean hasBackground) {
        this.hasBackground = hasBackground;
        return this;
    }
    
    public UILabel setBackground(int backgroundColor) {
        this.hasBackground = true;
        this.backgroundColor = backgroundColor;
        return this;
    }
    
    public UILabel setBorder(boolean hasBorder) {
        this.hasBorder = hasBorder;
        return this;
    }
    
    public UILabel setBorder(int borderColor) {
        this.hasBorder = true;
        this.borderColor = borderColor;
        return this;
    }
    
    public UILabel setAlignment(int horizontalAlignment) {
        setAlignment(horizontalAlignment, PConstants.BASELINE);
        return this;
    }
    
    public UILabel setAlignment(int horizontalAlignment, int verticalAlignment) {
        this.horizontalAlignment = horizontalAlignment;
        this.verticalAlignment = verticalAlignment;
        return this;
    }
    
    protected void onDraw(UI ui, PGraphics pg) {
        if (this.hasBackground || this.hasBorder) {
            if (this.hasBorder) {
                pg.stroke(this.borderColor);
            } else {
                pg.noStroke();
            }
            if (this.hasBackground) {
                pg.fill(this.backgroundColor);
            } else {
                pg.noFill();
            }
            pg.rect(0, 0, this.width, this.height);
        }
        pg.textFont((this.font == null) ? ui.getTitleFont() : this.font);
        pg.fill(this.color);
        float tx = this.padding, ty = this.padding;
        switch (this.horizontalAlignment) {
        case PConstants.CENTER: tx = this.width / 2; break;
        case PConstants.RIGHT: tx = this.width - this.padding; break;
        }
        switch (this.verticalAlignment) {
        case PConstants.BASELINE: ty = this.height - this.padding; break;
        case PConstants.BOTTOM: ty = this.height - this.padding; break;
        case PConstants.CENTER: ty = this.height/ 2; break;
        }
        pg.textAlign(this.horizontalAlignment, this.verticalAlignment);
        pg.text(this.label, tx, ty);
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
