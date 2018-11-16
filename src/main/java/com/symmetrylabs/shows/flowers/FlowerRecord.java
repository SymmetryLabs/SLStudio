package com.symmetrylabs.shows.flowers;

import heronarts.lx.transform.LXVector;

/**
 * Persistent metadata stored for each flower.
 *
 * The xz position here is used as an identifier to match the flower with a
 * point in the geometry file. The actual location of the flower used for model
 * creation is taken from the geometry file, with the one exception of the
 * flower's height: if overrideHeight is set, we override the height from the
 * geometry file with yOverride.
 */
public class FlowerRecord {
    public enum Harness { A, B, UNKNOWN }

    public static final int UNKNOWN_HARNESS_INDEX = -1;
    public static final int UNKNOWN_PIXLITE_ID = 0;

    public int id;
    public String panelId;
    public int pixliteId;
    public Harness harness;
    public int harnessIndex;
    public float x;
    public float z;
    public boolean overrideHeight;
    public float yOverride;

    public FlowerRecord(int id, float x, float z) {
        this(id, null, UNKNOWN_PIXLITE_ID, Harness.UNKNOWN, UNKNOWN_HARNESS_INDEX, x, z, false, 0);
    }

    public FlowerRecord(
        int id, String panelId, int pixliteId, Harness harness, int harnessIndex, float x, float z) {
        this(id, panelId, pixliteId, harness, harnessIndex, x, z, false, 0);
    }

    public FlowerRecord(
        int id, String panelId, int pixliteId, Harness harness, int harnessIndex, float x, float z,
        float yOverride) {
        this(id, panelId, pixliteId, harness, harnessIndex, x, z, true, yOverride);
    }

    private FlowerRecord(
        int id, String panelId, int pixliteId, Harness harness, int harnessIndex, float x, float z,
        boolean overrideHeight, float yOverride) {

        this.id = id;
        this.panelId = panelId;
        this.pixliteId = pixliteId;
        this.harness = harness;
        this.harnessIndex = harnessIndex;
        this.x = x;
        this.z = z;
        this.overrideHeight = overrideHeight;
        this.yOverride = yOverride;
    }

    @Override
    public String toString() {
        String panelStr =
            panelId == null ? "?" : panelId;
        String pixliteStr =
            pixliteId == UNKNOWN_PIXLITE_ID ? "?" : Integer.toString(pixliteId);
        String harnessStr =
            harness == Harness.UNKNOWN ? "?" : harness.toString();
        String harnessIndexStr =
            harnessIndex == UNKNOWN_HARNESS_INDEX ? "?" : Integer.toString(harnessIndex);
        return String.format(
            "%04d@%s:%s/%s/%s", id, panelStr, pixliteStr, harnessStr, harnessIndexStr);
    }
}
