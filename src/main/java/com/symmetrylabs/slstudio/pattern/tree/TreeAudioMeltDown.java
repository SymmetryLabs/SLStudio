package com.symmetrylabs.slstudio.pattern.tree;

import heronarts.lx.LX;

import com.symmetrylabs.shows.tree.TreeModel;


public class TreeAudioMeltDown extends TreeAudioMelt {

    public TreeAudioMeltDown(LX lx) {
        super(lx);
    }

    public float getDist(TreeModel.Leaf leaf) {
        return 1 - leaf.point.yn;
    }

}
