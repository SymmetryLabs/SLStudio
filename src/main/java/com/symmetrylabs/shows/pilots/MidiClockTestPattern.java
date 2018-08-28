package com.symmetrylabs.shows.pilots;

import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.midi.LXMidiInput;

public class MidiClockTestPattern extends SLPattern<SLModel> {
    int clock = -1;

    public MidiClockTestPattern(LX lx) {
        super(lx);
        for (LXMidiInput input : lx.engine.midi.inputs) {
            input.addBeatListener(newClock -> {
                clock = newClock;
            });
        }
    }

    @Override
    public String getCaption() {
        return String.format("beat clock: %d", clock);
    }
}
