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

import heronarts.lx.modulator.LXModulator;
import heronarts.lx.modulator.LinearEnvelope;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.LXNormalizedParameter;
import ddf.minim.AudioBuffer;
import ddf.minim.AudioSource;

/**
 * A DecibelMeter is a modulator that returns the level of an audio signal. Gain
 * may be applied to the signal. A decibel range is given in which values are
 * normalized from 0 to 1. Raw decibel values can be accessed if desired.
 */
public class DecibelMeter extends LXModulator implements LXNormalizedParameter {

    protected static final double LOG_10 = Math.log(10);

    private final LinearEnvelope decibels;

    protected final AudioBuffer buffer;

    /**
     * Gain of the meter, in decibels
     */
    public final BoundedParameter gain;

    /**
     * Range of the meter, in decibels.
     */
    public final BoundedParameter range;

    /**
     * Meter attack time, in milliseconds
     */
    public final BoundedParameter attack;

    /**
     * Meter release time, in milliseconds
     */
    public final BoundedParameter release;

    /**
     * Default constructor, creates a meter with unity gain and 72dB dynamic range
     *
     * @param source Audio source to meter
     */
    public DecibelMeter(AudioSource source) {
        this(source.mix);
    }

    /**
     * Default constructor, creates a meter with unity gain and 72dB dynamic range
     *
     * @param buffer Audio buffer to meter
     */
    public DecibelMeter(AudioBuffer buffer) {
        this("DBM", buffer);
    }

    /**
     * Default constructor, creates a meter with unity gain and 72dB dynamic range
     *
     * @param label Label
     * @param buffer Audio buffer to meter
     */
    public DecibelMeter(String label, AudioBuffer buffer) {
        super(label);
        this.buffer = buffer;
        addParameter(this.gain = new BoundedParameter("Gain", 0, -48, 48));
        addParameter(this.range = new BoundedParameter("Range", 72, 6, 96));
        addParameter(this.attack = new BoundedParameter("Attack", 30, 0, 500));
        addParameter(this.release = new BoundedParameter("Release", 100, 0, 1600));
        this.decibels = new LinearEnvelope(-this.range.getValue());
    }

    /**
     * @return Raw decibel value of the meter
     */
    public double getDecibels() {
        return decibels.getValue();
    }

    /**
     * @return Raw decibel value of the meter as a float
     */
    public float getDecibelsf() {
        return (float) getDecibels();
    }

    /**
     * @return A value for the audio meter from 0 to 1 with quadratic scaling
     */
    public double getSquare() {
        double norm = getValue();
        return norm * norm;
    }

    /**
     * @return Quadratic scaled value as a float
     */
    public float getSquaref() {
        return (float) getSquare();
    }

    @Override
    protected double computeValue(double deltaMs) {
        runEnvelope(deltaMs, decibels, this.buffer.level(), 0);
        double norm = (decibels.getValue() + this.range.getValue())
                / this.range.getValue();
        return (norm < 0) ? 0 : ((norm > 1) ? 1 : norm);
    }

    protected void runEnvelope(double deltaMs, LinearEnvelope env,
            double rawLevel, double slopeGain) {

        double minLevel = -this.range.getValue();
        double dbLevel = minLevel;
        if (rawLevel > 0) {
            // A signal level of 1.0 is our 0dB reference point, we use the
            // following definition for decibels:
            // dBV = 20 * log10(V / Vref);
            dbLevel = 20 * Math.log(rawLevel) / LOG_10;
            dbLevel += this.gain.getValue() + slopeGain;
            if (dbLevel < -this.range.getValue()) {
                dbLevel = -this.range.getValue();
            }
        }

        if (dbLevel > env.getValue()) {
            env.setRangeFromHereTo(dbLevel, attack.getValue()).trigger();
        }
        env.loop(deltaMs);
        if (!env.isRunning() && env.getValue() > minLevel) {
            env.setRangeFromHereTo(minLevel, release.getValue()).trigger();
        }
    }

    @Override
    public LXNormalizedParameter setNormalized(double value) {
        throw new UnsupportedOperationException("Cannot setNormalized on DecibelMeter");
    }

    @Override
    public double getNormalized() {
        return getValue();
    }

    @Override
    public float getNormalizedf() {
        return (float) getNormalized();
    }
}
