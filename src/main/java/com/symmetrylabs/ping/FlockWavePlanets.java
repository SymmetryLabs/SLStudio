package com.symmetrylabs.ping;

import heronarts.lx.LX;


public class FlockWavePlanets extends FlockWave {
    public FlockWavePlanets(LX lx) {
        super(lx);

        parameters.get("everywhere").setValue(1);
        parameters.get("nearBlobs").setValue(0);
        parameters.get("spnRate").setValue(2.000);
        parameters.get("detail").setValue(4.400);
        parameters.get("fadeInSec").setValue(1.700);
        parameters.get("fadeOutSec").setValue(0.780);
        parameters.get("atBlobs").setValue(0);
        parameters.get("maxBirds").setValue(8.000);
        parameters.get("maxSpd").setValue(10.000);
        parameters.get("palBias").setValue(-1.280);
        parameters.get("palCutoff").setValue(0);
        parameters.get("palShift").setValue(0.080);
        parameters.get("palStart").setValue(0);
        parameters.get("palStop").setValue(0.840);
        setPalette("sky.purple");
        parameters.get("ripple").setValue(-3.600);
        parameters.get("scatter").setValue(150.000);
        parameters.get("size").setValue(560.000);
        parameters.get("spdMult").setValue(0);
        parameters.get("spnVary").setValue(0);
        parameters.get("spnRad").setValue(280.000);
        parameters.get("timeScale").setValue(0.150);
        parameters.get("turnSec").setValue(1.560);
        parameters.get("x").setValue(300);
        parameters.get("y").setValue(0.400);
        parameters.get("z").setValue(78.398);
        parameters.get("zScale").setValue(-0.180);
    }
}
