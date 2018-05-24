package com.symmetrylabs.slstudio.pattern.base;

import com.symmetrylabs.layouts.oslo.TreeModel;
import heronarts.lx.LX;
import heronarts.lx.LXPattern;

public abstract class TreePattern extends LXPattern {
    protected final TreeModel model;

    public TreePattern(LX lx) {
        super(lx);
        this.model = (TreeModel) lx.model;
    }

    public abstract String getAuthor();

    public void onActive() {
        // TODO: report via OSC to blockchain
    }

    public void onInactive() {
        // TODO: report via OSC to blockchain
    }
}
