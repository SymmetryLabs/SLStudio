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
import heronarts.lx.LXUtils;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.BoundedParameter;
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
        new BoundedParameter("Range", 36, 6, 96).setUnits(LXParameter.Units.DECIBELS);

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
        new BoundedParameter("Decay", 400, 0, 1600).setUnits(LXParameter.Units.MILLISECONDS);

    /**
     * Minimum frequency for the band
     */
    public final BoundedParameter minFreq;

    /**
     * Maximum frequency for the band
     */
    public final BoundedParameter maxFreq;

    public final GraphicMeter meter;

    /**
     * Trigger parameter is set to true for one frame when the beat is triggered.
     */
    public final BooleanParameter trigger = new BooleanParameter("Trigger");

    /**
     * Level parameter is the average of the monitored band
     */
    public final BoundedParameter average = new BoundedParameter("Average");

    private float averageRaw = 0;

    /**
     * Envelope value that goes from 1 to 0 after this band is triggered
     */
    private double envelope = 0;

    private double averageOctave = 1;

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
        int nyquist = meter.fft.getSampleRate() / 2;
        this.minFreq = (BoundedParameter) new BoundedParameter("MinFreq", 60, 0, nyquist)
            .setExponent(4)
            .setUnits(LXParameter.Units.HERTZ);
        this.maxFreq = (BoundedParameter) new BoundedParameter("MaxFreq", 120, 0, nyquist)
            .setExponent(4)
            .setUnits(LXParameter.Units.HERTZ);

        addParameter(this.gain);
        addParameter(this.range);
        addParameter(this.attack);
        addParameter(this.release);
        addParameter(this.slope);
        addParameter(this.threshold);
        addParameter(this.floor);
        addParameter(this.decay);
        addParameter(this.minFreq);
        addParameter(this.maxFreq);
        addParameter(this.trigger);
        addParameter(this.average);
    }

    @Override
    public void onParameterChanged(LXParameter p) {
        super.onParameterChanged(p);
        if (p == this.minFreq) {
            if (this.minFreq.getValue() > this.maxFreq.getValue()) {
                this.minFreq.setValue(this.maxFreq.getValue());
            } else {
                updateAverageOctave();
            }
        } else if (p == this.maxFreq) {
            if (this.maxFreq.getValue() < this.minFreq.getValue()) {
                this.maxFreq.setValue(this.minFreq.getValue());
            } else {
                updateAverageOctave();
            }
        }
    }

    private void updateAverageOctave() {
        double averageFreq = (this.minFreq.getValue() + this.maxFreq.getValue()) / 2.;
        this.averageOctave = Math.log(averageFreq / FourierTransform.BASE_BAND_HZ) / FourierTransform.LOG_2;
    }

    /**
     * Constructs a gate that monitors a specified frequency band
     *
     * @param meter Equalizer to monitor
     * @param minHz Minimum frequency band
     * @param maxHz Maximum frequency band
     */
    public BandGate(GraphicMeter meter, float minHz, float maxHz) {
        this("Beat", meter);
        setFrequencyRange(minHz, maxHz);
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
        setFrequencyRange(minHz, maxHz);
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
    public BandGate setFrequencyRange(float minHz, float maxHz) {
        this.minFreq.setValue(minHz);
        this.maxFreq.setValue(maxHz);
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
        return this.average.getValue();
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
        double rangeValue = this.range.getValue();
        double gainValue = this.gain.getValue();
        double slopeValue = this.slope.getValue();

        // Computes all the underlying bands
        this.impl.compute(
            this.meter.fft,
            attackGain,
            releaseGain,
            gainValue,
            rangeValue,
            slopeValue
        );

        float newAverage = this.meter.fft.getAverage(this.minFreq.getValuef(), this.maxFreq.getValuef()) / this.meter.fft.getSize();
        float averageGain = (newAverage >= this.averageRaw) ? attackGain : releaseGain;
        this.averageRaw = newAverage + averageGain * (this.averageRaw - newAverage);
        double averageDb = 20 * Math.log(this.averageRaw) / DecibelMeter.LOG_10 + gainValue + slopeValue * this.averageOctave;

        double averageNorm = 1 + averageDb / rangeValue;
        this.average.setValue(LXUtils.constrain(averageNorm, 0, 1));

        double thresholdValue = this.threshold.getValue();

        if (this.waitingForFloor) {
            double floorValue = thresholdValue * this.floor.getValue();
            if (averageNorm < floorValue) {
                this.waitingForFloor = false;
            }
        }

        boolean triggered = !this.waitingForFloor && (thresholdValue > 0) && (averageNorm >= thresholdValue);
        if (triggered) {
            this.waitingForFloor = true;
            this.envelope = 1;
        } else {
            this.envelope = Math.max(0, this.envelope - deltaMs / this.decay.getValue());
        }
        this.trigger.setValue(triggered);

        return this.envelope;
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
