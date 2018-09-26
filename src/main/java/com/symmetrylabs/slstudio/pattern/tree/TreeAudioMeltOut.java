package com.symmetrylabs.slstudio.pattern.tree;

import heronarts.lx.LX;

import com.symmetrylabs.shows.tree.TreeModel;

import static java.lang.StrictMath.abs;

public class TreeAudioMeltOut extends TreeAudioMelt {

    public TreeAudioMeltOut(LX lx) {
        super(lx);
    }

    public float getDist(TreeModel.Leaf leaf) {
        return 2 * abs(leaf.point.yn - 0.5f);
    }

}
