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

package heronarts.lx.modulator;

import heronarts.lx.LX;

/**
 * Waveshapes compute a function in the range 0-1 over a basis 0-1.
 */
public interface LXWaveshape {

    public double compute(double basis);
    public double invert(double value, double basisHint);

    public static LXWaveshape SIN = new LXWaveshape() {
        @Override
        public double compute(double basis) {
            return (1 + Math.sin(basis * LX.TWO_PI - LX.HALF_PI)) / 2.;
        }

        @Override
        public double invert(double value, double basisHint) {
            double sinValue = -1 + 2. * value;
            double angle = Math.asin(sinValue);
            if (basisHint > 0.5) {
                angle = Math.PI - angle;
            }
            return (angle + LX.HALF_PI) / LX.TWO_PI;
        }
    };

    public static LXWaveshape TRI = new LXWaveshape() {
        @Override
        public double compute(double basis) {
            return (basis < 0.5) ? (2. * basis) : (1. - 2. * (basis-0.5));
        }

        @Override
        public double invert(double value, double basisHint) {
            return (basisHint < 0.5) ? (value / 2.) : (1. - (value / 2.));
        }
    };

    public static LXWaveshape UP = new LXWaveshape() {

        @Override
        public double compute(double basis) {
            return basis;
        }

        @Override
        public double invert(double value, double basisHint) {
            return value;
        }
    };

    public static LXWaveshape DOWN = new LXWaveshape() {

        @Override
        public double compute(double basis) {
            return 1. - basis;
        }

        @Override
        public double invert(double value, double basisHint) {
            return 1. - value;
        }
    };

    public static LXWaveshape SQUARE = new LXWaveshape() {

        @Override
        public double compute(double basis) {
            return (basis < 0.5) ? 0 : 1;
        }

        @Override
        public double invert(double value, double basisHint) {
            return (value == 0) ? 0 : 0.5;
        }
    };


}
