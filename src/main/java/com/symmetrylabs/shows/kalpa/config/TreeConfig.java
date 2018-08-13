package com.symmetrylabs.shows.kalpa.config;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class TreeConfig {

    private static final Map<String, BranchConfig[]> limbTypes = new HashMap<>();
    private static final Map<String, TwigConfig[]> branchTypes = new HashMap<>();

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

    public static void createLimbType(String id, BranchConfig[] type) {
        limbTypes.put(id, type);
    }

    public static List<String> getLimbTypes() {
        return new ArrayList(limbTypes.keySet());
    }

    public static BranchConfig[] getLimbType(String id) {
        BranchConfig[] configs = limbTypes.get(id);
        BranchConfig[] configsCopy = new BranchConfig[configs.length];
        for (int i = 0; i < configs.length; i++) {
            configsCopy[i] = configs[i].getCopy();
        }
        return configsCopy;
    }

    public static void createBranchType(String id, TwigConfig[] type) {
        branchTypes.put(id, type);
    }

    public static List<String> getBranchTypes() {
        return new ArrayList(branchTypes.keySet());
    }

    public static TwigConfig[] getBranchType(String id) {
        TwigConfig[] configs = branchTypes.get(id);
        TwigConfig[] configsCopy = new TwigConfig[configs.length];
        for (int i = 0; i < configs.length; i++) {
            configsCopy[i] = configs[i].getCopy();
        }
        return configsCopy;
    }
}
