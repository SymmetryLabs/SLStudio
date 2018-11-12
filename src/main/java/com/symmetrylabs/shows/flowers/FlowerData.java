package com.symmetrylabs.shows.flowers;

import heronarts.lx.transform.LXVector;

public class FlowerData {
    public enum Harness { A, B }

    public final String id;
    public final String panelId;
    public final Harness harness;
    public final int harnessIndex;
    public final LXVector pos;

    public FlowerData(
        String id, String panelId, Harness harness, int harnessIndex, LXVector pos) {
        this.id = id;
        this.panelId = panelId;
        this.harness = harness;
        this.harnessIndex = harnessIndex;
        this.pos = pos;
    }

    @Override
    public String toString() {
        return String.format("%s/%s%d", panelId, harness, harnessIndex);
    }
}
