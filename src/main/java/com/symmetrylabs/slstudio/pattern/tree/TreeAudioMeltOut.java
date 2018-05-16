package com.symmetrylabs.slstudio.pattern.tree;

import com.symmetrylabs.layouts.oslo.TreeModel;
import heronarts.lx.LX;

import static java.lang.StrictMath.abs;

public class TreeAudioMeltOut extends TreeAudioMelt {
    public PatternAudioMeltOut(LX lx) {
        super(lx);
    }

    public float getDist(TreeModel.Leaf leaf) {
        return 2 * abs(leaf.point.yn - .5);
    }

}
