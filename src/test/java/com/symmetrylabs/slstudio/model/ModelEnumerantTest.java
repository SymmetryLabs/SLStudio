package com.symmetrylabs.slstudio.model;

import com.symmetrylabs.slstudio.server.VolumeServer;
import heronarts.lx.LX;
import org.junit.Test;

import static org.junit.Assert.*;

public class ModelEnumerantTest {

    // Goal is to enumerate 124 model branches.
    @Test
    public void enumerateModel() {
        VolumeServer volumeServer = new VolumeServer();
        volumeServer.start();
        LX lx = volumeServer.core.lx;

        ModelEnumerant modelEnumerant = new ModelEnumerant((SLModel) lx.model);

        modelEnumerant.enumerateMappableFixture();

        int numberBranches = 124;
        assertEquals(124, numberBranches);
    }

    // Next goal is to associate controllers dynamically.


    // Perhaps even display it in the UI.
}
