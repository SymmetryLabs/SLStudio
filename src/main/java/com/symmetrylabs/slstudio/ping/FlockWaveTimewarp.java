package com.symmetrylabs.slstudio.ping;

import com.symmetrylabs.util.ParameterUtils;
import heronarts.lx.LX;


public class FlockWaveTimewarp extends FlockWave {
    public FlockWaveTimewarp(LX lx) {
        super(lx);

        ParameterUtils.setDiscreteParameter(birdMode, "everywhere");
        parameters.get("spnRate").setValue(2.000);
        parameters.get("detail").setValue(6.600);
        parameters.get("fadeInSec").setValue(1.700);
        parameters.get("fadeOutSec").setValue(0.780);
        parameters.get("maxBirds").setValue(50.000);
        parameters.get("maxSpd").setValue(10.000);
        parameters.get("palBias").setValue(-1.280);
        parameters.get("palCutoff").setValue(0);
        parameters.get("palShift").setValue(0.120);
        parameters.get("palStart").setValue(0.250);
        parameters.get("palStop").setValue(1);
        ParameterUtils.setDiscreteParameter(palette, "sun2");
        parameters.get("ripple").setValue(0.600);
        parameters.get("scatter").setValue(300.000);
        parameters.get("size").setValue(560.000);
        parameters.get("spdMult").setValue(0.240);
        parameters.get("spnVary").setValue(0);
        parameters.get("spnRad").setValue(280.000);
        parameters.get("timeScale").setValue(0.350);
        parameters.get("turnSec").setValue(1.560);
        parameters.get("x").setValue(300);
        parameters.get("y").setValue(0.400);
        parameters.get("z").setValue(78.398);
        parameters.get("zScale").setValue(5.580);
    }
}
