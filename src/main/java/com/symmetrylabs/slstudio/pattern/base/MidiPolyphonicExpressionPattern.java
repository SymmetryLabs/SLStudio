package com.symmetrylabs.slstudio.pattern.base;

import com.symmetrylabs.shows.cubes.CubesModel;
import com.symmetrylabs.slstudio.model.SLModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import heronarts.lx.LX;
import heronarts.lx.midi.MidiAftertouch;
import heronarts.lx.midi.MidiControlChange;
import heronarts.lx.midi.MidiNote;
import heronarts.lx.midi.MidiNoteOn;
import heronarts.lx.midi.MidiPitchBend;

/**
 * A pattern that supports MIDI messages from instruments that follow the
 * MPE standard for polyphonic aftertouch and polyphonic pitch bend, such
 * as the ROLI Seaboard.
 */
public abstract class MidiPolyphonicExpressionPattern<M extends SLModel> extends SLPattern<M> {
    static final int MAX_PITCHES = 128;
    static final int SLIDE_CONTROLLER = 74;  // Seaboard "slide" is sent on controller number 74

    /** The set of pitches currently on and assigned to each channel. */
    protected List<Set<Integer>> pitchChannels = new ArrayList<>();

    protected int[] channels = new int[MAX_PITCHES];
    protected double[] velocities = new double[MAX_PITCHES];
    protected double[] pressures = new double[MAX_PITCHES];
    protected double[] bends = new double[MAX_PITCHES];
    protected double[] slides = new double[MAX_PITCHES];

    /** Pitch is from 0 to 127, velocity is from 0.0 to 1.0. */
    public /* abstract */ void noteOn(int pitch, double velocity) { }

    /** Pitch is from 0 to 127. */
    public /* abstract */ void noteOff(int pitch) { }

    /** Pitch is from 0 to 127, pressure is from 0.0 to 1.0. */
    public /* abstract */ void notePressure(int pitch, double pressure) { }

    /** Pitch is from 0 to 127, bend is from -1.0 to 1.0. */
    public /* abstract */ void noteBend(int pitch, double bend) { }

    /** Pitch is from 0 to 127, slide is from -1.0 to 1.0. */
    public /* abstract */ void noteSlide(int pitch, double slide) { }

    /** Pitch is from 0 to 127, value is from -1.0 to 1.0. */
    public /* abstract */ void noteControl(int pitch, int controller, double value) { }

    public MidiPolyphonicExpressionPattern(LX lx) {
        super(lx);
    }

    private Set<Integer> getChannelPitches(int channel) {
        while (pitchChannels.size() <= channel) {
            pitchChannels.add(new HashSet<Integer>());
        }
        return pitchChannels.get(channel);
    }

    @Override
    public void noteOnReceived(MidiNoteOn noteOn) {
        int channel = noteOn.getChannel() + 1;  // 1 to 16
        int pitch = noteOn.getPitch();
        double velocity = noteOn.getVelocity() / 127.0;
        getChannelPitches(channels[pitch]).remove(pitch);
        getChannelPitches(channel).add(pitch);
        channels[pitch] = channel;
        velocities[pitch] = velocity;
        pressures[pitch] = 0;
        bends[pitch] = 0;
        noteOn(pitch, velocities[pitch]);
    }

    @Override
    public void noteOffReceived(MidiNote note) {
        int pitch = note.getPitch();
        getChannelPitches(channels[pitch]).remove(pitch);
        channels[pitch] = 0;
        velocities[pitch] = 0;
        pressures[pitch] = 0;
        bends[pitch] = 0;
        noteOff(pitch);
    }

    @Override
    public void aftertouchReceived(MidiAftertouch aftertouch) {
        int channel = aftertouch.getChannel() + 1;  // 1 to 16
        double pressure = aftertouch.getAftertouch() / 127.0;
        for (int pitch : getChannelPitches(channel)) {
            pressures[pitch] = pressure;
        }
        for (int pitch : getChannelPitches(channel)) {
            notePressure(pitch, pressure);
        }
    }

    @Override
    public void pitchBendReceived(MidiPitchBend pitchBend) {
        int channel = pitchBend.getChannel() + 1;  // 1 to 16
        double bend = pitchBend.getNormalized();
        for (int pitch : getChannelPitches(channel)) {
            bends[pitch] = bend;
        }
        for (int pitch : getChannelPitches(channel)) {
            noteBend(pitch, bends[pitch]);
        }
    }

    @Override
    public void controlChangeReceived(MidiControlChange controlChange) {
        int channel = controlChange.getChannel() + 1;  // 1 to 16
        int controller = controlChange.getCC();
        double value = controlChange.getValue() / 127.0;
        if (controller == SLIDE_CONTROLLER) {
            for (int pitch : getChannelPitches(channel)) {
                slides[pitch] = value;
            }
            for (int pitch : getChannelPitches(channel)) {
                noteSlide(pitch, value);
            }
        }
        for (int pitch : getChannelPitches(channel)) {
            noteControl(pitch, controller, value);
        }
    }

    @Override
    public String getCaption() {
        String result = "";
        for (int pitch = 0; pitch < MAX_PITCHES; pitch++) {
            if (channels[pitch] > 0) {
                result += "note " + pitch;
                if (velocities[pitch] > 0) result += String.format("; velocity = %4.2f", velocities[pitch]);
                if (pressures[pitch] > 0) result += String.format("; pressure = %4.2f", pressures[pitch]);
                if (bends[pitch] != 0) result += String.format("; bend = %+4.2f", bends[pitch]);
                if (slides[pitch] > 0) result += String.format("; slide = %4.2f", slides[pitch]);
            }
        }
        return result;
    }
}
