/**
 * Copyright 2017- Mark C. Slee, Heron Arts LLC
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
 * @author Mark C. Slee <mark@heronarts.com>
 */

package heronarts.lx.effect;

import heronarts.lx.LX;
import heronarts.lx.LXEffect;
import heronarts.lx.LXUtils;
import heronarts.lx.blend.MultiplyBlend;
import heronarts.lx.color.LXColor;
import heronarts.lx.modulator.LXWaveshape;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.EnumParameter;
import heronarts.lx.parameter.FunctionalParameter;
import heronarts.lx.parameter.LXParameter;

public class StrobeEffect extends LXEffect {

    public enum Waveshape {
        SIN,
        TRI,
        UP,
        DOWN,
        SQUARE
    };

    public final EnumParameter<Waveshape> shape =
        new EnumParameter<Waveshape>("Shape", Waveshape.SIN)
        .setDescription("Wave shape of strobing");

    public final CompoundParameter frequency = (CompoundParameter)
        new CompoundParameter("Freq", 1, .05, 10)
        .setExponent(2)
        .setUnits(LXParameter.Units.HERTZ)
        .setDescription("Frequency of strobing");

    public final CompoundParameter depth =
        new CompoundParameter("Depth", 0.5)
        .setDescription("Depth of the strobe effect");

    private final SawLFO basis = (SawLFO) startModulator(new SawLFO(1, 0, new FunctionalParameter() {
        @Override
        public double getValue() {
            return 1000 / frequency.getValue();
    }}));

    public StrobeEffect(LX lx) {
        super(lx);
        addParameter("frequency", this.frequency);
        addParameter("shape", this.shape);
        addParameter("depth", this.depth);
    }

    @Override
    protected void onEnable() {
        this.basis.setBasis(0).start();
    }

    private LXWaveshape getWaveshape() {
        switch (this.shape.getEnum()) {
        case SIN: return LXWaveshape.SIN;
        case TRI: return LXWaveshape.TRI;
        case UP: return LXWaveshape.UP;
        case DOWN: return LXWaveshape.DOWN;
        case SQUARE: return LXWaveshape.SQUARE;
        }
        return LXWaveshape.SIN;
    }

    @Override
    public void run(double deltaMs, double amount) {
        float amt = this.enabledDamped.getValuef() * this.depth.getValuef();
        if (amt > 0) {
            float strobef = this.basis.getValuef();
            strobef = (float) getWaveshape().compute(strobef);
            strobef = LXUtils.lerpf(1, strobef, amt);
            if (strobef < 1) {
                if (strobef == 0) {
                    setColors(LXColor.BLACK);
                } else {
                    int src = LXColor.gray(100 * strobef);
                    MultiplyBlend.multiply(this.colors, src, 1, this.colors);
                }
            }
        }
    }
}