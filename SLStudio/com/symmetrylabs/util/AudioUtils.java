package com.symmetrylabs.util;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
static class AudioUtils {

    static final double LOG_2 = Math.log(2);

    static double freqToOctave(double freq) {
        return freqToOctave(freq, 1);
    }

    static double freqToOctave(double freq, double freqRef) {
        return Math.log(Math.max(1, freq / freqRef)) / LOG_2;
    }

}
