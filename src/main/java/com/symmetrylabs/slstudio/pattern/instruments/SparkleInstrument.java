package com.symmetrylabs.slstudio.pattern.instruments;

import heronarts.lx.LX;

public class SparkleInstrument extends InstrumentPattern {
    public static final String SHOW_NAME = "hhflower";

    public SparkleInstrument(LX lx) {
        super(lx, InstrumentRegistry.getInstrument("Sparkle"));
    }
}
