package com.symmetrylabs.shows.empirewall.config;

import java.util.Collections;
import java.util.List;
import java.util.Arrays;

import static com.symmetrylabs.util.DistanceConstants.*;


public class VineConfig {
    public String id;
    public LeafConfig[] leaves;

    public VineConfig(String id, LeafConfig[] leaves) {
        this.id = id;
        this.leaves = leaves;
    }

    public List<LeafConfig> getLeaves() {
        return Collections.unmodifiableList(Arrays.asList(leaves));
    }

    public LeafConfig getLeafAtIndex(int i) {
        return leaves[i];
    }
}
