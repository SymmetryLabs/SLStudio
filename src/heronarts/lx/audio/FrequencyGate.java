/**
 * ##library.name##
 * ##library.sentence##
 * ##library.url##
 *
 * Copyright ##copyright## ##author##
 * All Rights Reserved
 * 
 * @author      ##author##
 * @modified    ##date##
 * @version     ##library.prettyVersion## (##library.version##)
 */

package heronarts.lx.audio;

import heronarts.lx.modulator.LinearEnvelope;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.parameter.BasicParameter;

import ddf.minim.AudioSource;

/**
 * A frequency gate monitors a Graphic EQ for a particular frequency range and
 * triggers when that range passes a certain threshold. 
 */
public class FrequencyGate extends LXModulator {
    
    /**
     * The gate level at which the trigger is engaged. When the signal crosses
     * this threshold, the gate fires. Value is in the normalized space from
     * 0 to 1.
     */
    public final BasicParameter threshold = new BasicParameter("THRSH", 0.8);
    
    /**
     * The time the trigger takes to falloff from 1 to 0 after triggered, in milliseconds
     */
    public final BasicParameter falloff = new BasicParameter("FALLOFF", 200, 0, 1600);
    
    private final GraphicEQ eq;
    
    private final LinearEnvelope signal = new LinearEnvelope(0); 
    
    private boolean peak = false;
    
    private final int minBand;
    private final int avgBands;
    
    private double lastValue = 0;
    
    /**
     * Constructs a gate that monitors a specified frequency band 
     * 
     * @param eq Equalizer to monitor
     * @param minHz Minimum frequency band
     * @param maxHz Maximum frequency band
     */
    public FrequencyGate(GraphicEQ eq, int minHz, int maxHz) {
        this("FQG", eq, minHz, maxHz);
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
        super(label);
        this.eq = eq;
        int _minBand = -1, _avgBands = 0;
        for (int i = 0; i < eq.numBands; ++i) {
            float centerFreq = eq.fft.getAverageCenterFrequency(i);
            if ((_minBand < 0) && (centerFreq > minHz)) {
                _minBand = i;
            }
            if (centerFreq > maxHz) {
                _avgBands = i - _minBand;
                break;
            }
        }
        minBand = _minBand;
        avgBands = _avgBands;
    }
    
    /**
     * 
     * @return true if this is the frame in which the gate was peaked
     */
    public boolean peak() {
        return this.peak;
    }
    
    protected double computeValue(double deltaMs) {
        double thresholdValue = this.threshold.getValue();
        double thisValue = eq.getAverage(minBand, avgBands);
        this.peak = (this.lastValue < thresholdValue) && (thisValue > thresholdValue);
        if (this.peak) {
            this.signal.setRange(1, 0, this.falloff.getValue()).trigger();
        }
        this.lastValue = thisValue;
        this.signal.run(deltaMs);
        
        return signal.getValue();
    }
}
