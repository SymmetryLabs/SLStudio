package com.symmetrylabs.slstudio.pattern.tree;

import com.symmetrylabs.layouts.oslo.TreeModel;
import heronarts.lx.LX;

public class TreeMeltDown extends TreeMelt {
    public TreeMeltDown(LX lx) {
        super(lx);
    }

    protected float getDist(TreeModel.Leaf leaf) {
        return 1 - leaf.point.yn;
    }
}
