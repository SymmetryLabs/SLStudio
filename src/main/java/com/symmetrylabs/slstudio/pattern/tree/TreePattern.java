package com.symmetrylabs.slstudio.pattern.tree;

import heronarts.lx.LX;

import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import com.symmetrylabs.shows.tree.*;


public abstract class TreePattern extends SLPattern<TreeModel> {
    public static final String GROUP_NAME = TreeShow.SHOW_NAME;

    protected final TreeModel tree;

    public TreePattern(LX lx) {
        super(lx);
        this.tree = (TreeModel) lx.model;
    }
}
