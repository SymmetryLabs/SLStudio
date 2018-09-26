package com.symmetrylabs.slstudio.pattern.tree;

import heronarts.lx.LX;
import heronarts.lx.modulator.DampedParameter;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.parameter.CompoundParameter;

import com.symmetrylabs.shows.tree.TreeModel;
import static com.symmetrylabs.util.MathUtils.*;
import static com.symmetrylabs.util.MathConstants.*;


public abstract class TreeAudioMelt extends TreeBuffer {
    public String getAuthor() {
        return "Mark C. Slee";
    }

    private final float[] multipliers = new float[32];

    public final CompoundParameter melt =
        new CompoundParameter("Melt", 0.5f)
            .setDescription("Amount of melt distortion");

    private final LXModulator meltDamped = startModulator(new DampedParameter(this.melt, 2, 2, 1.5f));
    private LXModulator rot = startModulator(new SawLFO(0, 1, 39000));

    public TreeAudioMelt(LX lx) {
        super(lx);
        addParameter("melt", this.melt);
        for (int i = 0; i < this.multipliers.length; ++i) {
            float r = random(0.6f, 1);
            this.multipliers[i] = r * r * r;
        }
    }

    public void onRun(double deltaMs) {
        float speed = this.speed.getValuef();
        float rot = this.rot.getValuef();
        float melt = this.meltDamped.getValuef();
        for (TreeModel.Leaf leaf : model.getLeaves()) {
            float az = leaf.point.azimuth;
            float maz = (float) (az / TWO_PI + rot) * this.multipliers.length;
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
        return this.lx.engine.audio.meter.getValuef();
    }
}
