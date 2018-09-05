package com.symmetrylabs.shows.pilots;

import com.google.common.base.Preconditions;

import java.util.List;

public class CartConfig {
    public static final String FSL = "FSL";
    public static final String BSL = "BSL";
    public static final String BSCL = "BSCL";
    public static final String BSC = "BSC";
    public static final String BSCR = "BSCR";
    public static final String BSR = "BSR";
    public static final String FSR = "FSR";
    public static final List<String> ids = List.of(new String[]{FSL, BSL, BSCL, BSC, BSCR, BSR, FSR});

    public final String modelId;
    public final String address;

    public CartConfig(String modelId, String address) {
        Preconditions.checkArgument(ids.contains(modelId));
        this.modelId = modelId;
        this.address = address;
    }

    @Override
    public String toString() {
        return String.format("%s@%s", modelId, address);
    }

    public static CartConfig[] defaultConfigs() {
        return new CartConfig[]{
            new CartConfig(FSL, "10.200.1.10"),
            new CartConfig(BSL, "10.200.1.11"),
            new CartConfig(BSCL, "10.200.1.12"),
            new CartConfig(BSC, "10.200.1.13"),
            new CartConfig(BSCR, "10.200.1.14"),
            new CartConfig(BSR, "10.200.1.15"),
            new CartConfig(FSR, "10.200.1.16"),
            };
    }
}
