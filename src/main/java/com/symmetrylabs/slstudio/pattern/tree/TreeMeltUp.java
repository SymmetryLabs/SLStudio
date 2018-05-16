package com.symmetrylabs.slstudio.pattern.tree;

import com.symmetrylabs.layouts.oslo.TreeModel;
import heronarts.lx.LX;

public class TreeMeltUp extends TreeMelt {

    public TreeMeltUp(LX lx) {
        super(lx);
    }

    protected float getDist(TreeModel.Leaf leaf) {
        return leaf.point.yn;
    }
}
