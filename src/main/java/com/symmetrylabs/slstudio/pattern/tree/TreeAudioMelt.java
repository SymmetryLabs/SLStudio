package com.symmetrylabs.slstudio.pattern.tree;

import com.symmetrylabs.layouts.oslo.TreeModel;
import heronarts.lx.LX;
import heronarts.lx.modulator.DampedParameter;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.parameter.CompoundParameter;

import static com.symmetrylabs.util.MathUtils.random;
import static com.symmetrylabs.util.MathUtils.round;
import static heronarts.lx.LX.TWO_PI;
import static heronarts.lx.LXUtils.lerp;

public abstract class TreeAudioMelt extends TreeBuffer {
    public String getAuthor() {
        return "Mark C. Slee";
    }

    private final float[] multipliers = new float[32];

    public final CompoundParameter melt =
        new CompoundParameter("Melt", .5)
            .setDescription("Amount of melt distortion");

    private final LXModulator meltDamped = startModulator(new DampedParameter(this.melt, 2, 2, 1.5));
    private LXModulator rot = startModulator(new SawLFO(0, 1, 39000));

    public TreeAudioMelt(LX lx) {
        super(lx);
        addParameter("melt", this.melt);
        for (int i = 0; i < this.multipliers.length; ++i) {
            float r = random(.6f, 1f);
            this.multipliers[i] = r * r * r;
        }
    }

    public void onRun(double deltaMs) {
        float speed = this.speed.getValuef();
        float rot = this.rot.getValuef();
        float melt = this.meltDamped.getValuef();
        for (TreeModel.Leaf leaf : model.leaves) {
            float az = leaf.point.azimuth;
            float maz = (float) (az / TWO_PI + rot) * this.multipliers.length;
            float lerp = maz % 1;
            int floor = (int) (maz - lerp);
            float m = ((float) lerp(1f, lerp(this.multipliers[floor % this.multipliers.length], this.multipliers[(floor + 1) % this.multipliers.length], lerp), melt));
            float d = getDist(leaf);
            int offset = round(d * speed * m);
            setColor(leaf, this.history[(this.cursor + offset) % this.history.length]);
        }
    }

    protected abstract float getDist(TreeModel.Leaf leaf);

    public float getLevel() {
        return this.lx.engine.audio.meter.getValuef();
    }
}
