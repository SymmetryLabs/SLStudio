package com.symmetrylabs.slstudio.pattern.tree;

import com.symmetrylabs.layouts.oslo.TreeModel;
import heronarts.lx.LX;

public class TreeAudioMeltUp extends TreeAudioMelt {
    public TreeAudioMeltUp(LX lx) {
        super(lx);
    }

    public float getDist(TreeModel.Leaf leaf) {
        return leaf.point.yn;
    }
}
