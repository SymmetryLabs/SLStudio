package com.symmetrylabs.util.dmx;

public class DMXDataSnapshot {

    public final int[] data;
    public final int length;

    public DMXDataSnapshot(int[] data) {
        this.data = data;
        this.length = data.length;
    }
}
