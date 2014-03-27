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

import heronarts.lx.LXUtils;
import heronarts.lx.ui.UI;
import heronarts.lx.ui.UIFocus;

import processing.core.PConstants;
import processing.core.PGraphics;

public class UISlider extends UIParameterControl implements UIFocus {

    private static final float HANDLE_WIDTH = 12;

    public UISlider() {
        this(0, 0, 0, 0);
    }
    
    public UISlider(float x, float y, float w, float h) {
        super(x, y, w, h);
        setBackgroundColor(0xff333333);
        setBorderColor(0xff666666);
    }

    protected void onDraw(UI ui, PGraphics pg) {
        pg.noStroke();
        pg.fill(0xff222222);
        pg.rect(4, this.height / 2 - 2, this.width - 8, 4);
        pg.fill(0xff666666);
        pg.stroke(0xff222222);
        pg.rect((int) (4 + getNormalized() * (this.width - 8 - HANDLE_WIDTH)), 4, HANDLE_WIDTH, this.height - 8);
    }

    private boolean editing = false;
    private long lastClick = 0;
    private float doubleClickMode = 0;
    private float doubleClickX = 0;

    protected void onMousePressed(float mx, float my) {
        long now = System.currentTimeMillis();
        double handleLeft = 4 + getNormalized() * (this.width - 8 - HANDLE_WIDTH);
        if ((mx >= handleLeft) && (mx < handleLeft + HANDLE_WIDTH)) {
            this.editing = true;
        } else {
            if ((now - this.lastClick) < DOUBLE_CLICK_THRESHOLD
                    && Math.abs(mx - this.doubleClickX) < 3) {
                setNormalized(this.doubleClickMode);
            }
            this.doubleClickX = mx;
            if (mx < this.width * .25) {
                this.doubleClickMode = 0;
            } else if (mx > this.width * .75) {
                this.doubleClickMode = 1;
            } else {
                this.doubleClickMode = 0.5f;
            }
        }
        this.lastClick = now;
    }

    protected void onMouseReleased(float mx, float my) {
        this.editing = false;
    }

    protected void onMouseDragged(float mx, float my, float dx, float dy) {
        if (this.editing) {
            setNormalized(LXUtils.constrain((mx - HANDLE_WIDTH / 2. - 4) / (this.width - 8 - HANDLE_WIDTH), 0, 1));
        }
    }
}
