package com.symmetrylabs.slstudio.pattern.instruments;

import heronarts.lx.LX;

public class SwimInstrument extends InstrumentPattern {
    public static final String SHOW_NAME = "hhflower";

    public SwimInstrument(LX lx) {
        super(lx, InstrumentRegistry.getInstrument("Swim"));
    }
}
