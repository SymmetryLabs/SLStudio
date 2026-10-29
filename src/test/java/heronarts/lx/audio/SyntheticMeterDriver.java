package heronarts.lx.audio;

/**
 * Test helper: feeds synthetic sample windows through a real GraphicMeter
 * (same FFT, band layout and dB normalization the live audio input uses).
 */
public class SyntheticMeterDriver {
    public static final int SAMPLE_RATE = LXAudioComponent.SAMPLE_RATE;
    public static final int BUFFER_SIZE = LXAudioComponent.SAMPLE_BUFFER_SIZE;

    private final LXAudioBuffer buffer = new LXAudioBuffer(BUFFER_SIZE, SAMPLE_RATE);
    public final GraphicMeter meter = new GraphicMeter(buffer);

    public SyntheticMeterDriver(double gainDb, double rangeDb) {
        meter.gain.setValue(gainDb);
        meter.range.setValue(rangeDb);
        meter.start();
    }

    public void push(float[] window, double deltaMs, float[] bandsOut) {
        System.arraycopy(window, 0, buffer.samples, 0, BUFFER_SIZE);
        float ss = 0;
        for (float s : window) ss += s * s;
        buffer.rms = (float) Math.sqrt(ss / BUFFER_SIZE);
        meter.loop(deltaMs);
        for (int i = 0; i < bandsOut.length && i < meter.numBands; i++) {
            bandsOut[i] = meter.getBandf(i);
        }
    }
}
