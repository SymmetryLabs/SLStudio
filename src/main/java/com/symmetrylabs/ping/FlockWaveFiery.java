package com.symmetrylabs.ping;

import heronarts.lx.LX;


public class FlockWaveFiery extends FlockWave {
    public FlockWaveFiery(LX lx) {
        super(lx);

        parameters.get("everywhere").setValue(1);
        parameters.get("nearBlobs").setValue(0);
        parameters.get("spnRate").setValue(2.000);
        parameters.get("detail").setValue(6.600);
        parameters.get("fadeInSec").setValue(1.700);
        parameters.get("fadeOutSec").setValue(0.780);
        parameters.get("atBlobs").setValue(0);
        parameters.get("maxBirds").setValue(8.000);
        parameters.get("maxSpd").setValue(10.000);
        parameters.get("palBias").setValue(-1.640);
        parameters.get("palCutoff").setValue(0.210);
        parameters.get("palShift").setValue(0);
        parameters.get("palStart").setValue(0.250);
        parameters.get("palStop").setValue(1);
        setPalette("sky.red");
        parameters.get("ripple").setValue(0.600);
        parameters.get("scatter").setValue(150.000);
        parameters.get("size").setValue(500.000);
        parameters.get("spdMult").setValue(0.240);
        parameters.get("spnVary").setValue(0);
        parameters.get("spnRad").setValue(280.000);
        parameters.get("timeScale").setValue(0.150);
        parameters.get("turnSec").setValue(1.560);
        parameters.get("x").setValue(300);
        parameters.get("y").setValue(0.400);
        parameters.get("z").setValue(78.398);
        parameters.get("zScale").setValue(5.580);
    }
}
