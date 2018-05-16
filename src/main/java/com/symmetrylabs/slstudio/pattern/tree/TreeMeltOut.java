package com.symmetrylabs.slstudio.pattern.tree;

import com.symmetrylabs.layouts.oslo.TreeModel;
import heronarts.lx.LX;

import static java.lang.StrictMath.abs;

public class TreeMeltOut extends TreeMelt {
    public TreeMeltOut(LX lx) {
        super(lx);
    }

    protected float getDist(TreeModel.Leaf leaf) {
        return 2*abs(leaf.point.yn - .5);
    }
}
