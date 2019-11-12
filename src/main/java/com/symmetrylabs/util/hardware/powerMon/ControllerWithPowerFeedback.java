package com.symmetrylabs.util.hardware.powerMon;

import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.DiscreteParameter;

import java.io.IOException;

public interface ControllerWithPowerFeedback {

    DiscreteParameter blackoutPowerThreshold = new DiscreteParameter("Blackout", 0, 4095);
    BooleanParameter blackoutRogueLEDsActive = new BooleanParameter("Activate blackout procedure", false);

    void writeSample(MetaSample metaPowerSample);

    // flips port power if above threshold (like a digital breaker)
    void killByThreshHold();

    // kills port power
    void killPortPower();

    MetaSample getLastSample();
}
