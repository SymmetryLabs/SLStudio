package com.symmetrylabs.util.ubnt;

public class PortPowerSample {
    double samples[];

    public PortPowerSample (int numPorts){
        samples = new double[numPorts];
    }

    public void put (int idx, double sample){
        samples[idx] =  sample;
    }
}
