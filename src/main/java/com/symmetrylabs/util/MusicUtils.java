package com.symmetrylabs.util;

import com.google.common.collect.ImmutableMap;

import java.util.EnumMap;

import heronarts.lx.parameter.LXParameter;

public class MusicUtils {
    public enum Key {
        C_FLAT, G_FLAT, D_FLAT, A_FLAT, E_FLAT, B_FLAT, F,
      C,
        G, D, A, E, B, F_SHARP, C_SHARP
    };

    public static final MidiPitchFormatter MIDI_PITCH_FORMATTER = new MidiPitchFormatter();

    public static final int PITCH_CMINUS1 = 0;
    public static final int PITCH_C0 = 12;
    public static final int PITCH_C1 = PITCH_C0 + 12;
    public static final int PITCH_C2 = PITCH_C0 + 2 * 12;
    public static final int PITCH_C3 = PITCH_C0 + 3 * 12;
    public static final int PITCH_C4 = PITCH_C0 + 4 * 12;
    public static final int PITCH_C5 = PITCH_C0 + 5 * 12;
    public static final int PITCH_C6 = PITCH_C0 + 6 * 12;
    public static final int PITCH_C7 = PITCH_C0 + 7 * 12;
    public static final int MAX_PITCH = 127;
    public static final int NUM_PITCHES = MAX_PITCH + 1;

    public static final int CLASS_C = 0;
    public static final int CLASS_C_SHARP = 1;
    public static final int CLASS_D_FLAT = 1;
    public static final int CLASS_D = 2;
    public static final int CLASS_D_SHARP = 3;
    public static final int CLASS_E_FLAT = 3;
    public static final int CLASS_E = 4;
    public static final int CLASS_F = 5;
    public static final int CLASS_F_SHARP = 6;
    public static final int CLASS_G_FLAT = 6;
    public static final int CLASS_G = 7;
    public static final int CLASS_G_SHARP = 8;
    public static final int CLASS_A_FLAT = 8;
    public static final int CLASS_A = 9;
    public static final int CLASS_A_SHARP = 10;
    public static final int CLASS_B_FLAT = 10;
    public static final int CLASS_B = 11;

    public static final String DOUBLE_FLAT = "\ud834\udd2b";
    public static final String FLAT = "\u266d";
    public static final String NATURAL = "\u266e";
    public static final String SHARP = "\u266f";
    public static final String DOUBLE_SHARP = "\ud834\udd2a";

    private static double tunedHertz = 440;
    private static int tunedPitch = PITCH_C4 + CLASS_A;
    private static Key keySignature = Key.C;
    private static boolean useAccidentalSymbols = true;

    private static final EnumMap<Key, String[]> PITCH_CLASS_NAMES = new EnumMap<Key, String[]>(
        ImmutableMap.<Key, String[]>builder()
            .put(Key.C_FLAT, "Cn Db Dn Eb Fb Fn Gb Gn Ab An Bb Cb".split(" "))
            .put(Key.G_FLAT, "Cn Db Dn Eb En F Gb Gn Ab An Bb Cb".split(" "))
            .put(Key.D_FLAT, "C Db Dn Eb En F Gb Gn Ab An Bb Bn".split(" "))
            .put(Key.A_FLAT, "C Db Dn Eb En F F# G Ab An Bb Bn".split(" "))
            .put(Key.E_FLAT, "C C# D Eb En F F# G Ab An Bb Bn".split(" "))
            .put(Key.B_FLAT, "C C# D Eb En F F# G G# A Bb Bn".split(" "))
            .put(Key.F, "C C# D D# E F F# G G# A Bb Bn".split(" "))
            .put(Key.C, "C C# D D# E F F# G G# A A# B".split(" "))
            .put(Key.G, "C C# D D# E Fn F# G G# A A# B".split(" "))
            .put(Key.D, "Cn C# D D# E Fn F# G G# A A# B".split(" "))
            .put(Key.A, "Cn C# D D# E Fn F# Gn G# A A# B".split(" "))
            .put(Key.E, "Cn C# Dn D# E Fn F# Gn G# A A# B".split(" "))
            .put(Key.B, "Cn C# Dn D# E Fn F# Gn G# An A# B".split(" "))
            .put(Key.F_SHARP, "Cn C# Dn D# En E# F# Gn G# An A# B".split(" "))
            .put(Key.C_SHARP, "B# C# Dn D# En E# F# Gn G# An A# Bn".split(" "))
            .build()
    );

    /** A formatter that displays a MIDI note number (61) as a pitch ("C#3"). */
    public static class MidiPitchFormatter implements LXParameter.Formatter {
        @Override public String format(double value) {
            return formatPitch(value);
        }
    }

    public static String formatPitch(int pitch) {
        int octave = Math.floorDiv(pitch - PITCH_C0, 12);
        int pitchClass = Math.floorMod(pitch - PITCH_C0, 12);
        String pitchClassName = PITCH_CLASS_NAMES.get(keySignature)[pitchClass];
        if (useAccidentalSymbols) {
            pitchClassName = pitchClassName.replace("b", FLAT).replace("n", NATURAL).replace("#", SHARP);
        }
        return pitchClassName + octave;
    }

    public static String formatPitch(double value) {
        int pitch = (int) Math.round(value);
        int cents = (int) Math.round((value - pitch)*100);
        String pitchName = formatPitch(pitch);
        if (cents != 0) {
            pitchName += String.format(" %+02d", cents);
        }
        return pitchName;
    }

    public static Key getKeySignature() {
        return keySignature;
    }

    public static void setKeySignature(Key newKeySignature) {
        keySignature = newKeySignature;
    }

    public static double getTunedHertz() {
        return tunedHertz;
    }

    public static int getTunedPitch() {
        return tunedPitch;
    }

    public static void setTuning(double hertz, int pitch) {
        tunedHertz = hertz;
        tunedPitch = pitch;
    }

    public static double pitchToHertz(double pitch) {
        return tunedHertz * Math.pow(2.0, (pitch - tunedPitch) / 12.0);
    }
}
