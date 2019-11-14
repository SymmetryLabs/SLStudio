package com.symmetrylabs.util.hardware.powerMon;

import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.DiscreteParameter;

import java.io.IOException;

public interface ControllerWithPowerFeedback {

    void writeSample(MetaSample metaPowerSample);

    // flips port power if above threshold (like a digital breaker)
    void killByThreshHold();

    // kills port power
    void killPortPower();

    MetaSample getLastSample();

    void enableBlackoutProcedure(boolean b);

    void setBlackoutThreshhold(int valuei);

    boolean allPortsLessThanThreshholdDuringBlackout(int valuei);

    String getHumanId();
}
