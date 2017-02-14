/**
 * Copyright 2017- Mark C. Slee, Heron Arts LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ##library.name##
 * ##library.sentence##
 * ##library.url##
 *
 * @author      ##author##
 * @modified    ##date##
 * @version     ##library.prettyVersion## (##library.version##)
 */

package heronarts.p3lx.ui.component;

import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dComponent;
import heronarts.p3lx.ui.UIFocus;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

public class UIDropMenu extends UI2dComponent implements UIFocus, LXParameterListener {

    private DiscreteParameter parameter = null;

    private boolean expanded = false;

    private float closedHeight;

    private int highlight = -1;

    public UIDropMenu(float x, float y, float w, float h, DiscreteParameter parameter) {
        super(x, y, w, h);
        this.closedHeight = h;
        setParameter(parameter);
    }

    public UIDropMenu setParameter(DiscreteParameter parameter) {
        if (this.parameter != null) {
            this.parameter.removeListener(this);
        }
        this.parameter = parameter;
        this.parameter.addListener(this);
        return this;
    }

    public void onParameterChanged(LXParameter p) {
        this.highlight = this.parameter.getValuei();
        redraw();
    }

    @Override
    protected int getFocusSize() {
        return 4;
    }

    @Override
    public void onDraw(UI ui, PGraphics pg) {
        String text;
        String[] options = this.parameter.getOptions();
        if (options != null) {
            text = options[this.parameter.getValuei()];
        } else {
            text = "" + this.parameter.getValuei();
        }
        pg.stroke(ui.theme.getControlBorderColor());
        pg.fill(ui.theme.getControlBackgroundColor());
        pg.rect(0, 0, this.width-1, this.height);
        if (this.expanded) {
            pg.line(1, this.closedHeight, this.width-2, this.closedHeight);
            pg.noStroke();
            pg.fill(ui.theme.getPrimaryColor());
            pg.rect(1, this.closedHeight * (1 + this.highlight), this.width-2, this.closedHeight);
        }

        pg.textFont(hasFont() ? getFont() : ui.theme.getControlFont());
        pg.fill(ui.theme.getControlTextColor());
        pg.textAlign(PConstants.LEFT, PConstants.TOP);
        pg.text(text, 4, 2);
        pg.textAlign(PConstants.RIGHT, PConstants.TOP);
        pg.text("▾", this.width-3, 2);

        if (this.expanded) {
            int range = this.parameter.getRange();
            for (int i = 0; i < range; ++i) {
                String label = (options != null) ? options[i] : ("" + i);
                pg.fill(i == this.highlight ? 0xff000000 : ui.theme.getControlTextColor());
                pg.textAlign(PConstants.LEFT, PConstants.TOP);
                pg.text(label, 4, 2 + this.closedHeight * (i+1));
            }
        }
    }

    private void toggleExpanded() {
        if (!this.expanded) {
            this.expanded = true;
            this.highlight = this.parameter.getValuei();
            setSize(this.width, this.closedHeight * (this.parameter.getRange() + 1));
        } else {
            this.expanded = false;
            setSize(this.width, this.closedHeight);
        }
    }

    @Override
    public void onMousePressed(MouseEvent mouseEvent, float x, float y) {
        if (!this.expanded) {
            toggleExpanded();
        } else {
            if (y >= this.closedHeight) {
                this.parameter.setValue((y - this.closedHeight) / this.closedHeight);
            }
            toggleExpanded();
        }
    }

    @Override
    public void onMouseMoved(MouseEvent mouseEvent, float x, float y) {
        if (y > this.closedHeight) {
            int highlight = (int) ((y - this.closedHeight) / this.closedHeight);
            if (this.highlight != highlight) {
                this.highlight = highlight;
                redraw();
            }
        }
    }

    @Override
    public void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
        if (keyCode == java.awt.event.KeyEvent.VK_ENTER ||
                keyCode == java.awt.event.KeyEvent.VK_SPACE) {
            toggleExpanded();
        } else if (keyCode == java.awt.event.KeyEvent.VK_DOWN) {
            this.parameter.increment();
        } else if (keyCode == java.awt.event.KeyEvent.VK_UP) {
            this.parameter.decrement();
        }
    }



}