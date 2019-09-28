package com.symmetrylabs.slstudio.pattern.playback;

import heronarts.lx.LX;
import heronarts.lx.model.GridModel;
import heronarts.lx.model.LXModel;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MTCPlaybackTest {

    //    OfflinePlayback player;
    MTCPlayback player;

    LXModel model = new GridModel(10, 10);
    LX lx;

    @BeforeEach
    public void setUp() {
        lx = new LX(model);
    }

    @Test
    void testSongRetrieval() {
        player = new MTCPlayback(lx);
    }

    @Test
    void onActive() {
        player = new MTCPlayback(lx);
    }

    @Test
    void goToFrame() {
    }

    @Test
    void loadSong() {
    }

    @Test
    void run() {
    }
}
