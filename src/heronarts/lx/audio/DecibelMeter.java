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

import heronarts.lx.modulator.LXModulator;
import heronarts.lx.modulator.LinearEnvelope;
import heronarts.lx.control.BasicParameter;

import ddf.minim.AudioBuffer;

/**
 * A DecibelMeter is a modulator that returns the level of an audio signal. Gain may
 * be applied to the signal. A decibel range is given in which values are normalized
 * from 0 to 1. Raw decibel values can be accessed if desired.
 */
public class DecibelMeter extends LXModulator {
    
    protected static final double LOG_10 = Math.log(10);

    private final LinearEnvelope decibels;    
    
    protected final AudioBuffer buffer;
        
    /**
     * Gain of the meter, in decibels
     */
    public final BasicParameter gain;
    
    /**
     * Range of the meter, in decibels.
     */
    public final BasicParameter range;
    
    /**
     * Meter attack time, in milliseconds
     */
    public final BasicParameter attack;
    
    /**
     * Meter release time, in milliseconds
     */
    public final BasicParameter release;
    
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
        addParameter(this.gain = new BasicParameter("GAIN", 0, -36, 36));
        addParameter(this.range = new BasicParameter("RANGE", 72, 6, 96));
        addParameter(this.attack = new BasicParameter("ATTACK", 30, 0, 500));
        addParameter(this.release = new BasicParameter("RELEASE", 100, 0, 1600));
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
        double norm = (decibels.getValue() + this.range.getValue()) / this.range.getValue();
        return (norm < 0) ? 0 : ((norm > 1) ? 1 : norm);
    }
    
    protected void runEnvelope(double deltaMs, LinearEnvelope env, double rawLevel, double slopeGain) {
        
        double minLevel = -this.range.getValue();
        double dbLevel = minLevel;
        if (rawLevel > 0) {
            // A signal level of 1.0 is our 0dB reference point, we use the
            // following definition for decibels:
            //   dBV = 20 * log10(V / Vref);
            dbLevel = 20 * Math.log(rawLevel) / LOG_10;
            dbLevel += this.gain.getValue() + slopeGain;
            if (dbLevel < -this.range.getValue()) {
                dbLevel = -this.range.getValue();
            }
        }
        
        if (dbLevel > env.getValue()) {
            env.setRangeFromHereTo(dbLevel, attack.getValue()).trigger();
        }
        env.run(deltaMs);
        if (!env.isRunning() && env.getValue() > minLevel) {
            env.setRangeFromHereTo(minLevel, release.getValue()).trigger();
        }
    }
}
