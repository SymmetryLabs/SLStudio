package com.symmetrylabs.util.ubnt;

import java.io.IOException;

public class UbntSwitch {

    private static class PortPowerSample {
        double samples[] = new double[24];
    }

    PortPowerSample sample = new PortPowerSample();

    // connect
    UbntTelnetConnection conn;

    public UbntSwitch (String ip){
        try {
            conn = new UbntTelnetConnection(ip);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
