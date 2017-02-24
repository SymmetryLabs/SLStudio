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

public class UITextBox extends UIInputBox {

    private String value = "";
    private StringParameter parameter = null;

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

    @Override
    protected String getValueString() {
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

    /**
     * Subclasses may override to handle value changes
     *
     * @param value
     */
    protected /* abstract */ void onValueChange(String value) {}


    @Override
    protected void saveEditBuffer() {
        String value = this.editBuffer.trim();
        if (value.length() > 0) {
            setValue(value);
        }
    }

    private static final String VALID_CHARACTERS =
        "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ ,./<>?;':\"[]{}-=_+`~!@#$%^&*()|\\1234567890";

    public static boolean isValidTextCharacter(char keyChar) {
        return VALID_CHARACTERS.indexOf(keyChar) >= 0;
    }

    @Override
    protected boolean isValidCharacter(char keyChar) {
        return isValidTextCharacter(keyChar);
    }

}
