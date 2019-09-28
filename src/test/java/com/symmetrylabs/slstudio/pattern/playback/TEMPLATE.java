package com.symmetrylabs.slstudio.pattern.playback;

import heronarts.lx.LX;
import heronarts.lx.model.GridModel;
import heronarts.lx.model.LXModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TEMPLATE {

    //    OfflinePlayback player;
    MTCPlayback player;

    LXModel model = new GridModel(10, 10);
    LX lx;

    @BeforeEach
    public void setUp() {
        lx = new LX(model);
    }

    @Test
    void onActive() {
    }

    @Test
    void run() {
    }
}
