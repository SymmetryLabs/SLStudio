package com.symmetrylabs.layouts.tree.config;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;


public class TreeConfig {

    private List<LimbConfig> limbs;

    public TreeConfig() {
        this(new ArrayList<LimbConfig>());
    }

    public TreeConfig(List<LimbConfig> limbs) {
        this.limbs = Collections.unmodifiableList(limbs);
    }

    public List<LimbConfig> getLimbs() {
        return limbs;
    }

    public LimbConfig getLimbAtIndex(int i) {
        return limbs.get(i);
    }
}