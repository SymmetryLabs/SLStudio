package com.symmetrylabs.slstudio.pattern.tree;

import com.symmetrylabs.layouts.oslo.TreeModel;
import heronarts.lx.LX;

public class TreeAudioMeltDown extends TreeAudioMelt {

    public TreeAudioMeltDown(LX lx) {
        super(lx);
    }

    public float getDist(TreeModel.Leaf leaf) {
        return 1 - leaf.point.yn;
    }

}
