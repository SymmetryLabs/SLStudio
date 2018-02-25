package com.symmetrylabs.slstudio.model;

import java.util.List;

/*
A single channel of data output.
One contiguous shift register of LEDs.
Each LED has index in the chain.
 */
public class DataChannel {
    public final List<Strip> strips;

    DataChannel(List<Strip> strips){
        this.strips = strips;
    }

    public void addPixels(Strip strip){
        this.strips.add(strip);
    }
}
