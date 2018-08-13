package com.symmetrylabs.slstudio.pattern.tree;

import heronarts.lx.LX;

import com.symmetrylabs.shows.kalpa.TreeModel;


public class TreeMeltDown extends TreeMelt {
    public TreeMeltDown(LX lx) {
        super(lx);
    }

    protected float getDist(TreeModel.Leaf leaf) {
        return 1 - leaf.point.yn;
    }
}
