package com.symmetrylabs.util.ubnt;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class UbntSwitchTest {

    UbntSwitch netSwitch = new UbntSwitch("10.200.1.240");


    @Test
    void powerCycle() {
        try {
            netSwitch.powerCycleAllPorts();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Test
    void sample() {
        try {
            for(int i = 0; i < 24; i++){
                netSwitch.sample();
                Thread.sleep(200);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
