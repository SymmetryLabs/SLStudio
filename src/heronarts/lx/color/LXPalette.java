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
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.DampedParameter;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.TriangleLFO;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.FunctionalParameter;
import heronarts.lx.parameter.LXParameter;

/**
 * A palette is an object that is used to compute color values and set modes
 * of color computation. Though its use is not required, it is very useful for
 * creating coherent color schemes across patterns.
 */
public class LXPalette extends LXComponent {

    public enum Mode {
        FIXED,
        OSCILLATE,
        CYCLE
    };

    public final DiscreteParameter hueMode = new DiscreteParameter("Mode", Mode.values()).setOptions(new String[] { "Fixed", "Oscillate", "Cycle" });

    public final ColorParameter color = new ColorParameter("Color", 0xffff0000);

    /**
     * Hack... the Process preprocessor doesn't let you address object.color, duplicate it to clr
     */
    public final ColorParameter clr = color;

    public final BoundedParameter range = new BoundedParameter("Range", 0, 360);

    public final BoundedParameter period = new BoundedParameter("Period", 120000, 1000, 1800000);

    public final BoundedParameter spreadX = new BoundedParameter("X-add", 0, 360);

    public final BoundedParameter spreadY = new BoundedParameter("Y-add", 0, 360);

    public final BoundedParameter spreadZ = new BoundedParameter("Z-add", 0, 360);

    public final BoundedParameter offsetX = new BoundedParameter("X-off", 0, -1, 1);

    public final BoundedParameter offsetY = new BoundedParameter("Y-off", 0, -1, 1);

    public final BoundedParameter offsetZ = new BoundedParameter("Z-off", 0, -1, 1);

    public final BoundedParameter spreadR = new BoundedParameter("R-add", 0, 360);

    public final BooleanParameter mirror = new BooleanParameter("Mirror", true);

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

    private double xMult;
    private double yMult;
    private double zMult;
    private double rMult;

    public LXPalette(LX lx) {
        super(lx);
        computeMults(lx.model);
        lx.addListener(new LX.Listener() {
            @Override
            public void modelChanged(LX lx, LXModel model) {
                computeMults(model);
            }
        });

        addParameter(this.hueMode);
        addParameter(this.color);
        addParameter(this.period);
        addParameter(this.range);
        addParameter(this.spreadX);
        addParameter(this.spreadY);
        addParameter(this.spreadZ);
        addParameter(this.spreadR);
        addParameter(this.offsetX);
        addParameter(this.offsetY);
        addParameter(this.offsetZ);
        addParameter(this.mirror);
        addModulator(this.hueFixed).start();
        addModulator(this.hueCycle);
        addModulator(this.hueOscillate);
    }

    private void computeMults(LXModel model) {
        this.xMult = 1 / model.xRange;
        this.yMult = 1 / model.yRange;
        this.zMult = 1 / model.zRange;
        this.rMult = 1 / model.rRange;
    }

    @Override
    public void onParameterChanged(LXParameter parameter) {
        if (parameter == this.hueMode) {
            double hueValue = this.hue.getValue();
            this.color.hue.setValue(hueValue);
            switch ((Mode) this.hueMode.getObject()) {
                case FIXED:
                    this.hue = this.hueFixed;
                    this.hueFixed.setValue(hueValue).start();
                    this.hueCycle.stop();
                    this.hueOscillate.stop();
                    break;
                case CYCLE:
                    this.hue = this.hueCycle;
                    this.hueFixed.stop();
                    this.hueOscillate.stop();
                    this.hueCycle.setValue(hueValue).start();
                    break;
                case OSCILLATE:
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
        double dx = point.x - this.model.cx - this.offsetX.getValue() * model.xRange;
        double dy = point.y - this.model.cy - this.offsetY.getValue() * model.yRange;
        double dz = point.z - this.model.cz - this.offsetZ.getValue() * model.zRange;
        if (this.mirror.isOn()) {
            dx = Math.abs(dx);
            dy = Math.abs(dy);
            dz = Math.abs(dz);
        }
        return (
            this.hue.getValue() +
            360 +
            this.spreadX.getValue() * this.xMult * dx +
            this.spreadY.getValue() * this.yMult * dy +
            this.spreadZ.getValue() * this.zMult * dz +
            this.spreadR.getValue() * this.rMult * Math.abs(point.r)
         ) % 360;
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
