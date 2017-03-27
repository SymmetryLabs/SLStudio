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

import heronarts.lx.LX;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXNormalizedParameter;
import heronarts.lx.parameter.LXParameter;

/**
 * A frequency gate monitors a Graphic Meter for a particular frequency range and
 * triggers when that range passes a certain threshold. Note that the beat detect
 * does *not* respect the attack and release settings of the underlying meter, it
 * merely shares the raw values. The BeatDetect applies its own time-band filtering.
 */
public class BandGate extends LXModulator implements LXNormalizedParameter {

    /**
     * Gain of the meter, in decibels
     */
    public final BoundedParameter gain = (BoundedParameter)
        new BoundedParameter("Gain", 0, -48, 48).setUnits(LXParameter.Units.DECIBELS);

    /**
     * Range of the meter, in decibels.
     */
    public final BoundedParameter range = (BoundedParameter)
        new BoundedParameter("Range", 48, 6, 96).setUnits(LXParameter.Units.DECIBELS);

    /**
     * Meter attack time, in milliseconds
     */
    public final BoundedParameter attack = (BoundedParameter)
        new BoundedParameter("Attack", 10, 0, 100).setUnits(LXParameter.Units.MILLISECONDS);

    /**
     * Meter release time, in milliseconds
     */
    public final BoundedParameter release = (BoundedParameter)
        new BoundedParameter("Release", 100, 0, 1000).setExponent(2).setUnits(LXParameter.Units.MILLISECONDS);

    /**
     * dB/octave slope applied to the equalizer
     */
    public final BoundedParameter slope = (BoundedParameter)
        new BoundedParameter("Slope", 4.5, -3, 12).setUnits(LXParameter.Units.DECIBELS);

    /**
     * The gate level at which the trigger is engaged. When the signal crosses
     * this threshold, the gate fires. Value is in the normalized space from 0 to
     * 1.
     */
    public final BoundedParameter threshold = new BoundedParameter("Threshold", 0.8);

    /**
     * The floor at which the trigger releases. Once triggered, the signal must
     * fall below this amount before a new trigger may occur. This value is
     * specified as a fraction of the threshold. So, a value of 0.75 means the
     * signal must fall to 75% of the threshold value.
     */
    public final BoundedParameter floor = new BoundedParameter("Floor", 0.75);

    /**
     * The time the trigger takes to falloff from 1 to 0 after triggered, in
     * milliseconds
     */
    public final BoundedParameter decay = (BoundedParameter)
        new BoundedParameter("Decay", 200, 0, 1600).setUnits(LXParameter.Units.MILLISECONDS);

    /**
     * Trigger parameter is set to true for one frame when the beat is triggered.
     */
    public final BooleanParameter trigger = new BooleanParameter("Trigger");

    /**
     * The first band that is inspected for the avarage
     */
    public final DiscreteParameter minBand;

    /**
     * The number of bands that are inspected for the average
     */
    public final DiscreteParameter numBands;

    public final GraphicMeter meter;

    private double level = 0;

    private double smoothed = 0;

    private boolean waitingForFloor = false;

    private final LXMeterImpl impl;

    public BandGate(LX lx) {
        this("Beat", lx);
    }

    public BandGate(String label, LX lx) {
        this(label, lx.engine.audio.meter);
    }

    /**
     * Constructs a gate that monitors a specified frequency band
     *
     * @param label Label
     * @param meter GraphicEQ object to drive this gate
     */
    public BandGate(String label, GraphicMeter meter) {
        super(label);
        this.impl = new LXMeterImpl(meter.numBands, meter.fft.getBandOctaveRatio());
        this.meter = meter;
        this.minBand = new DiscreteParameter("Band", meter.numBands);
        this.numBands = new DiscreteParameter("Width", 1, meter.numBands + 1);
        addParameter(this.gain);
        addParameter(this.range);
        addParameter(this.attack);
        addParameter(this.release);
        addParameter(this.slope);
        addParameter(this.threshold);
        addParameter(this.floor);
        addParameter(this.decay);
        addParameter(this.minBand);
        addParameter(this.numBands);
        addParameter(this.trigger);
    }

