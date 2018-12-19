package com.symmetrylabs.slstudio.pattern.instruments;

import com.symmetrylabs.shows.hhgarden.HHGardenShow;

import heronarts.lx.LX;

public class JetInstrument extends InstrumentPattern {
    public static final String GROUP_NAME = HHGardenShow.SHOW_NAME;

    public JetInstrument(LX lx) {
        super(lx, InstrumentRegistry.getInstrument("Jet"));
    }
}
