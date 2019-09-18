package com.symmetrylabs.shows.empirewall.config;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class VineWallConfig {

    private VineConfig[] vines;

    public VineWallConfig() {
        this(new VineConfig[0]);
    }

    public VineWallConfig(VineConfig[] vines) {
        this.vines = vines;
    }

    public List<VineConfig> getVines() {
        return Collections.unmodifiableList(Arrays.asList(vines));
    }

    public VineConfig[] getVinesArray() {
        return vines;
    }

    public VineConfig getVineAtIndex(int i) {
        return vines[i];
    }
}
