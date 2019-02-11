package com.symmetrylabs.slstudio.output;

import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.pattern.TimeCodedSlideshow.OfflinePlayback;
import heronarts.lx.LX;
import heronarts.lx.model.GridModel;
import heronarts.lx.model.LXModel;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.Assert.assertEquals;

public class DoubleBufferedOfflineRenderOutputTest {

    LX lx;

    @Before
    public void setUp() {
        SLStudio sl = new SLStudio();
        LXModel model = new GridModel(10, 10);
        lx = new LX(model);
    }

    @Test
    public void testBufferSupply(){
        DoubleBufferedOfflineRenderOutput output;
        output = new DoubleBufferedOfflineRenderOutput(lx);
        output.pOutputDir.setValue("~/000_renders/000_green");
        output.hunkSize.setValue(150);
        output.concurrent.setValue(false);
//        output.doubleBuffer.initialize();
        lx.addOutput(output);
        assertEquals("DoubleBuffer", 0, output.doubleBuffer.getFront().hunkIndex);
        assertEquals("DoubleBuffer", 1, output.doubleBuffer.getBack().hunkIndex);
        for (output.currentFrame = 0; output.currentFrame < 700; output.currentFrame++){
            output.updateBuffers();
        }
    }
}