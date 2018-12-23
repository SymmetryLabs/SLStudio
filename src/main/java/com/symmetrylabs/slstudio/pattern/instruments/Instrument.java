package com.symmetrylabs.slstudio.pattern.instruments;

import heronarts.lx.LXPattern;
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
    void run(LXModel model, LXPattern pattern, ParameterSet paramSet, Note[] notes, double deltaSec, PolyBuffer buffer);
    String getCaption();

    interface ParameterSet {
        double getHue(double variation);
        default double getHue() { return getHue(0); }
        double getHueVar();
        double getSat(double variation);
        default double getSat() { return getSat(0); }
        double getBrt(double variation);
        default double getBrt() { return getBrt(0); }
        long getColor(double hueVariation, double satVariation, double brtVariation);
        default long getColor(double hueV) { return getColor(hueV, 0, 0); }
        default long getColor(double hueV, double ampV) { return getColor(hueV, ampV, ampV); }

        LXVector getPosition(int pitch, LXVector variation);
        LXPoint getPoint(int pitch, LXVector variation);
        default double getSize() { return getSize(0); }
        double getSize(double variation);
        default double getRate() { return getRate(0); }
        double getRate(double variation);
        double getDecaySec();
        double getTwist();
        LXVector getDirection();

        int getPitchLo();
        int getPitchHi();
        double getPitchFraction(double pitch);
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
