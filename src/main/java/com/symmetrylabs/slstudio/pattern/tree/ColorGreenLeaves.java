package com.symmetrylabs.slstudio.pattern.tree;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;

import com.symmetrylabs.shows.tree.TreeModel;


public class ColorGreenLeaves extends TreePattern {
    public String getAuthor() {
        return "Mark C. Slee";
    }

    public final CompoundParameter range = new CompoundParameter("Range", 30, 15, 100);

    public ColorGreenLeaves(LX lx) {
        super(lx);
        addParameter("range", this.range);
    }

    public void run(double deltaMs) {
        float sat = palette.getSaturationf();
        int li = 0;
        int range = (int) this.range.getValuef();
        for (TreeModel.Leaf leaf : tree.leaves) {
            setColor(leaf, LXColor.hsb(140 - li % range, sat, 100));
            ++li;
        }
    }
}
