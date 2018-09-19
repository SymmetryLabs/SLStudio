package com.symmetrylabs.slstudio.pattern.tree;

import heronarts.lx.LX;

import com.symmetrylabs.shows.tree.TreeModel;


public class TreeAudioMeltUp extends TreeAudioMelt {
    public TreeAudioMeltUp(LX lx) {
        super(lx);
    }

    public float getDist(TreeModel.Leaf leaf) {
        return leaf.point.yn;
    }
}
