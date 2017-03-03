/**
 * Copyright 2013- Mark C. Slee, Heron Arts LLC
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

import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.LXListenableNormalizedParameter;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UIFocus;
import processing.core.PGraphics;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

public class UISwitch extends UIParameterControl implements UIFocus {

    public final static int SWITCH_MARGIN = 6;
    public final static int SWITCH_SIZE = 28;
    public final static int WIDTH = SWITCH_SIZE + 2*SWITCH_MARGIN;

    protected boolean isMomentary = false;

    public UISwitch(float x, float y) {
        super(x, y, WIDTH, SWITCH_SIZE);
    }

    public UISwitch setMomentary(boolean momentary) {
        this.isMomentary = momentary;
        return this;
    }

    @Override
    public UIParameterControl setParameter(LXListenableNormalizedParameter parameter) {
        if (!(parameter instanceof BooleanParameter)) {
            throw new IllegalArgumentException("UISwitch may only take BooleanParameter");
        }
        return super.setParameter(parameter);
    }

    private BooleanParameter getBooleanParameter() {
        return (BooleanParameter) this.parameter;
    }

    @Override
    protected void onDraw(UI ui, PGraphics pg) {
        pg.stroke(ui.theme.getControlBorderColor());
        if (isEnabled() && (this.parameter != null)) {
            pg.fill((this.parameter.getValue() > 0) ? ui.theme.getPrimaryColor() :
                ui.theme.getControlBackgroundColor());
        } else {
            pg.fill(ui.theme.getControlDisabledColor());
        }
        pg.rect(SWITCH_MARGIN, 0, SWITCH_SIZE, SWITCH_SIZE);

        super.onDraw(ui, pg);
    }

    @Override
    protected void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
        super.onKeyPressed(keyEvent, keyChar, keyCode);
        if ((keyCode == java.awt.event.KeyEvent.VK_SPACE) || (keyCode == java.awt.event.KeyEvent.VK_ENTER)) {
            if (this.parameter != null) {
                consumeKeyEvent();
                if (this.isMomentary) {
                    getBooleanParameter().setValue(true);
                } else {
                    getBooleanParameter().toggle();
                }
            }
        }
    }

    @Override
    protected void onKeyReleased(KeyEvent keyEvent, char keyChar, int keyCode) {
        super.onKeyReleased(keyEvent, keyChar, keyCode);
        if ((keyCode == java.awt.event.KeyEvent.VK_SPACE) || (keyCode == java.awt.event.KeyEvent.VK_ENTER)) {
            if ((this.parameter != null) && this.isMomentary) {
                consumeKeyEvent();
                getBooleanParameter().setValue(false);
            }
        }
    }

    private boolean isOnSwitch(float mx, float my) {
        return
            (mx >= SWITCH_MARGIN) &&
            (mx < SWITCH_SIZE + SWITCH_MARGIN) &&
            (my < SWITCH_SIZE);
    }

    @Override
    protected void onMousePressed(MouseEvent mouseEvent, float mx, float my) {
        super.onMousePressed(mouseEvent, mx, my);
        if (this.parameter != null && isOnSwitch(mx, my)) {
            if (this.isMomentary) {
                getBooleanParameter().setValue(true);
            } else {
                getBooleanParameter().toggle();
            }
        }
    }

    @Override
    protected void onMouseReleased(MouseEvent mouseEvent, float mx, float my) {
        super.onMouseReleased(mouseEvent, mx, my);
        if (this.isMomentary && (this.parameter != null) && isOnSwitch(mx, my)) {
            getBooleanParameter().setValue(false);
        }
    }

}
