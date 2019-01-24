package com.symmetrylabs.shows.pilots;

import com.symmetrylabs.slstudio.component.HiddenComponent;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.midi.LXMidiInput;
import heronarts.lx.midi.MidiTime;

@HiddenComponent
public class MidiClockTestPattern extends SLPattern<SLModel> {
    MidiTime mt = null;

    public MidiClockTestPattern(LX lx) {
        super(lx);
        for (LXMidiInput input : lx.engine.midi.inputs) {
            input.addTimeListener(new LXMidiInput.MidiTimeListener() {
                @Override
                public void onBeatClockUpdate(int i, double v) {
                }

                @Override
                public void onMTCUpdate(MidiTime midiTime) {
                    mt = midiTime.clone();
                }
            });
        }
    }

    @Override
    public String getCaption() {
        return String.format("midi time: %s", mt == null ? "unknown" : mt.toString());
    }
}
