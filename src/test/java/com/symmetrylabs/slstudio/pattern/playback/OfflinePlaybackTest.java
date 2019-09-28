package com.symmetrylabs.slstudio.pattern.playback;

import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.SLModel;
import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import heronarts.lx.model.GridModel;
import heronarts.lx.model.LXModel;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import processing.core.PApplet;

import static org.junit.Assert.assertEquals;

public class OfflinePlaybackTest {

    private LXModel model = new GridModel(10, 10);
    private LX lx = new LX(model);

    @Test
    public void offlinePlaybackTest(){
        //initialize
        MTCPlayback player = new MTCPlayback(lx);

        // goto pet cheetah
        for (int i = 1566000 - 20; i < 1566000 + 300; i++){
            player.goToFrame(i);
            player.run(System.currentTimeMillis(), PolyBuffer.Space.RGB8);
            System.out.println(player.currentSongPng);
            System.out.println(player.currentSongName);
            System.out.println('\n');
        }

        // goto levitate
        for (int i = 108000 + 20; i < 108000 + 300; i++){
            player.goToFrame(i);
            player.run(System.currentTimeMillis(), PolyBuffer.Space.RGB8);
            System.out.println(player.currentSongPng);
            System.out.println(player.currentSongName);
            System.out.println('\n');
        }
    }
}
