package com.symmetrylabs.shows.pilots;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import heronarts.lx.LX;
import heronarts.lx.midi.LXMidiInput;
import heronarts.lx.midi.MidiTime;

public class TimeCodeLog {
    private static final String FILE_NAME = "timecode.csv";

    Writer w;

    public TimeCodeLog(LX lx) {
        try {
            w = new BufferedWriter(new FileWriter(FILE_NAME));
        } catch (IOException e) {
            System.err.println("couldn't start time code log:");
            e.printStackTrace();
            w = null;
            return;
        }

        for (int i = 0; i < lx.engine.midi.inputs.size(); i++) {
            LXMidiInput input = lx.engine.midi.inputs.get(i);
            final int inputIndex = i;
            input.addTimeListener(new LXMidiInput.MidiTimeListener() {
                @Override
                public void onBeatClockUpdate(int i, double v) {
                }

                @Override
                public void onMTCUpdate(MidiTime midiTime) {
                    if (w != null) {
                        try {
                            w.write(
                                String.format(
                                    "%d,%d,%s\n",
                                    System.nanoTime(),
                                    inputIndex,
                                    midiTime.toString()));
                        } catch (IOException e) {
                            System.err.println("time code log write failed, stopping log:");
                            e.printStackTrace();
                            w = null;
                        }
                    }
                }
            });
        }
    }
}
