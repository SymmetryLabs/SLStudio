package com.symmetrylabs.slstudio.pattern.instruments;

import com.symmetrylabs.shows.hhgarden.HHGardenShow;

import heronarts.lx.LX;

public class PuffInstrument extends InstrumentPattern {
    public static final String GROUP_NAME = HHGardenShow.SHOW_NAME;

    public PuffInstrument(LX lx) {
        super(lx, InstrumentRegistry.getInstrument("Puff"));
    }
}
