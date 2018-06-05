package com.symmetrylabs.layouts.tree.config;

private class TreeConfig {

    private List<LimbConfig> limbs;

    public TreeConfig() {
        this(new Arraylist<LimbConfig>());
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