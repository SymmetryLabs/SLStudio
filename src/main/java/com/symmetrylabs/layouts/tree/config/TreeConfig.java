package com.symmetrylabs.layouts.tree.config;

import java.util.Collections;
import java.util.List;
import java.util.Arrays;


public class TreeConfig {

    private LimbConfig[] limbs;

    public TreeConfig() {
        this(new LimbConfig[0]);
    }

    public TreeConfig(LimbConfig[] limbs) {
        this.limbs = limbs;
    }

    public List<LimbConfig> getLimbs() {
        return Collections.unmodifiableList(Arrays.asList(limbs));
    }

    public LimbConfig getLimbAtIndex(int i) {
        return limbs[i];
    }
}