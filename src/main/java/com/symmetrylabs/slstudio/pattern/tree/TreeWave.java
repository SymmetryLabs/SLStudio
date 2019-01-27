package com.symmetrylabs.slstudio.pattern.tree;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.modulator.DampedParameter;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.modulator.TriangleLFO;
import heronarts.lx.parameter.CompoundParameter;

import com.symmetrylabs.shows.tree.TreeModel;
import static com.symmetrylabs.util.MathUtils.*;


public class TreeWave extends TreeBuffer {

    public static final int NUM_MODES = 5;
    private final float[] dm = new float[NUM_MODES];

    public final CompoundParameter mode =
        new CompoundParameter("Mode", 0, NUM_MODES - 1)
            .setDescription("Mode of the wave motion");

    private final LXModulator modeDamped = startModulator(new DampedParameter(this.mode, 1, 8));
    private LXModulator autoLevel = startModulator(new TriangleLFO(-0.5f, 1, startModulator(new SinLFO(3000, 7000, 19000))));

    public TreeWave(LX lx) {
        super(lx);
        addParameter("mode", this.mode);
    }

    public void onRun(double deltaMs) {
        float speed = this.speed.getValuef();
        float mode = this.modeDamped.getValuef();
        float lerp = mode % 1;
        int floor = (int) (mode - lerp);
        for (TreeModel.Leaf leaf : model.leaves) {
            dm[0] = abs(leaf.point.yn - 0.5f);
            dm[1] = 0.5f * abs(leaf.point.xn - 0.5f) + 0.5f * abs(leaf.point.yn - 0.5f);
            dm[2] = abs(leaf.point.xn - 0.5f);
            dm[3] = leaf.point.yn;
            dm[4] = 1 - leaf.point.yn;

            int offset1 = round(dm[floor] * dm[floor] * speed);
            int offset2 = round(dm[(floor + 1) % dm.length] * dm[(floor + 1) % dm.length] * speed);
            int c1 = this.history[(this.cursor + offset1) % this.history.length];
            int c2 = this.history[(this.cursor + offset2) % this.history.length];
            setColor(leaf, LXColor.lerp(c1, c2, lerp));
        }
    }

    protected float getLevel() {
        float autoLevel = this.autoLevel.getValuef();
        if (autoLevel > 0) {
            return pow(autoLevel, 0.5f);
        }
        return 0;
    }
}