    /**
     * Constructs a gate that monitors a specified frequency band
     *
     * @param meter Equalizer to monitor
     * @param minHz Minimum frequency band
     * @param maxHz Maximum frequency band
     */
    public BandGate(GraphicMeter meter, int minHz, int maxHz) {
        this("Beat", meter);
        setRange(minHz, maxHz);
    }

    /**
     * Constructs a gate that monitors a specified frequency band
     *
     * @param label Label
     * @param meter Equalizer to monitor
     * @param minHz Minimum frequency band
     * @param maxHz Maximum frequency band
     */
    public BandGate(String label, GraphicMeter meter, int minHz, int maxHz) {
        this(label, meter);
        setRange(minHz, maxHz);
    }

    public double getExponent() {
        throw new UnsupportedOperationException("BandGate does not support exponent");
    }

    /**
     * Sets range of frequencies to look at
     *
     * @param minHz Minimum frequency
     * @param maxHz Maximum frequency
     * @return this
     */
    public BandGate setRange(int minHz, int maxHz) {
        // TODO(mcslee): fix this!
//    int _minBand = -1, _avgBands = 0;
//    for (int i = 0; i < this.meter.numBands; ++i) {
//      float centerFreq = this.meter.fft.getAverageCenterFrequency(i);
//      if ((_minBand < 0) && (centerFreq > minHz)) {
//        _minBand = i;
//      }
//      if (centerFreq > maxHz) {
//        _avgBands = i - _minBand;
//        break;
//      }
//    }
//    this.minBand.setValue(_minBand);
//    this.numBands.setValue(_avgBands);
        return this;
    }

    /**
     * Set the bands to look at
     *
     * @param minBand First band index
     * @param numBands Number of bands to average
     * @return this
     */
    public BandGate setBands(int minBand, int numBands) {
        this.minBand.setValue(minBand);
        this.numBands.setValue(numBands);
        return this;
    }

    public double getBand(int i) {
        return this.impl.getBand(i);
    }

    /**
     *
     * @return true if this is the frame in which the gate was peaked
     */
    public boolean peak() {
        return this.trigger.isOn();
    }

    /**
     * Gets the level of the frequency range being monitored.
     *
     * @return Level of range from 0-1
     */
    public double getLevel() {
        return this.level;
    }

    /**
     * Gets the level of the frequency range being monitored.
     *
     * @return Level of range from 0-1
     */
    public float getLevelf() {
        return (float) getLevel();
    }

    @Override
    protected double computeValue(double deltaMs) {
        float attackGain = (float) Math.exp(-deltaMs / this.attack.getValue());
        float releaseGain = (float) Math.exp(-deltaMs / this.release.getValue());
        this.impl.compute(
            this.meter.fft,
            attackGain,
            releaseGain,
            this.gain.getValue(),
            this.range.getValue(),
            this.slope.getValue()
        );

        this.level = this.impl.getAverage(this.minBand.getValuei(), this.numBands.getValuei());

        double thresholdValue = this.threshold.getValue();

        if (this.waitingForFloor) {
            double floorValue = thresholdValue * this.floor.getValue();
            if (this.level < floorValue) {
                this.waitingForFloor = false;
            }
        }

        boolean triggered = !this.waitingForFloor && (thresholdValue > 0) && (this.level >= thresholdValue);
        if (triggered) {
            this.waitingForFloor = true;
            this.smoothed = 1;
        } else {
            this.smoothed = Math.max(0, this.smoothed - deltaMs / this.decay.getValue());
        }
        this.trigger.setValue(triggered);

        return this.smoothed;
    }

    @Override
    public LXNormalizedParameter setNormalized(double value) {
        throw new UnsupportedOperationException("BandGate does not support setNormalized()");
    }

    @Override
    public double getNormalized() {
        return getLevel();
    }

    @Override
    public float getNormalizedf() {
        return (float) getNormalized();
    }
}
