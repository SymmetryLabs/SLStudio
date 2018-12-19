package com.symmetrylabs.slstudio.pattern.instruments;

import java.util.ArrayList;
import java.util.List;

import heronarts.lx.modulator.LXModulator;

public abstract class AttackDecayMark implements Mark {
    protected double amplitude; // rises to 1, sustains, then decays
    protected double attackSec;
    protected double decaySec;
    protected boolean sustaining = true;
    protected List<LXModulator> modulators = new ArrayList<>();

    public AttackDecayMark(double attackSec, double decaySec) {
        amplitude = 0;
        this.attackSec = attackSec;
        this.decaySec = decaySec;
    }

    public void advance(double deltaSec, double intensity, boolean sustain) {
        if (sustain) {
            if (attackSec > 0) {
                amplitude = Math.min(1, amplitude + deltaSec / attackSec);
            } else {
                amplitude = 1;
            }
        } else {
            if (decaySec > 0) {
                amplitude *= Math.pow(0.01, deltaSec / decaySec);
            } else {
                amplitude = 0;
            }
        }
        sustaining = sustain;
    }

    public boolean isExpired() {
        return !sustaining && amplitude < 0.001;
    }

    public List<LXModulator> getModulators() {
        return modulators;
    }

    protected LXModulator addModulator(LXModulator modulator) {
        modulators.add(modulator);
        return modulator;
    }
}
