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
import processing.core.PGraphics;

public class UIToggleSet extends UIObject {

    private String[] options = null;

    private int[] boundaries = null;

    private String value = null;

    private boolean evenSpacing = false;

    public UIToggleSet(float x, float y, float w, float h) {
        super(x, y, w, h);
    }

    public UIToggleSet setOptions(String[] options) {
        this.options = options;
        this.value = options[0];
        this.boundaries = new int[options.length];
        computeBoundaries();
        redraw();
        return this;
    }

    private void computeBoundaries() {
        if (this.boundaries == null) {
            return;
        }
        if (this.evenSpacing) {
            for (int i = 0; i < this.boundaries.length; ++i) {
                this.boundaries[i] = (int) ((i + 1) * this.width / this.boundaries.length);
            }
        } else {
            int totalLength = 0;
            for (String s : this.options) {
                totalLength += s.length();
            }
            int lengthSoFar = 0;
            for (int i = 0; i < this.options.length; ++i) {
                lengthSoFar += this.options[i].length();
                this.boundaries[i] = (int) (lengthSoFar * this.width / totalLength);
            }
        }
    }

    public UIToggleSet setEvenSpacing() {
        if (!this.evenSpacing) {
            this.evenSpacing = true;
            computeBoundaries();
            redraw();
        }
        return this;
    }

    public String getValue() {
        return this.value;
    }

    public UIToggleSet setValue(String option) {
        if (this.value != option) {
            this.value = option;
            onToggle(this.value);
            redraw();
        }
        return this;
    }

    public void onDraw(UI ui, PGraphics pg) {
        pg.stroke(0xff666666);
        pg.fill(0xff222222);
        pg.rect(0, 0, this.width, this.height);
        for (int b : this.boundaries) {
            pg.line(b, 1, b, this.height - 1);
        }
        pg.noStroke();
        pg.textAlign(PConstants.CENTER);
        pg.textFont(ui.getItemFont());
        int leftBoundary = 0;

        for (int i = 0; i < this.options.length; ++i) {
            boolean isActive = this.options[i].equals(this.value);
            if (isActive) {
                pg.fill(ui.getHighlightColor());
                pg.rect(leftBoundary + 1, 1, this.boundaries[i] - leftBoundary - 1, this.height - 1);
            }
            pg.fill(isActive ? ui.WHITE : ui.getTextColor());
            pg.text(this.options[i], (leftBoundary + this.boundaries[i]) / 2.f, this.height - 6);
            leftBoundary = this.boundaries[i];
        }
    }

    public void onMousePressed(float mx, float my) {
        for (int i = 0; i < this.boundaries.length; ++i) {
            if (mx < this.boundaries[i]) {
                setValue(this.options[i]);
                break;
            }
        }
    }

    protected void onToggle(String option) {
    }

}
