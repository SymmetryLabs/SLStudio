package com.symmetrylabs.util.ubnt;

import java.io.IOException;

public class UbntSwitch {

    PortPowerSample sample = new PortPowerSample(24);

    // connect
    UbntTelnetConnection conn;

    public UbntSwitch (String ip){
        try {
            conn = new UbntTelnetConnection(ip);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sample() throws IOException {
        sample = conn.pollPowerSamples();

        System.out.println();
        for (double s : sample.samples){
            System.out.println(s);
        }
    }

    public void powerCycleAllPorts() throws IOException {
        conn.powerCycle();
    }
}
