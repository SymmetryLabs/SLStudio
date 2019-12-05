package com.symmetrylabs.slstudio.cue;

import com.symmetrylabs.slstudio.server.VolumeServer;
import heronarts.lx.LX;
import heronarts.lx.parameter.BoundedParameter;
import org.junit.Test;

public class TriggerVezerCueTest {

    @Test
    public void executeTask() {
        System.out.println("hi");

        VolumeServer volumeServer = new VolumeServer();
        volumeServer.start();
        LX lx = volumeServer.core.lx;
        lx.engine.start();
        lx.engine.osc.transmitActive.setValue(true);

        TriggerVezerCue tc = new TriggerVezerCue(lx, new BoundedParameter("null")); // just a hack to ignore this arg.
        tc.triggerVezerShow();
        lx.engine.run();

    }
}
