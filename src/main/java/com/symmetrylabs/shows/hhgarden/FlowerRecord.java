package com.symmetrylabs.shows.hhgarden;

import heronarts.lx.transform.LXVector;

/**
 * Persistent metadata stored for each flower.
 *
 * The xz position here is used as an identifier to match the flower with a
 * point in the geometry file. The actual location of the flower used for model
 * creation is taken from the geometry file, with the one exception of the
 * flower's height: if heightOverrideEnabled is set, we override the height from the
 * geometry file with heightOverride.
 */
public class FlowerRecord {
    public static final int UNKNOWN_HARNESS = 0;
    public static final int UNKNOWN_HARNESS_INDEX = 0;
    public static final int UNKNOWN_PIXLITE_ID = 0;
    public static final int MAX_HARNESS_SIZE = 9;

    public int id;
    public String panelId;
    public int pixliteId;
    public int harness;
    public int harnessIndex;
    public float x;
    public float z;
    public boolean heightOverrideEnabled;
    public float heightOverride;

    public FlowerRecord(int id, float x, float z) {
        this(id, null, UNKNOWN_PIXLITE_ID, UNKNOWN_HARNESS, UNKNOWN_HARNESS_INDEX, x, z, false, 0);
    }

    public FlowerRecord(
        int id, String panelId, int pixliteId, int harness, int harnessIndex, float x, float z) {
        this(id, panelId, pixliteId, harness, harnessIndex, x, z, false, 0);
    }

    private FlowerRecord(
        int id, String panelId, int pixliteId, int harness, int harnessIndex, float x, float z,
        boolean heightOverrideEnabled, float heightOverride) {
        this.id = id;
        this.panelId = panelId;
        this.pixliteId = pixliteId;
        this.harness = harness;
        this.harnessIndex = harnessIndex;
        this.x = x;
        this.z = z;
        this.heightOverrideEnabled = heightOverrideEnabled;
        this.heightOverride = heightOverride;
    }

    @Override
    public String toString() {
        String panelStr =
            panelId == null ? "?" : panelId;
        String pixliteStr =
            pixliteId == UNKNOWN_PIXLITE_ID ? "?" : Integer.toString(pixliteId);
        String harnessStr =
            harness == UNKNOWN_HARNESS_INDEX ? "?" : Integer.toString(harness);
        String harnessIndexStr =
            harnessIndex == UNKNOWN_HARNESS_INDEX ? "?" : Integer.toString(harnessIndex);
        return String.format(
            "%04d@%s:%s/%s/%s", id, panelStr, pixliteStr, harnessStr, harnessIndexStr);
    }
}
