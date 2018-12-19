package com.symmetrylabs.slstudio.pattern.instruments;

import java.util.ArrayList;
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
    private List<PitchMark> pitchMarks = new ArrayList<>();
    private final Emitter emitter;

    public EmitterInstrument(Emitter emitter) {
        this.emitter = emitter;
    }

    @Override public void run(LXModel model, LXPattern pattern, ParameterSet paramSet, Note[] notes, double deltaSec, PolyBuffer buffer) {
        for (int i = paramSet.getPitchLo(); i <= paramSet.getPitchHi(); i++) {
            if (notes[i].attack) {
                Mark mark = emitter.emit(paramSet, i, notes[i].intensity);
                if (mark != null) {
                    for (LXModulator modulator : mark.getModulators()) {
                        pattern.addModulator(modulator);
                    }
                    pitchMarks.add(new PitchMark(i, mark));
                }
            }
        }

        List<Mark> unexpiredMarks = new ArrayList<>();
        for (PitchMark pitchMark : pitchMarks) {
            if (!pitchMark.mark.isExpired()) {
                unexpiredMarks.add(pitchMark.mark);
            }
        }
        List<Mark> discardedMarks = emitter.discardMarks(unexpiredMarks);

        // Advance and render each Mark.
        List<PitchMark> survivingPitchMarks = new ArrayList<>();
        for (PitchMark pitchMark : pitchMarks) {
            int pitch = pitchMark.pitch;
            Mark mark = pitchMark.mark;
            if (!discardedMarks.contains(mark)) {
                mark.render(model, buffer);
                mark.advance(deltaSec, notes[pitch].intensity, notes[pitch].sustain);
                if (mark.isExpired()) {
                    discardedMarks.add(mark);
                } else {
                    survivingPitchMarks.add(pitchMark);
                }
            }
        }
        pitchMarks = survivingPitchMarks;

        // Clean up any pattern resources used by expired Marks.
        for (Mark mark : discardedMarks) {
            for (LXModulator modulator : mark.getModulators()) {
                modulator.stop();
                pattern.removeModulator(modulator);
            }
        }

        // Global advance and render.
        emitter.run(deltaSec, paramSet);
        emitter.render(model, buffer);
    }

    @Override public String getCaption() {
        return String.format("%s: %d", emitter.getClass().getSimpleName(), pitchMarks.size());
    }

    class PitchMark {
        public final int pitch;
        public final Mark mark;

        public PitchMark(int pitch, Mark mark) {
            this.pitch = pitch;
            this.mark = mark;
        }
    }

    public interface Emitter {
        Mark emit(ParameterSet paramSet, int pitch, double intensity);
        List<Mark> discardMarks(List<Mark> marks);
        void run(double deltaSec, ParameterSet paramSet);
        void render(LXModel model, PolyBuffer buffer);
    }

    public interface Mark {
        void advance(double deltaSec, double intensity, boolean sustain);
        void render(LXModel model, PolyBuffer buffer);
        List<LXModulator> getModulators();
        boolean isExpired();
    }

    public static abstract class AbstractEmitter {
        Random random = new Random();

        public List<Mark> discardMarks(List<Mark> marks) {
            int numToDiscard = Math.max(0, marks.size() - getLimit());
            return marks.subList(0, numToDiscard);
        }

        private int getLimit() {
            return 10;
        }

        public void run(double deltaSec, ParameterSet paramSet) {}

        public void render(LXModel model, PolyBuffer buffer) {}

        // Convenience utilities
        public LXVector randomXyDisc() {
            while (true) {
                float x = random.nextFloat() * 2 - 1;
                float y = random.nextFloat() * 2 - 1;
                if (x*x + y*y < 1) {
                    return new LXVector(x, y, 0);
                }
            }
        }

        public LXVector randomSphere() {
            while (true) {
                float x = random.nextFloat() * 2 - 1;
                float y = random.nextFloat() * 2 - 1;
                float z = random.nextFloat() * 2 - 1;
                if (x*x + y*y + z*z < 1) {
                    return new LXVector(x, y, z);
                }
            }
        }

        public double randomVariation() {
            return random.nextDouble() * 2 - 1;
        }
    }

    public static abstract class AbstractMark {
        public List<LXModulator> getModulators() {
            return new ArrayList<>();
        }
    }
}
