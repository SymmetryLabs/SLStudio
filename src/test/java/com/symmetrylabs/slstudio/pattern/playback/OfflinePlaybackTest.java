package com.symmetrylabs.slstudio.pattern.playback;

import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.pattern.playback.MTCPlayback;
import heronarts.lx.LX;
import heronarts.lx.model.GridModel;
import heronarts.lx.model.LXModel;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class OfflinePlaybackTest {

//    OfflinePlayback player;
    MTCPlayback player;

    LXModel model = new GridModel(10, 10);
    LX lx;

    @Before
    public void setUp() {
        lx = new LX(model);
    }

    @Test
    public void offlinePlaybackTest(){
        //initialize
        player = new MTCPlayback(lx);
//        player.renderFile.setValue("/Users/symmetry/symmetrylabs/software/000_RENDERER/0_continuous_30_1min.png");
        player.renderFile.setValue("/Users/symmetry/symmetrylabs/software/000_RENDERER/0_2min_30fps_sss.png");

        player.fillBuffers();




//        assertEquals("DoubleBuffer hunk", 1, player.doubleBuffer.getBack().hunkIndex);
//        player.currentFrame = 146;
    }

    @AfterEach
    void tearDown() {
    }
}
