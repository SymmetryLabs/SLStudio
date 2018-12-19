package com.symmetrylabs.slstudio.pattern.instruments;

import com.symmetrylabs.shows.hhgarden.HHGardenShow;

import heronarts.lx.LX;

public class RippleInstrument extends InstrumentPattern {
    public static final String GROUP_NAME = HHGardenShow.SHOW_NAME;

    public RippleInstrument(LX lx) {
        super(lx, InstrumentRegistry.getInstrument("Ripple"));
    }
}
