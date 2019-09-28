package com.symmetrylabs.slstudio.pattern.playback;

import java.awt.image.BufferedImage;

public class Hunk {
    BufferedImage hunk;
    int frameStart;

    public Hunk(BufferedImage hunk, int frameStart){
        this.hunk = hunk;
        this.frameStart = frameStart;
    }
}
