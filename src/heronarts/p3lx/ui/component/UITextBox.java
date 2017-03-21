/**
 * Copyright 2013- Mark C. Slee, Heron Arts LLC
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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
