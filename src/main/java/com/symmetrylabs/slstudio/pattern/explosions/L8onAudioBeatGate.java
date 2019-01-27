package com.symmetrylabs.slstudio.pattern.explosions;

import heronarts.lx.LX;
import heronarts.lx.audio.BandGate;
import heronarts.lx.audio.GraphicMeter;

/**
 * Use this to get a beat gate that has been configured to be very sensitive to
 * the bass beat of the audio input.
 */
public class L8onAudioBeatGate extends BandGate {
    final float DEFAULT_GAIN = 7f;
    final float DEFAULT_THRESHOLD = .5f;
    final float DEFAULT_FLOOR = .88f;

    public L8onAudioBeatGate(LX lx) {
        this("Beat", lx);
    }

    public L8onAudioBeatGate(String label, LX lx) {
        this(label, lx.engine.audio.meter);
    }

    public L8onAudioBeatGate(String label, GraphicMeter meter) {
        super(label, meter);
        this.gain.setValue(DEFAULT_GAIN);
        this.threshold.setValue(DEFAULT_THRESHOLD);
        this.floor.setValue(DEFAULT_FLOOR);
    }

    public L8onAudioBeatGate(GraphicMeter meter, float minHz, float maxHz) {
        this("Beat", meter);
        setFrequencyRange(minHz, maxHz);
    }

    public L8onAudioBeatGate(String label, GraphicMeter meter, int minHz, int maxHz) {
        this(label, meter);
        setFrequencyRange(minHz, maxHz);
    }
}
