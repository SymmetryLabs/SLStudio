package com.symmetrylabs.slstudio.pattern.instruments;

import heronarts.lx.PolyBuffer;
import heronarts.lx.model.LXModel;
import heronarts.lx.transform.LXVector;

/**
 * A visual "instrument" is an object that renders an animation from an
 * array of Notes, and is tunable with a standard set of parameters that
 * control colour, position, and movement.
 */
public interface Instrument {
    void run(LXModel model, ParameterSet paramSet, Note[] notes, double deltaSec, PolyBuffer buffer);

    interface ParameterSet {
        long generateColor(double variation);
        LXVector generatePosition(LXVector variation);

        double getSize();
        double getRate();
        double getOrient();

        int getPitchLo();
        int getPitchHi();
    }

    /** A Note represents the state of a musical note. */
    class Note {
        public boolean attack;  // true if the note has just been triggered
        public boolean sustain;  // true if the note is being sustained
        public double intensity;  // velocity, pressure, or amplitude

        public Note(boolean attack, boolean sustain, double intensity) {
                this.attack = attack;
                this.sustain = sustain;
                this.intensity = intensity;
        }

        public void copyFrom(Note other) {
            attack = other.attack;
            sustain = other.sustain;
            intensity = other.intensity;
        }
    }
}
