package com.symmetrylabs.slstudio.pattern.tree;

import heronarts.lx.LX;
import heronarts.lx.modulator.*;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;

import com.symmetrylabs.shows.tree.TreeModel;
import static com.symmetrylabs.util.MathConstants.*;
import static com.symmetrylabs.util.MathUtils.*;


public abstract class TreeMelt extends TreeBuffer {

    private final float[] multipliers = new float[32];

    public final CompoundParameter level =
        new CompoundParameter("Level", 0)
            .setDescription("Level of the melting effect");

    public final BooleanParameter auto =
        new BooleanParameter("Auto", true)
            .setDescription("Automatically make content");

    public final CompoundParameter melt =
        new CompoundParameter("Melt", 0.5f)
            .setDescription("Amount of melt distortion");

    public final BooleanParameter resetOnAuto =
        new BooleanParameter("RstOnAu", false)
        .setDescription("If set, level is reset when auto is turned off");

    private final LXModulator meltDamped = startModulator(new DampedParameter(this.melt, 2, 2, 1.5f));
    private LXModulator rot = startModulator(new SawLFO(0, 1, 39000));
    private LXModulator autoLevel = startModulator(new TriangleLFO(-0.5f, 1, startModulator(new SinLFO(3000, 7000, 19000))));

    public TreeMelt(LX lx) {
        super(lx);
        addParameter("level", this.level);
        addParameter("auto", this.auto);
        addParameter("melt", this.melt);
        addParameter("resetOnAuto", this.resetOnAuto);
        for (int i = 0; i < this.multipliers.length; ++i) {
            float r = random(0.6f, 1);
            this.multipliers[i] = r * r * r;
        }
    }

    @Override
    public void onParameterChanged(LXParameter p) {
        if (p == auto && resetOnAuto.getValueb()) {
            autoLevel.setValue(1.f);
        }
    }

    public void onRun(double deltaMs) {
        float speed = this.speed.getValuef();
        float rot = this.rot.getValuef();
        float melt = this.meltDamped.getValuef();
        for (TreeModel.Leaf leaf : model.getLeaves()) {
            float az = leaf.point.azimuth;
            float maz = (az / TWO_PI + rot) * this.multipliers.length;
            float lerp = maz % 1;
            int floor = (int) (maz - lerp);
            float m = lerp(1, lerp(this.multipliers[floor % this.multipliers.length], this.multipliers[(floor + 1) % this.multipliers.length], lerp), melt);
            float d = getDist(leaf);
            int offset = round(d * speed * m);
            setColor(leaf, this.history[(this.cursor + offset) % this.history.length]);
        }
    }

    protected abstract float getDist(TreeModel.Leaf leaf);

    public float getLevel() {
        if (this.auto.isOn()) {
            float autoLevel = this.autoLevel.getValuef();
            if (autoLevel > 0) {
                return pow(autoLevel, 0.5f);
            }
            return 0;
        }
        return this.level.getValuef();
    }
}
