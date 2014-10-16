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

package heronarts.lx.color;

import heronarts.lx.LX;
import heronarts.lx.LXComponent;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.LinearEnvelope;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.TriangleLFO;
import heronarts.lx.parameter.BasicParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.MutableParameter;

/**
 * A palette is an object that is used to compute color values and set modes
 * of color computation. Though its use is not required, it is very useful for
 * creating coherent color schemes across patterns.
 */
public class LXPalette extends LXComponent {

    public static final int HUE_MODE_FIXED = 0;
    public static final int HUE_MODE_CYCLE = 1;
    public static final int HUE_MODE_OSCILLATE = 2;

    public final DiscreteParameter hueMode = new DiscreteParameter("Mode",
        new String[] { "Fixed", "Cycle", "Oscillate" });

    public final BasicParameter hue = new BasicParameter("Hue", 0, 360);

    public final BasicParameter hue2 = new BasicParameter("Hue2", 0, 360);

    public final BasicParameter saturation = new BasicParameter("Saturation", 100, 0, 100);

    public final MutableParameter period = new MutableParameter("Period");

    private final LinearEnvelope hueFixed = new LinearEnvelope(0, hue, 100);

    private final SawLFO hueCycle = new SawLFO(0, 360, period);

    private final TriangleLFO hueOscillate = new TriangleLFO(hue, hue2, period);

    public LXPalette(LX lx) {
        super(lx);
        addParameter(this.hue);
        addParameter(this.hue2);
        addParameter(this.saturation);
        addParameter(this.period);
        addModulator(this.hueFixed);
        addModulator(this.hueCycle);
        addModulator(this.hueOscillate);
    }

    @Override
    public void onParameterChanged(LXParameter parameter) {
        if (parameter == this.hue) {
            this.hueFixed.start();
        } else if (parameter == this.hueMode) {
            switch (this.hueMode.getValuei()) {
                case HUE_MODE_FIXED:
                    this.hueFixed.start();
                    this.hueCycle.stop();
                    this.hueOscillate.stop();
                    break;
                case HUE_MODE_CYCLE:
                    this.hueFixed.stop();
                    this.hueOscillate.stop();
                    this.hueCycle.start();
                    break;
                case HUE_MODE_OSCILLATE:
                    this.hueFixed.stop();
                    this.hueOscillate.stop();
                    this.hueCycle.start();
                    break;
            }
        }
    }

    public double getHue() {
        switch (this.hueMode.getValuei()) {
        case HUE_MODE_CYCLE:
            return this.hueCycle.getValue();
        case HUE_MODE_OSCILLATE:
            return this.hueOscillate.getValue();
        default:
        case HUE_MODE_FIXED:
            return this.hueFixed.getValue();
        }
    }

    public final float getHuef() {
        return (float) getHue();
    }

    public double getSaturation() {
        return this.saturation.getValue();
    }

    public final float getSaturationf() {
        return (float) getSaturation();
    }

    public double getHue(LXPoint point) {
        return getHue();
    }

    public final float getHuef(LXPoint point) {
        return (float) getHue(point);
    }

    public double getSaturation(LXPoint point) {
        return getSaturation();
    }

    public final float getSaturationf(LXPoint point) {
        return (float) getSaturation(point);
    }

    public int getColor() {
        return LXColor.hsb(getHue(), getSaturation(), 100);
    }

    public int getColor(double brightness) {
        return LXColor.hsb(getHue(), getSaturation(), brightness);
    }

    public int getColor(double saturation, double brightness) {
        return LXColor.hsb(getHue(), saturation, brightness);
    }

    public int getColor(LXPoint point) {
        return getColor(point, getSaturation(), 100);
    }

    public int getColor(LXPoint point, double brightness) {
        return getColor(point, getSaturation(point), brightness);
    }

    public int getColor(LXPoint point, double saturation, double brightness) {
        return LXColor.hsb(getHue(point), saturation, brightness);
    }


}
