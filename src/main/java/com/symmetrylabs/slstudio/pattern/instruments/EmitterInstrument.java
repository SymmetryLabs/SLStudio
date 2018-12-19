package com.symmetrylabs.slstudio.pattern.instruments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

import heronarts.lx.LXPattern;
import heronarts.lx.PolyBuffer;
import heronarts.lx.model.LXModel;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.transform.LXVector;

/**
 * An EmitterInstrument is a type of Instrument that emits a Mark every time
 * a note is played.  A Mark is an animation element with a life cycle; it is
 * created when a note is played, sustained while the note is sustained, and
 * can either expire on its own or when the note is released.
 */
public class EmitterInstrument implements Instrument {
    private HashMap<Integer, Mark> pitchMarks = new LinkedHashMap<>();
    private final Emitter emitter;

    public EmitterInstrument(Emitter emitter) {
        this.emitter = emitter;
    }

    @Override public void run(LXModel model, LXPattern pattern, ParameterSet paramSet, Note[] notes, double deltaSec, PolyBuffer buffer) {
        for (int i = paramSet.getPitchLo(); i <= paramSet.getPitchHi(); i++) {
            if (notes[i].attack) {
                addMark(i, emitter.emit(paramSet, i, notes[i].intensity), pattern);
            }
        }

        List<Integer> pitches = new ArrayList<>(pitchMarks.keySet());
        List<Integer> pitchesToDiscard = new ArrayList<>();
        int numToDiscard = pitches.size() - emitter.getMaxCount();
        if (numToDiscard > 0) {
            pitchesToDiscard = pitches.subList(0, numToDiscard);
            pitches = pitches.subList(numToDiscard, pitches.size());
        }

        // Advance and render each Mark.
        for (Integer pitch : pitches) {
            Mark mark = pitchMarks.get(pitch);
            mark.render(model, buffer);
            mark.advance(deltaSec, notes[pitch].intensity, notes[pitch].sustain);
            if (mark.isExpired()) {
                pitchesToDiscard.add(pitch);
            }
        }

        // Remove any expired Marks.
        for (Integer pitch : pitchesToDiscard) {
            removeMark(pitch, pattern);
        }

        // Global advance and render.
        emitter.run(deltaSec, paramSet);
        emitter.render(model, buffer);
    }

    protected void addMark(int pitch, Mark mark, LXPattern pattern) {
        if (mark != null) {
            removeMark(pitch, pattern);
            for (LXModulator modulator : mark.getModulators()) {
                pattern.addModulator(modulator);
            }
            pitchMarks.put(pitch, mark);
        }
    }

    protected void removeMark(int pitch, LXPattern pattern) {
        Mark mark = pitchMarks.get(pitch);
        if (mark != null) {
            for (LXModulator modulator : mark.getModulators()) {
                modulator.stop();
                pattern.removeModulator(modulator);
            }
            pitchMarks.remove(pitch);
        }
    }

    @Override public String getCaption() {
        return String.format("%s: %d", emitter.getClass().getSimpleName(), pitchMarks.size());
    }
}
