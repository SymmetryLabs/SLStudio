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

import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.parameter.StringParameter;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dComponent;
import heronarts.p3lx.ui.UIFocus;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.event.KeyEvent;

public class UITextBox extends UI2dComponent implements UIFocus {

    private String value = "";
    private StringParameter parameter = null;

    private boolean editing = false;
    private final StringBuilder editBuffer = new StringBuilder();

    private final LXParameterListener parameterListener = new LXParameterListener() {
        public void onParameterChanged(LXParameter p) {
            setValue(parameter.getString());
        }
    };

    public UITextBox() {
        this(0, 0, 0, 0);
    }

    public UITextBox(float x, float y, float w, float h) {
        super(x, y, w, h);
        setBorderColor(UI.get().theme.getControlBorderColor());
        setBackgroundColor(UI.get().theme.getControlBackgroundColor());
    }

    public UITextBox setParameter(StringParameter parameter) {
        if (this.parameter != null) {
            this.parameter.removeListener(this.parameterListener);
        }
        this.parameter = parameter;
        if (parameter != null) {
            this.parameter.addListener(this.parameterListener);
            setValue(parameter.getString());
        }
        return this;
    }

    public String getValue() {
        return this.value;
    }

    public UITextBox setValue(String value) {
        if (!this.value.equals(value)) {
            this.value = value;
            if (this.parameter != null) {
                this.parameter.setValue(this.value);
            }
            this.onValueChange(this.value);
            redraw();
        }
        return this;
    }

    @Override
    protected void onDraw(UI ui, PGraphics pg) {
        pg.textFont(hasFont() ? getFont() : ui.theme.getControlFont());
        pg.textAlign(PConstants.CENTER, PConstants.BOTTOM);
        if (this.editing) {
            pg.fill(UI.BLACK);
            pg.noStroke();
            pg.rect(0, 0, this.width, this.height);
        }

        pg.fill(this.editing ? ui.theme.getPrimaryColor() : ui.theme.getControlTextColor());
        // TODO(mcslee): handle text overflowing buffer
        pg.text(this.editing ? this.editBuffer.toString() : this.value, this.width / 2, this.height - 2);
    }

    protected void onValueChange(String value) {
    }

    float dAccum = 0;

    @Override
    protected void onBlur() {
        super.onBlur();
        if (this.editing) {
            this.editing = false;
            String newValue = this.editBuffer.toString().trim();
            if (newValue.length() > 0) {
                setValue(newValue);
            }
            redraw();
        }
    }

    private static final String VALID_CHARACTERS =
        "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ ,./<>?;':\"[]{}-=_+`~!@#$%^&*()|\\1234567890";

    static boolean isValidTextCharacter(char keyChar) {
        return VALID_CHARACTERS.indexOf(keyChar) >= 0;
    }

    public void edit() {
        if (!this.editing) {
            this.editing = true;
            this.editBuffer.setLength(0);
            redraw();
        }
    }

    @Override
    protected void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
        if (keyCode == java.awt.event.KeyEvent.VK_ENTER) {
            if (!this.editing) {
                edit();
            } else {
                this.editing = false;
                String newValue = this.editBuffer.toString().trim();
                if (newValue.length() > 0) {
                    setValue(newValue);
                }
                redraw();
            }
        }
        if (this.editing && isValidTextCharacter(keyChar)) {
            this.editBuffer.append(keyChar);
            redraw();
        }
        if (this.editing) {
            if (keyCode == java.awt.event.KeyEvent.VK_BACK_SPACE) {
                if (this.editBuffer.length() > 0) {
                    this.editBuffer.setLength(this.editBuffer.length() - 1);
                    redraw();
                }
            } else if (keyCode == java.awt.event.KeyEvent.VK_ESCAPE) {
                this.editing = false;
                redraw();
            }
        }
    }
}
