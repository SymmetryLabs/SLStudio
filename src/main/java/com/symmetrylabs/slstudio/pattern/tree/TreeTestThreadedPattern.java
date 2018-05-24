package com.symmetrylabs.slstudio.pattern.tree;

import com.symmetrylabs.layouts.oslo.TreeModel;
import com.symmetrylabs.slstudio.pattern.base.TreePattern;
import heronarts.lx.LX;

import java.util.List;

public abstract class TreeTestThreadedPattern extends TreePattern {
    public String getAuthor() {
        return "Mark C. Slee";
    }

    public TreeTestThreadedPattern(LX lx) {
        super(lx);
    }

    public void runThread(List<TreeModel.Branch> branches, double deltaMs) {
        for (TreeModel.Branch branch : branches) {
            for (TreeModel.Leaf leaf : branch.leaves) {
                setColor(leaf, 16); //#ff0000
            }
        }
    }
}
