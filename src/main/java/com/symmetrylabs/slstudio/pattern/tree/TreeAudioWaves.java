package com.symmetrylabs.slstudio.pattern.tree;

import heronarts.lx.LX;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;


public class TreeAudioWaves extends TreeWave {

    public final BooleanParameter manual =
        new BooleanParameter("Manual", false)
            .setDescription("When true, uses the manual parameter");

    public final CompoundParameter level =
        new CompoundParameter("Level", 0)
            .setDescription("Manual input level");

    public TreeAudioWaves(LX lx) {
        super(lx);
        addParameter("manual", this.manual);
        addParameter("level", this.level);
    }

    protected float getLevel() {
        return this.manual.isOn() ? this.level.getValuef() : this.lx.engine.audio.meter.getValuef();
    }

}
