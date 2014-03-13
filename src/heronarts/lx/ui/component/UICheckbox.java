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
import processing.core.PConstants;
import processing.core.PGraphics;

public class UICheckbox extends UIButton {

    private boolean firstDraw = true;

    public UICheckbox() {
        this(0, 0, 0, 0);
    }
    
    public UICheckbox(float x, float y, float w, float h) {
        super(x, y, w, h);
        setMomentary(false);
    }

    public void onDraw(UI ui, PGraphics pg) {
        if (this.firstDraw) {
            pg.fill(this.labelColor);
            pg.textFont(ui.getItemFont());
            pg.textAlign(PConstants.LEFT, PConstants.CENTER);
            pg.text(this.label, this.height + 4, this.height / 2);
            this.firstDraw = false;
        }
    }
}
