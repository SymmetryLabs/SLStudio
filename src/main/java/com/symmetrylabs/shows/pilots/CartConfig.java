package com.symmetrylabs.shows.pilots;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.symmetrylabs.util.FileUtils;

import processing.data.JSONArray;
import processing.data.JSONObject;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class CartConfig {
    public static final String FSL = "FSL";
    public static final String BSL = "BSL";
    public static final String BSCL = "BSCL";
    public static final String BSC = "BSC";
    public static final String BSCR = "BSCR";
    public static final String BSR = "BSR";
    public static final String FSR = "FSR";
    public static final List<String> ids = Arrays.asList(FSL, BSL, BSCL, BSC, BSCR, BSR, FSR);

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
            new CartConfig(FSL, "10.200.1.13"),
            new CartConfig(BSL, "10.200.1.17"),
            new CartConfig(BSCL, "10.200.1.16"),
            new CartConfig(BSC, "10.200.1.15"),
            new CartConfig(BSCR, "10.200.1.14"),
            new CartConfig(BSR, "10.200.1.11"),
            new CartConfig(FSR, "10.200.1.12"),
        };
    }

    public static CartConfig[] readConfigsFromFile() {
//        return FileUtils.readShowJsonIfExists(PilotsShow.IP_CONFIGS_FILENAME, CartConfig[].class);
        return FileUtils.readAbsoluteJsonIfExists(PilotsShow.IP_CONFIGS_FILENAME, CartConfig[].class);
    }
}
