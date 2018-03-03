package com.symmetrylabs.slstudio.ping;

import com.symmetrylabs.util.ParameterUtils;
import heronarts.lx.LX;


public class FlockWaveOoze extends FlockWave {
    public FlockWaveOoze(LX lx) {
        super(lx);

        ParameterUtils.setDiscreteParameter(birdMode, "everywhere");
        parameters.get("spnRate").setValue(2.000);
        parameters.get("detail").setValue(6.600);
        parameters.get("fadeInSec").setValue(1.700);
        parameters.get("fadeOutSec").setValue(0.780);
        parameters.get("maxBirds").setValue(8.000);
        parameters.get("maxSpd").setValue(10.000);
        parameters.get("palBias").setValue(-2.120);
        parameters.get("palCutoff").setValue(0);
        parameters.get("palShift").setValue(0.120);
        parameters.get("palStart").setValue(0.250);
        parameters.get("palStop").setValue(1);
        ParameterUtils.setDiscreteParameter(palette, "sky.orange");
        parameters.get("ripple").setValue(0.400);
        parameters.get("scatter").setValue(150.000);
        parameters.get("size").setValue(560.000);
        parameters.get("spdMult").setValue(0.240);
        parameters.get("spnVary").setValue(0);
        parameters.get("spnRad").setValue(280.000);
        parameters.get("timeScale").setValue(0.150);
        parameters.get("turnSec").setValue(1.560);
        parameters.get("x").setValue(300);
        parameters.get("y").setValue(0.400);
        parameters.get("z").setValue(78.398);
        parameters.get("zScale").setValue(6.300);
    }
}
