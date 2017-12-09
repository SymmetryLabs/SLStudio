package com.symmetrylabs.slstudio.ping;

import heronarts.lx.LX;


public class FlockWaveBlues extends FlockWave {
    public FlockWaveBlues(LX lx) {
        super(lx);

        parameters.get("everywhere").setValue(1);
        parameters.get("nearBlobs").setValue(0);
        parameters.get("spnRate").setValue(2.000);
        parameters.get("detail").setValue(2.000);
        parameters.get("fadeInSec").setValue(1.700);
        parameters.get("fadeOutSec").setValue(0.780);
        parameters.get("atBlobs").setValue(0);
        parameters.get("maxBirds").setValue(8.000);
        parameters.get("maxSpd").setValue(10.000);
        parameters.get("palBias").setValue(-0.800);
        parameters.get("palCutoff").setValue(0);
        parameters.get("palShift").setValue(0);
        parameters.get("palStart").setValue(0);
        parameters.get("palStop").setValue(1);
        setPalette("jupiter1");
        parameters.get("ripple").setValue(0.600);
        parameters.get("scatter").setValue(300.000);
        parameters.get("size").setValue(620.000);
        parameters.get("spdMult").setValue(0.240);
        parameters.get("spnVary").setValue(0);
        parameters.get("spnRad").setValue(280.000);
        parameters.get("timeScale").setValue(0.170);
        parameters.get("turnSec").setValue(1.560);
        parameters.get("x").setValue(300);
        parameters.get("y").setValue(0.400);
        parameters.get("z").setValue(78.398);
        parameters.get("zScale").setValue(0.540);
    }
}
