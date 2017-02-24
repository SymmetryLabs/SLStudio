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

import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dComponent;
import heronarts.p3lx.ui.UIFocus;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

public abstract class UIInputBox extends UI2dComponent implements UIFocus {

    protected boolean editing = false;
    protected String editBuffer = "";

    protected UIInputBox() {
        this(0, 0, 0, 0);
    }

    protected UIInputBox(float x, float y, float w, float h) {
        super(x, y, w, h);
        setBorderColor(UI.get().theme.getControlBorderColor());
        setBackgroundColor(UI.get().theme.getControlBackgroundColor());
        setTextAlignment(PConstants.CENTER);
    }

    protected abstract String getValueString();

    protected abstract void saveEditBuffer();

    public void edit() {
        if (!this.editing) {
            this.editing = true;
            this.editBuffer = "";
        }
        redraw();
    }

    @Override
    protected void onBlur() {
        super.onBlur();
        if (this.editing) {
            this.editing = false;
            saveEditBuffer();
            redraw();
        }
    }

    @Override
    protected void onDraw(UI ui, PGraphics pg) {
        pg.textFont(hasFont() ? getFont() : ui.theme.getControlFont());
        if (this.editing) {
            pg.fill(UI.BLACK);
            pg.noStroke();
            pg.rect(0, 0, this.width, this.height);
        }

        pg.fill(this.editing ? ui.theme.getPrimaryColor() : ui.theme.getControlTextColor());

        // TODO(mcslee): handle text overflowing buffer
        if (this.textAlignHorizontal == PConstants.LEFT) {
            pg.textAlign(PConstants.LEFT, PConstants.CENTER);
            pg.text(this.editing ? this.editBuffer.toString() : getValueString(), 2, this.height / 2);
        } else {
            pg.textAlign(PConstants.CENTER, PConstants.CENTER);
            pg.text(this.editing ? this.editBuffer.toString() : getValueString(), this.width / 2, this.height / 2);
        }
    }

    protected abstract boolean isValidCharacter(char keyChar);

    /**
     * Subclasses may optionally override to decrement value in response to arrows.
     * Decrement is invoked for the left or down arrow keys.
     *
     * @param keyEvent
     */
    protected void decrementValue(KeyEvent keyEvent) {}

    /**
     * Subclasses may optionally override to decrement value in response to arrows.
     * Increment is invoked for the right or up keys.
     *
     * @param keyEvent
     */
    protected void incrementValue(KeyEvent keyEvent) {}

    @Override
    protected void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
        if (this.editing) {
            // Editing!
            if (isValidCharacter(keyChar)) {
                consumeKeyEvent();
                this.editBuffer += keyChar;
                redraw();
            } else if (keyCode == java.awt.event.KeyEvent.VK_ENTER) {
                consumeKeyEvent();
                this.editing = false;
                saveEditBuffer();
                redraw();
            } else if (keyCode == java.awt.event.KeyEvent.VK_BACK_SPACE) {
                consumeKeyEvent();
                if (this.editBuffer.length() > 0) {
                    this.editBuffer = this.editBuffer.substring(0, this.editBuffer.length() - 1);
                    redraw();
                }
            } else if (keyCode == java.awt.event.KeyEvent.VK_ESCAPE) {
                consumeKeyEvent();
                this.editing = false;
                redraw();
            }
        } else {
            // Not editing
            if (keyCode == java.awt.event.KeyEvent.VK_ENTER || keyCode == java.awt.event.KeyEvent.VK_SPACE) {
                consumeKeyEvent();
                this.editing = true;
                this.editBuffer = "";
                redraw();
            } else if ((keyCode == java.awt.event.KeyEvent.VK_LEFT) || (keyCode == java.awt.event.KeyEvent.VK_DOWN)) {
                decrementValue(keyEvent);
            } else if ((keyCode == java.awt.event.KeyEvent.VK_RIGHT) || (keyCode == java.awt.event.KeyEvent.VK_UP)) {
                incrementValue(keyEvent);
            }
        }
    }

    /**
     * Subclasses may optionally implement to change value based upon mouse click+drag in the box.
     *
     * @param offset Units of mouse movement, positive or negative
     */
    protected void incrementMouseValue(MouseEvent mouseEvent, int offset) {}

    private float dAccum = 0;

    @Override
    protected void onMousePressed(MouseEvent mouseEvent, float mx, float my) {
        this.dAccum = 0;
    }

    @Override
    protected void onMouseDragged(MouseEvent mouseEvent, float mx, float my, float dx, float dy) {
        this.dAccum -= dy;
        int offset = (int) (this.dAccum / 5);
        this.dAccum = this.dAccum - (offset * 5);
        if (!this.editing) {
            incrementMouseValue(mouseEvent, offset);
        }
    }

}
