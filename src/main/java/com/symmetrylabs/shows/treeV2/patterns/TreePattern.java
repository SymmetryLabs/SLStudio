package com.symmetrylabs.shows.treeV2.patterns;

import heronarts.lx.LX;

import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import com.symmetrylabs.shows.treeV2.TreeModel;
import com.symmetrylabs.shows.treeV2.TreeShow;


public abstract class TreePattern extends SLPattern<TreeModel> {
    public static final String GROUP_NAME = TreeShow.SHOW_NAME;

    protected final TreeModel tree;

    public TreePattern(LX lx) {
        super(lx);
        this.tree = (TreeModel) lx.model;
    }
}
