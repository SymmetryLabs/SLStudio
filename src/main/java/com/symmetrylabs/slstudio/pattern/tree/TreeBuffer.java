package com.symmetrylabs.slstudio.pattern.tree;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.modulator.DampedParameter;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.parameter.CompoundParameter;

public class TreeBuffer extends TreePattern {

    public String getAuthor() {
        return "Mark C. Slee";
    }

    public final CompoundParameter speedRaw = (CompoundParameter)
        new CompoundParameter("Speed", 256, 2048, 64)
            .setExponent(.5)
            .setDescription("Speed of the wave propagation");

    public final LXModulator speed = startModulator(new DampedParameter(speedRaw, 256, 512));

    private static final int BUFFER_SIZE = 4096;
    protected int[] history = new int[BUFFER_SIZE];
    protected int cursor = 0;

    public BufferPattern(LX lx) {
        super(lx);
        addParameter("speed", this.speedRaw);
        for (int i = 0; i < this.history.length; ++i) {
            this.history[i] = #000000;
        }
    }

    public final void run(double deltaMs) {
        // Add to history
        if (--this.cursor < 0) {
            this.cursor = this.history.length - 1;
        }
        this.history[this.cursor] = getColor();
        onRun(deltaMs);
    }

    protected int getColor() {
        return LXColor.gray(100 * getLevel());
    }

    protected float getLevel() {
        return 0;
    }

    abstract void onRun(double deltaMs);

}
