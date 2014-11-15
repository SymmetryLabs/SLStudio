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
import heronarts.lx.modulator.DampedParameter;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.TriangleLFO;
import heronarts.lx.parameter.BasicParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.FunctionalParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.MutableParameter;

/**
 * A palette is an object that is used to compute color values and set modes
 * of color computation. Though its use is not required, it is very useful for
 * creating coherent color schemes across patterns.
 */
public class LXPalette extends LXComponent {

    public static final int HUE_MODE_STATIC = 0;
    public static final int HUE_MODE_CYCLE = 1;
    public static final int HUE_MODE_OSCILLATE = 2;

    public final DiscreteParameter hueMode = new DiscreteParameter("Mode",
        new String[] { "Static", "Cycle", "Oscillate" });

    public final ColorParameter color = new ColorParameter("Color", 0xffff0000);

    /**
     * Hack for Processing 2, doesn't let you address color
     */
    public final ColorParameter clr = color;

    public final BasicParameter range = new BasicParameter("Range", 0, 360);

    public final MutableParameter period = new MutableParameter("Period", 120000);

    private final DampedParameter hueFixed = new DampedParameter(color.hue, 1800);

    private final SawLFO hueCycle = new SawLFO(0, 360, period);

    private final FunctionalParameter hue2 = new FunctionalParameter() {
        @Override
        public double getValue() {
            return color.hue.getValue() + range.getValue();
        }
    };

    private final TriangleLFO hueOscillate = new TriangleLFO(color.hue, hue2, period);

    private LXModulator hue = hueFixed;

    public LXPalette(LX lx) {
        super(lx);
        addParameter(this.hueMode);
        addParameter(this.color);
        addParameter(this.period);
        addParameter(this.range);
        addModulator(this.hueFixed).start();
        addModulator(this.hueCycle);
        addModulator(this.hueOscillate);
    }

    @Override
    public void onParameterChanged(LXParameter parameter) {
        if (parameter == this.hueMode) {
            double hueValue = this.hue.getValue();
            this.color.hue.setValue(hueValue);
            switch (this.hueMode.getValuei()) {
                case HUE_MODE_STATIC:
                    this.hue = this.hueFixed;
                    this.hueFixed.setValue(hueValue).start();
                    this.hueCycle.stop();
                    this.hueOscillate.stop();
                    break;
                case HUE_MODE_CYCLE:
                    this.hue = this.hueCycle;
                    this.hueFixed.stop();
                    this.hueOscillate.stop();
                    this.hueCycle.setValue(hueValue).start();
                    break;
                case HUE_MODE_OSCILLATE:
                    this.hue = this.hueOscillate;
                    this.hueFixed.stop();
                    this.hueCycle.stop();
                    this.hueOscillate.setValue(hueValue).start();
                    break;
            }
        }
    }

    public double getHue() {
        return this.hue.getValue();
    }

    public final float getHuef() {
        return (float) getHue();
    }

    public double getSaturation() {
        return this.color.saturation.getValue();
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
        if (brightness > 0) {
            return LXColor.hsb(getHue(), getSaturation(), brightness);
        }
        return LXColor.BLACK;
    }

    public int getColor(double saturation, double brightness) {
        if (brightness > 0) {
            return LXColor.hsb(getHue(), saturation, brightness);
        }
        return LXColor.BLACK;
    }

    public int getColor(LXPoint point) {
        return getColor(point, getSaturation(), 100);
    }

    public int getColor(LXPoint point, double brightness) {
        if (brightness > 0) {
            return getColor(point, getSaturation(point), brightness);
        }
        return LXColor.BLACK;
    }

    public int getColor(LXPoint point, double saturation, double brightness) {
        if (brightness > 0) {
            return LXColor.hsb(getHue(point), saturation, brightness);
        }
        return LXColor.BLACK;
    }

}
