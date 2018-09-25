package com.symmetrylabs.shows.streetlamp;

import heronarts.lx.LX;

public class Unilines extends FramePattern {
    public static final String GROUP_NAME = StreetlampShow.SHOW_NAME;

    public Unilines(LX lx) {
        super(lx);
    }

    @Override
    protected int[][] getFrames() {
        return new int[][] {
            { 0, 8, 9, 13, 14, 15, 23, 24, 28, 29 },
            { 1, 7, 10, 12, 16, 22, 25, 27 },
            { 2, 6, 11, 17, 21, 26 },
            { 3, 5, 18, 20 },
            { 4, 19 },
            {}
        };
    }
}
