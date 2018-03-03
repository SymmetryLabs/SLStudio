package com.symmetrylabs.slstudio.ping;

import com.symmetrylabs.util.ParameterUtils;
import heronarts.lx.LX;


public class FlockWaveMercury extends FlockWave {
    public FlockWaveMercury(LX lx) {
        super(lx);

        ParameterUtils.setDiscreteParameter(birdMode, "everywhere");
        parameters.get("spnRate").setValue(2.000);
        parameters.get("detail").setValue(10.000);
        parameters.get("fadeInSec").setValue(2.000);
        parameters.get("fadeOutSec").setValue(1.500);
        parameters.get("maxBirds").setValue(50.000);
        parameters.get("maxSpd").setValue(0);
        parameters.get("palBias").setValue(0);
        parameters.get("palCutoff").setValue(0);
        parameters.get("palShift").setValue(0);
        parameters.get("palStart").setValue(0);
        parameters.get("palStop").setValue(1);
        ParameterUtils.setDiscreteParameter(palette, "lake");
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
