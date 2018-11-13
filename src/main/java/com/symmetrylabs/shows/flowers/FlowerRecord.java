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

    public int id;
    public String panelId;
    public Harness harness;
    public int harnessIndex;
    public float x;
    public float z;
    public boolean overrideHeight;
    public float yOverride;

    public FlowerRecord(int id, float x, float z) {
        this(id, null, Harness.UNKNOWN, -1, x, z, false, 0);
    }

    public FlowerRecord(
        int id, String panelId, Harness harness, int harnessIndex, float x, float z) {
        this(id, panelId, harness, harnessIndex, x, z, false, 0);
    }

    public FlowerRecord(
        int id, String panelId, Harness harness, int harnessIndex, float x, float z,
        float yOverride) {
        this(id, panelId, harness, harnessIndex, x, z, true, yOverride);
    }

    private FlowerRecord(
        int id, String panelId, Harness harness, int harnessIndex, float x, float z,
        boolean overrideHeight, float yOverride) {

        this.id = id;
        this.panelId = panelId;
        this.harness = harness;
        this.harnessIndex = harnessIndex;
        this.x = x;
        this.z = z;
        this.overrideHeight = overrideHeight;
        this.yOverride = yOverride;
    }

    @Override
    public String toString() {
        if (panelId == null) {
            return String.format("%d unmapped", id);
        }
        if (harnessIndex < 0) {
            return String.format("%d@%s/%s?", id, panelId, harness);
        }
        return String.format("%d@%s/%s%d", id, panelId, harness, harnessIndex);
    }
}
