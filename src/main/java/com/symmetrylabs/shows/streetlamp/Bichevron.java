package com.symmetrylabs.shows.streetlamp;

import heronarts.lx.LX;

public class Bichevron extends FramePattern {
    public static final String GROUP_NAME = StreetlampShow.SHOW_NAME;

    public Bichevron(LX lx) {
        super(lx);
    }

    @Override
    protected int[][] getFrames() {
        return new int[][] {
            { 0, 1, 2, 3, 4, 5, 11, 12, 14, 15, 16, 17, 18, 19, 20, 26, 27, 29 },
            { 8, 7, 6, 10, 13, 23, 22, 21, 25, 28 },
            { 9, 24 },
            { 8, 7, 6, 10, 13, 23, 22, 21, 25, 28 },
        };
    }
}
