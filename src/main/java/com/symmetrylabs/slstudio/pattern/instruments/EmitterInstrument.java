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
    private HashMap<Integer, Mark> lastAttack = new HashMap<>();
    private HashMap<MarkKey, Mark> pitchMarks = new LinkedHashMap<>();
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

        List<MarkKey> keys = new ArrayList<>(pitchMarks.keySet());
        List<MarkKey> keysToDiscard = new ArrayList<>();
        int numToDiscard = keys.size() - emitter.getMaxCount();
        if (numToDiscard > 0) {
            keysToDiscard.addAll(keys.subList(0, numToDiscard));
            keys = keys.subList(numToDiscard, keys.size());
        }

        // Advance and render each Mark.
        for (MarkKey key : keys) {
            Mark mark = pitchMarks.get(key);
            mark.render(model, buffer);
            if (mark == lastAttack.get(key.pitch)) {
                mark.advance(deltaSec, notes[key.pitch].intensity, notes[key.pitch].sustain);
            } else {
                mark.advance(deltaSec, 0, false);
            }
            if (mark.isExpired()) {
                keysToDiscard.add(key);
            }
        }

        // Remove any expired Marks.
        for (MarkKey key : keysToDiscard) {
            removeMark(key, pattern);
        }

        // Global advance and render.
        emitter.run(deltaSec, paramSet);
        emitter.render(model, buffer);
    }

    /** An integer with its own distinct identity. */
    class MarkKey {
        int pitch;
        public MarkKey(int pitch) {
            this.pitch = pitch;
        }
    }

    protected void addMark(int pitch, Mark mark, LXPattern pattern) {
        if (mark != null) {
            for (LXModulator modulator : mark.getModulators()) {
                pattern.addModulator(modulator);
            }
            pitchMarks.put(new MarkKey(pitch), mark);
            lastAttack.put(pitch, mark);
        }
    }

    protected void removeMark(MarkKey key, LXPattern pattern) {
        Mark mark = pitchMarks.get(key);
        if (mark != null) {
            for (LXModulator modulator : mark.getModulators()) {
                modulator.stop();
                pattern.removeModulator(modulator);
            }
            pitchMarks.remove(key);
            if (lastAttack.get(key.pitch) == mark) {
                lastAttack.remove(key.pitch);
            }
        }
    }

    @Override public String getCaption() {
        return String.format("%s: %d", emitter.getClass().getSimpleName(), pitchMarks.size());
    }
}
