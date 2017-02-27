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

public class UIFileNameBox extends UITextBox {
    public UIFileNameBox() {
        this(0, 0, 0, 0);
    }

    public UIFileNameBox(float x, float y, float w, float h) {
        super(x, y, w, h);
    }

    private static final String VALID_CHARACTERS =
        "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890.-";

    public static boolean isValidTextCharacter(char keyChar) {
        return VALID_CHARACTERS.indexOf(keyChar) >= 0;
    }

    @Override
    protected boolean isValidCharacter(char keyChar) {
        return isValidTextCharacter(keyChar);
    }
}
