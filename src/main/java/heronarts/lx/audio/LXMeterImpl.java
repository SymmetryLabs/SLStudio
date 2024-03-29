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
 * @author Mark C. Slee <mark@heronarts.com>
 */

package heronarts.lx.audio;

import heronarts.lx.parameter.NormalizedParameter;

class LXMeterImpl {

    final int numBands;
    final float bandOctaveRatio;

    final float[] rawBands;
    final float[] rmsBands;
    final double[] dbBands;
    final NormalizedParameter[] bands;

    LXMeterImpl(int numBands, float bandOctaveRatio) {
        this.numBands = numBands;
        this.bandOctaveRatio = bandOctaveRatio;
        this.rawBands = new float[this.numBands];
        this.rmsBands = new float[this.numBands];
        this.dbBands = new double[this.numBands];
        this.bands = new NormalizedParameter[this.numBands];
        for (int i = 0; i < this.numBands; ++i) {
            this.bands[i] = new NormalizedParameter("Band-" + (i+1), 0);
            this.rawBands[i] = 0;
            this.rmsBands[i] = 0;
            this.dbBands[i] = -96;
        }
    }

    void compute(FourierTransform fft, float attackGain, float releaseGain, double gain, double range, double slope) {
        for (int i = 0; i < this.numBands; ++i) {
            float rmsBand = fft.getBand(i) / fft.getSize();
            float rmsGain = (rmsBand >= this.rmsBands[i]) ? attackGain : releaseGain;
            this.rmsBands[i] = rmsBand + rmsGain * (this.rmsBands[i] - rmsBand);
            this.dbBands[i] = 20 * Math.log(this.rmsBands[i]) / DecibelMeter.LOG_10 + gain + i * slope * this.bandOctaveRatio;
            this.bands[i].setValue(1 + this.dbBands[i] / range);
        }
    }

    double getBand(int i) {
        return this.bands[i].getValue();
    }

    /**
     * Averages the value of a set of bands
     *
     * @param minBand The first band to start at
     * @param avgBands How many bands to average
     * @return Average value of all these bands
     */
    double getAverage(int minBand, int avgBands) {
        double avg = 0;
        int i = 0;
        for (; i < avgBands; ++i) {
            if (minBand + i >= numBands) {
                break;
            }
            avg += getBand(minBand + i);
        }
        return avg / i;
    }

}
