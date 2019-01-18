package com.symmetrylabs.shows.twigtest;

import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;

import java.util.Arrays;

import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;

/**
 * The intent of this pattern is to change the colour of every pixel
 * dramatically on every frame, to make it visually obvious when any
 * pixel falls behind or skips frames.
 */
public class FrameRateTestPattern extends SLPattern<SLModel> {
    // Colors that change dramatically on every frame.
    int[] colorChoices = new int[] {
        0xffff0000,
        0xff00ff00,
        0xff0000ff,
        0xffffff00,
        0xff00ffff,
        0xffff00ff,
        0xff000000,
        0xffffffff
    };
    int colorIndex = 0;

    public FrameRateTestPattern(LX lx) {
        super(lx);
    }

    public void run(double deltaMs, PolyBuffer.Space preferredSpace) {
        int[] buffer = (int[]) getArray(PolyBuffer.Space.RGB8);
        Arrays.fill(buffer, colorChoices[colorIndex]);
        colorIndex = (colorIndex + 1) % colorChoices.length;
        markModified(PolyBuffer.Space.RGB8);
    }
}
