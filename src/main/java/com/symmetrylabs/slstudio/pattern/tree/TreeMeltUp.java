package com.symmetrylabs.slstudio.pattern.tree;

import heronarts.lx.LX;

import com.symmetrylabs.shows.tree.TreeModel;


public class TreeMeltUp extends TreeMelt {
    public TreeMeltUp(LX lx) {
        super(lx);
    }

    protected float getDist(TreeModel.Leaf leaf) {
        return leaf.point.yn;
    }
}
