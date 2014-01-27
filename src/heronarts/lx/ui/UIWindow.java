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

import heronarts.lx.LXUtils;
import heronarts.lx.ui.component.UILabel;

import processing.core.PGraphics;

/**
 * A UIWindow is a UIContext that by default has a title bar and can be dragged
 * around when the mouse is pressed on the title bar. 
 */
public class UIWindow extends UIContext {

    private final static int TITLE_LABEL_HEIGHT = 24;
    private final static int TITLE_OFFSET_X = 6;
    private final static int TITLE_OFFSET_Y = 8;
    
    /**
     * The label object
     */
    private final UILabel label;
    
    /**
     * Constructs a window object
     * 
     * @param ui UI to place in
     * @param title Title for this window
     * @param x
     * @param y
     * @param w
     * @param h
     */
    public UIWindow(final UI ui, String title, float x, float y, float w, float h) {
        super(ui, x, y, w, h);
        this.label = new UILabel(TITLE_OFFSET_X, TITLE_OFFSET_Y, w-TITLE_OFFSET_X, TITLE_LABEL_HEIGHT-TITLE_OFFSET_Y) {
            
            protected void onMousePressed(float mx, float my) {
                ui.bringToTop(UIWindow.this);
            }
            
            protected void onMouseDragged(float mx, float my, float dx, float dy) {
                parent.x = LXUtils.constrainf(parent.x + dx, 0, ui.applet.width - this.width);
                parent.y = LXUtils.constrainf(parent.y + dy, 0, ui.applet.height - this.height);
            }
            
        }.setLabel(title).setFont(ui.getTitleFont());
        this.label.addToContainer(this);
    }

    /**
     * Set the title of the window.
     * 
     * @param title Title of the window
     * @return this window
     */
    public UIWindow setTitle(String title) {
        this.label.setLabel(title);
        return this;
    }
    
    protected void onDraw(UI ui, PGraphics pg) {
        // Draw a background under the whole window
        pg.noStroke();
        pg.fill(ui.getBackgroundColor());
        pg.stroke(0xff292929);
        pg.rect(0, 0, this.width-1, this.height-1);
    }
}
