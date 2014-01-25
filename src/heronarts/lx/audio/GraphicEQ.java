package heronarts.lx.audio;

import heronarts.lx.modulator.LinearEnvelope;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.parameter.BasicParameter;

import ddf.minim.AudioSource;
import ddf.minim.analysis.FFT;

/**
 * A graphic equalizer splits the signal into frequency bands and computes envelopes
 * for each of the bands independently. It can also give the overall level, just
 * like a normal decibel meter.
 * 
 * Since energy is not typically evenly distributed through the spectrum, a slope
 * can be applied to the equalizer to even out the levels, typically something like
 * 4.5 dB/octave is used, though this varies by recording.
 */
public class GraphicEQ extends DecibelMeter {

    /**
     * dB/octave slope applied to the equalizer
     */
    public final BasicParameter slope = new BasicParameter("SLOPE", 4.5, -3, 12); 
    
    /**
     * Number of bands in the equalizer
     */
    public final int numBands;

    final FFT fft;
    
    private final int timeSize;
    
    private final int bandsPerOctave;    
    
    private final LinearEnvelope[] bands; 
    
    /**
     * Default graphic equalizer with 2 bands per octave
     * 
     * @param source Audio source
     */
    public GraphicEQ(AudioSource source) {
        this(source, 2);
    }
    
    /**
     * Default graphic equalizer with 2 bands per octave
     * 
     * @param label Label
     * @param source Audio source
     */
    public GraphicEQ(String label, AudioSource source) {
        this(label, source, 2);
    }
    
    /**
     * Makes a graphic equalizer with a default slope of 4.5 dB/octave
     * 
     * @param source Audio source to listen to
     * @param bandsPerOctave Number of bands per octave
     */
    public GraphicEQ(AudioSource source, int bandsPerOctave) {
        this("GEQ", source, bandsPerOctave);
    }
    
    /**
     * Makes a graphic equalizer with a default slope of 4.5 dB/octave
     * 
     * @param label Label
     * @param source Audio source to listen to
     * @param bandsPerOctave Number of bands per octave
     */
    public GraphicEQ(String label, AudioSource source, int bandsPerOctave) {
        super(label, source.mix);
        addParameter(this.slope);
        this.fft = new FFT(this.timeSize = source.bufferSize(), source.sampleRate());
        this.fft.window(FFT.HAMMING);
        this.fft.logAverages(50, this.bandsPerOctave = bandsPerOctave);
        this.numBands = this.fft.avgSize();
        this.bands = new LinearEnvelope[this.numBands];
        for (int i = 0; i < this.numBands; ++i) {
            this.bands[i] = new LinearEnvelope(-this.range.getValue());
        }
    }
    
    @Override
    protected double computeValue(double deltaMs) {
        this.fft.forward(this.buffer);
        for (int i = 0; i < this.numBands; ++i) {
            runEnvelope(deltaMs, this.bands[i], this.fft.getAvg(i) / this.timeSize, i * this.slope.getValue() / this.bandsPerOctave);
        }
        return super.computeValue(deltaMs);
    }
    
    /**
     * @param i Which frequency band to access
     * @return Level of that band in decibels
     */
    public double getDecibels(int i) {
        return -this.range.getValue() * (1 - getBand(i));
    }
    
    /**
     * @param i Which frequency band to access
     * @return Level of that band in decibels as a float
     */
    public float getDecibelsf(int i) {
        return (float) getDecibels(i);
    }
    
    /**
     * @param i Which frequency band to retrieve
     * @return The value of the ith frequency band
     */
    public double getBand(int i) {
        double norm = (this.bands[i].getValue() + this.range.getValue()) / this.range.getValue();
        return (norm < 0) ? 0 : ((norm > 1) ? 1 : norm);
    }
    
    /**
     * @param i Which frequency band to retrieve
     * @return The value of that band, as a float
     */
    public float getBandf(int i) {
        return (float) getBand(i);
    }
    
    /**
     * Gets the squared value of the i-th band
     * 
     * @param i Frequency band
     * @return Squared normalized value
     */
    public double getSquare(int i) {
        double norm = getBand(i);
        return norm * norm;
    }
    
    /**
     * Gets the squared value of the i-th band
     * 
     * @param i Frequency band
     * @return Squared normalized value as a float
     */
    public float getSquaref(int i) {
        return (float) getSquare(i);
    }
    
    /**
     * Averages the value of a set of bands
     * 
     * @param minBand The first band to start at
     * @param avgBands How many bands to average
     * @return Average value of all these bands
     */
    public double getAverage(int minBand, int avgBands) {
        double avg = 0;
        int i = 0;
        for (; i < avgBands; ++i) {
            if (minBand + i >= numBands) break;
            avg += getBand(minBand + i);
        }
        return avg / i;
    }
    
    /**
     * Averages the value of a set of bands
     * 
     * @param minBand The first band to start at
     * @param avgBands How many bands to average
     * @return Average value of all these bands as a float
     */
    public float getAveragef(int minBand, int avgBands) {
        return (float) getAverage(minBand, avgBands);
    }

}
