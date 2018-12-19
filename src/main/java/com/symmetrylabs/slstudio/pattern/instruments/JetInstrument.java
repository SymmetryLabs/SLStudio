package com.symmetrylabs.slstudio.pattern.instruments;

import heronarts.lx.LX;

public class JetInstrument extends InstrumentPattern {
    public static final String SHOW_NAME = "hhflower";

    public JetInstrument(LX lx) {
        super(lx, InstrumentRegistry.getInstrument("Jet"));
    }
}
