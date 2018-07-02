package com.symmetrylabs.slstudio.pattern.tree;

import heronarts.lx.LX;

import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import com.symmetrylabs.layouts.tree.TreeModel;


public abstract class TreePattern extends SLPattern<TreeModel> {

    protected final TreeModel tree;

    public TreePattern(LX lx) {
        super(lx);
        this.tree = (TreeModel) lx.model;
    }
}
