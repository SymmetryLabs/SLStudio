package com.symmetrylabs.slstudio.pattern.tree;

import heronarts.lx.LX;

import com.symmetrylabs.shows.kalpa.TreeModel;
import static com.symmetrylabs.util.MathUtils.*;


public class TreeMeltOut extends TreeMelt {
    public TreeMeltOut(LX lx) {
        super(lx);
    }

    protected float getDist(TreeModel.Leaf leaf) {
        return 2*abs(leaf.point.yn - 0.5f);
    }
}
