package com.symmetrylabs.slstudio.pattern.TimeCodedSlideshow;

import com.symmetrylabs.slstudio.SLStudio;
import heronarts.lx.LX;
import heronarts.lx.model.LXModel;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;

import static org.junit.Assert.assertEquals;

public class OfflinePlaybackTest {

    OfflinePlayback player;

    LX lx;

    @Before
    public void setUp() {
        SLStudio sl = new SLStudio();

//        Pilots_v2_Show show = new Pilots_v2_Show();
//        SLModel m = show.buildModel();
        LXModel m = new LXModel();
        lx = new LX(m);
    }

    @Test
    public void offlinePlaybackTest(){
        //initialize
        player = new OfflinePlayback(lx);
        player.directory.setValue("~/000_renders/000_levitate");
        player.doubleBuffer.initialize();
        // now double buffer should contain 0.png and 1.png
        assertEquals("DoubleBuffer hunk", 0, player.doubleBuffer.getFront().hunkIndex);
        assertEquals("DoubleBuffer hunk", 1, player.doubleBuffer.getBack().hunkIndex);

        // test jump forward
        player.currentFrame = 146;
        // this should clear out the double buffer and load hunk 4.png and 5.png
        player.updateBuffers();

        assertEquals("DoubleBuffer hunk", 4, player.doubleBuffer.getFront().hunkIndex);
        assertEquals("DoubleBuffer hunk", 5, player.doubleBuffer.getBack().hunkIndex);

        while (player.currentFrame < 155){
            player.currentFrame++;
            player.updateBuffers();
        }
        assertEquals("DoubleBuffer hunk", 5, player.doubleBuffer.getFront().hunkIndex);
        assertEquals("DoubleBuffer hunk", 6, player.doubleBuffer.getBack().hunkIndex);
    }

    @AfterEach
    void tearDown() {
    }
}
