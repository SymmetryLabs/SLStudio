package com.symmetrylabs.slstudio.pattern.tree;

import heronarts.lx.LX;
import heronarts.lx.LXUtils;
import heronarts.lx.color.LXColor;
import heronarts.lx.modulator.DampedParameter;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.parameter.CompoundParameter;

import com.symmetrylabs.shows.tree.TreeModel;
import static com.symmetrylabs.util.MathConstants.*;
import static com.symmetrylabs.util.MathUtils.*;


public class TreeLighthouse extends TreeSpinningPattern {
        public String getAuthor() {
            return "Mark C. Slee";
        }

        public final CompoundParameter size = (CompoundParameter)
            new CompoundParameter("Size", HALF_PI, PI/8, TWO_PI)
                .setDescription("Size of lighthouse arc");

        public final CompoundParameter slope = (CompoundParameter)
            new CompoundParameter("Slope", 0, -1, 1)
                .setDescription("Slope of gradient");

        private final LXModulator sizeDamped = startModulator(new DampedParameter(this.size, 3*PI, 4*PI, TWO_PI));
        private final LXModulator slopeDamped = startModulator(new DampedParameter(this.slope, 2, 4, 2));

        public TreeLighthouse(LX lx) {
            super(lx);
            addParameter("size", this.size);
            addParameter("slope", this.slope);
        }

        public void run(double deltaMs) {
            float azimuth = this.azimuth.getValuef();
            float falloff = 100 / this.sizeDamped.getValuef();
            float slope = PI * this.slopeDamped.getValuef();
            for (TreeModel.Leaf leaf : model.getLeaves()) {
                float az = (TWO_PI + leaf.point.azimuth + abs(leaf.point.yn - 0.5f) * slope) % TWO_PI;
                float b = max(0, 100 - falloff * LXUtils.wrapdistf(az, azimuth, TWO_PI));
                setColor(leaf, LXColor.gray(b));
            }
        }
}
