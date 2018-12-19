package com.symmetrylabs.slstudio.pattern.instruments;

import com.symmetrylabs.shows.hhgarden.HHGardenShow;

import heronarts.lx.LX;

public class SparkleInstrument extends InstrumentPattern {
    public static final String GROUP_NAME = HHGardenShow.SHOW_NAME;

    public SparkleInstrument(LX lx) {
        super(lx, InstrumentRegistry.getInstrument("Sparkle"));
    }
}
