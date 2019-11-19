package com.symmetrylabs.shows.treeV2.patterns;

import com.symmetrylabs.shows.treeV2.TreeModel_v2;
import heronarts.lx.LX;

import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import com.symmetrylabs.shows.treeV2.TreeShow;


public abstract class TreePattern extends SLPattern<TreeModel_v2> {
    public static final String GROUP_NAME = TreeShow.SHOW_NAME;

    protected final TreeModel_v2 tree;

    public TreePattern(LX lx) {
        super(lx);
        this.tree = (TreeModel_v2) lx.model;
    }
}
