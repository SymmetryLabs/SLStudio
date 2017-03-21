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

package heronarts.lx.audio;

import heronarts.lx.modulator.LXModulator;
import heronarts.lx.modulator.LinearEnvelope;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXNormalizedParameter;

/**
 * A frequency gate monitors a Graphic EQ for a particular frequency range and
 * triggers when that range passes a certain threshold.
 */
public class FrequencyGate extends LXModulator implements LXNormalizedParameter {

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
    public final BoundedParameter release = new BoundedParameter("Release", 200, 0,
            1600);

    public final DiscreteParameter minBand;

    public final DiscreteParameter avgBands;

    public final GraphicEQ eq;

    private double level = 0;

    private final LinearEnvelope signal = new LinearEnvelope(0);

    private boolean peak = false;

    private boolean waitingForFloor = false;

    /**
     * Constructs a gate that monitors a specified frequency band
     *
     * @param label Label
     * @param eq GraphicEQ object to drive this gate
     */
    public FrequencyGate(String label, GraphicEQ eq) {
        super(label);
        this.eq = eq;
        this.minBand = new DiscreteParameter("Band", eq.numBands);
        this.avgBands = new DiscreteParameter("Width", 1, eq.numBands + 1);
    }

    /**
     * Constructs a gate that monitors a specified frequency band
     *
     * @param eq Equalizer to monitor
     * @param minHz Minimum frequency band
     * @param maxHz Maximum frequency band
     */
    public FrequencyGate(GraphicEQ eq, int minHz, int maxHz) {
        this("FQG", eq);
        setRange(minHz, maxHz);
    }

    /**
     * Constructs a gate that monitors a specified frequency band
     *
     * @param label Label
     * @param eq Equalizer to monitor
     * @param minHz Minimum frequency band
     * @param maxHz Maximum frequency band
     */
    public FrequencyGate(String label, GraphicEQ eq, int minHz, int maxHz) {
        this(label, eq);
        setRange(minHz, maxHz);
    }

    /**
     * Sets range of frequencies to look at
     *
     * @param minHz Minimum frequency
     * @param maxHz Maximum frequency
     * @return this
     */
    public FrequencyGate setRange(int minHz, int maxHz) {
        int _minBand = -1, _avgBands = 0;
        for (int i = 0; i < this.eq.numBands; ++i) {
            float centerFreq = this.eq.fft.getAverageCenterFrequency(i);
            if ((_minBand < 0) && (centerFreq > minHz)) {
                _minBand = i;
            }
            if (centerFreq > maxHz) {
                _avgBands = i - _minBand;
                break;
            }
        }
        this.minBand.setValue(_minBand);
        this.avgBands.setValue(_avgBands);
        return this;
    }

    /**
     * Set the bands to look at
     *
     * @param minBand First band index
     * @param numBands Number of bands to average
     * @return this
     */
    public FrequencyGate setBands(int minBand, int numBands) {
        this.minBand.setValue(minBand);
        this.avgBands.setValue(numBands);
        return this;
    }

    /**
     *
     * @return true if this is the frame in which the gate was peaked
     */
    public boolean peak() {
        return this.peak;
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
        double thresholdValue = this.threshold.getValue();
        double floorValue = thresholdValue * this.floor.getValue();
        this.level = this.eq.getAverage(minBand.getValuei(), avgBands.getValuei());
        if (this.waitingForFloor) {
            if (this.level < floorValue) {
                this.waitingForFloor = false;
            }
        }
        this.peak = !this.waitingForFloor && (this.level > thresholdValue);
        if (this.peak) {
            this.waitingForFloor = true;
            this.signal.setRange(1, 0, this.release.getValue()).trigger();
        }
        this.signal.loop(deltaMs);
        return signal.getValue();
    }

    @Override
    public LXNormalizedParameter setNormalized(double value) {
        // TODO Auto-generated method stub
        return null;
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
