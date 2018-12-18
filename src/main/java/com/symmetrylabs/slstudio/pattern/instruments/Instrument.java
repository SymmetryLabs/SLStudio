package com.symmetrylabs.slstudio.pattern.instruments;

import heronarts.lx.PolyBuffer;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXVector;

/**
 * A visual "instrument" is an object that renders an animation from an
 * array of Notes, and is tunable with a standard set of parameters that
 * control colour, position, and movement.
 */
public interface Instrument {
    void run(LXModel model, ParameterSet paramSet, Note[] notes, double deltaSec, PolyBuffer buffer);
    String getCaption();

    interface ParameterSet {
        long getColor(double variation);
        LXVector getPosition(LXVector variation);
        LXPoint getPoint(LXVector variation);
        double getSize(double variation);
        double getRate();
        LXVector getDirection();

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
