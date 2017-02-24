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

public abstract class UINumberBox extends UIInputBox {

    protected boolean hasShiftMultiplier = false;
    protected float shiftMultiplier = 1;

    protected UINumberBox() {
        this(0, 0, 0, 0);
    }

    protected UINumberBox(float x, float y, float w, float h) {
        super(x, y, w, h);
    }

    /**
     * Sets a multiplier by which the amount value changes are modulated
     * when the shift key is down. Either for more precise control or
     * larger jumps, depending on the component.
     *
     * @param shiftMultiplier Amount to multiply by
     * @return
     */
    public UINumberBox setShiftMultiplier(float shiftMultiplier) {
        this.hasShiftMultiplier = true;
        this.shiftMultiplier = shiftMultiplier;
        return this;
    }

}
