package com.symmetrylabs.ping;

import heronarts.lx.LX;


public class FlockWaveMercury extends FlockWave {
    public FlockWaveMercury(LX lx) {
        super(lx);

        parameters.get("everywhere").setValue(1);
        parameters.get("nearBlobs").setValue(0);
        parameters.get("spnRate").setValue(2.000);
        parameters.get("detail").setValue(10.000);
        parameters.get("fadeInSec").setValue(2.000);
        parameters.get("fadeOutSec").setValue(1.500);
        parameters.get("atBlobs").setValue(0);
        parameters.get("maxBirds").setValue(20.000);
        parameters.get("maxSpd").setValue(0);
        parameters.get("palBias").setValue(0);
        parameters.get("palCutoff").setValue(0);
        parameters.get("palShift").setValue(0);
        parameters.get("palStart").setValue(0);
        parameters.get("palStop").setValue(1);
        setPalette("lake");
        parameters.get("ripple").setValue(0.600);
        parameters.get("scatter").setValue(200.000);
        parameters.get("size").setValue(340.000);
        parameters.get("spdMult").setValue(0);
        parameters.get("spnVary").setValue(0);
        parameters.get("spnRad").setValue(280.000);
        parameters.get("timeScale").setValue(1);
        parameters.get("turnSec").setValue(1.560);
        parameters.get("x").setValue(300);
        parameters.get("y").setValue(0.400);
        parameters.get("z").setValue(78.398);
        parameters.get("zScale").setValue(3.900);
    }
}
