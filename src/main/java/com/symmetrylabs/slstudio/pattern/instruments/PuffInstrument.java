package com.symmetrylabs.slstudio.pattern.instruments;

import heronarts.lx.LX;

public class PuffInstrument extends InstrumentPattern {
    public static final String SHOW_NAME = "hhflower";

    public PuffInstrument(LX lx) {
        super(lx, InstrumentRegistry.getInstrument("Puff"));
    }
}
