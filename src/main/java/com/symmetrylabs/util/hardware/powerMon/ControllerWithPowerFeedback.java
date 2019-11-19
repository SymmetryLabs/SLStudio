package com.symmetrylabs.util.hardware.powerMon;

import heronarts.lx.parameter.BooleanParameter;

public interface ControllerWithPowerFeedback {

    void writeSample(MetaSample metaPowerSample);

    // flips port power if above threshold (like a digital breaker)
    void killByThreshHold();

    // kills port power
    void writePortPowerMaskToController();

    void setPortPowerMask(byte mask);

    MetaSample getLastSample();

    void enableBlackoutProcedure(boolean b);

    void setBlackoutThreshhold(int valuei);

    boolean allPortsLessThanThreshholdDuringBlackout(int valuei);

    String getHumanId();

    BooleanParameter[] getPwrMaskParams();

    void enableAllPorts();
}
