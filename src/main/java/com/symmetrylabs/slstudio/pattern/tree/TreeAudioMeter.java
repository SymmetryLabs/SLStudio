package com.symmetrylabs.slstudio.pattern.tree;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;

import com.symmetrylabs.shows.tree.TreeModel;
import static com.symmetrylabs.util.MathUtils.*;


public class TreeAudioMeter extends TreePattern {


    public String getAuthor() {
        return "Mark C. Slee";
    }

    public final CompoundParameter mode =
        new CompoundParameter("Mode", 0)
            .setDescription("Sets the mode of the equalizer");

    public final CompoundParameter size =
        new CompoundParameter("Size", 0.2f, 0.1f, 0.4f)
            .setDescription("Sets the size of the display");

    public TreeAudioMeter(LX lx) {
        super(lx);
        addParameter("mode", this.mode);
        addParameter("size", this.size);
    }

    public void run(double deltaMs) {
        float meter = lx.engine.audio.meter.getValuef();
        float mode = this.mode.getValuef();
        float falloff = 100 / this.size.getValuef();
        for (TreeModel.Leaf leaf : model.getLeaves()) {
            float leafPos = 2 * abs(leaf.point.yn - 0.5f);
            float b1 = constrain(50 - falloff * (leafPos - meter), 0, 100);
            float b2 = constrain(50 - falloff * abs(leafPos - meter), 0, 100);
            setColor(leaf, LXColor.gray(lerp(b1, b2, mode)));
        }
    }
}
