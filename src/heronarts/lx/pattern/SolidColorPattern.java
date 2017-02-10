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
 * @author Mark C. Slee <mark@heronarts.com>
 */

package heronarts.lx.pattern;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.LXParameter;

public class SolidColorPattern extends LXPattern {
    public final int color;

    public final BoundedParameter brightness = new BoundedParameter("Bright", 100, 100);

    public SolidColorPattern(LX lx, int color) {
        super(lx);
        addParameter(this.brightness);

        this.color = color;
        setBrightness();
    }

    @Override
    public void onParameterChanged(LXParameter p) {
        if (p == this.brightness) {
            setBrightness();
        }
    }

    private void setBrightness() {
        setColors(LX.hsb(LXColor.h(this.color), LXColor.s(this.color), this.brightness.getValuef()));
    }

    @Override
    public void run(double deltaMs) {
    }
}
