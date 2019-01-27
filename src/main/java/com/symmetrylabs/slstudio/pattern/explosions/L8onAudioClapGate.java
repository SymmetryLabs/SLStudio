package com.symmetrylabs.slstudio.pattern.explosions;

import heronarts.lx.LX;
import heronarts.lx.audio.BandGate;
import heronarts.lx.audio.GraphicMeter;

/**
 * Use this to get a beat gate that has been configured to be very sensitive to
 * the bass beat of the audio input.
 */
public class L8onAudioClapGate extends BandGate {
    final float DEFAULT_GAIN = 7;
    final float DEFAULT_THRESHOLD = .5f;
    final float DEFAULT_FLOOR = .88f;
    final float CLAP_MIN_FREQ = 2200;
    final float CLAP_MAX_FREQ = 2800;

    public L8onAudioClapGate(LX lx) {
        this("Clap", lx);
    }

    public L8onAudioClapGate(String label, LX lx) {
        this(label, lx.engine.audio.meter);
    }

    public L8onAudioClapGate(String label, GraphicMeter meter) {
        super(label, meter);
        this.gain.setValue(DEFAULT_GAIN);
        this.threshold.setValue(DEFAULT_THRESHOLD);
        this.floor.setValue(DEFAULT_FLOOR);

        this.maxFreq.setValue(CLAP_MAX_FREQ);
        this.minFreq.setValue(CLAP_MIN_FREQ);
    }

    public L8onAudioClapGate(GraphicMeter meter, float minHz, float maxHz) {
        this("Clap", meter);
        setFrequencyRange(minHz, maxHz);
    }

    public L8onAudioClapGate(String label, GraphicMeter meter, int minHz, int maxHz) {
        this(label, meter);
        setFrequencyRange(minHz, maxHz);
    }
}
