package com.symmetrylabs.shows.flowers;

import heronarts.lx.transform.LXVector;

public class FlowerData {
    public LXVector location;
    public FlowerRecord record;
    /* the height of the flower in the original geometry file */
    public final float geometryHeight;

    public FlowerData(LXVector location) {
        this(null, location);
    }

    public FlowerData(FlowerRecord record, LXVector location) {
        this.record = record;
        this.location = location;
        geometryHeight = location.y;
    }

    public void recalculateLocation() {
        location.y = record.overrideHeight ? record.yOverride : geometryHeight;
    }

    @Override
    public String toString() {
        return String.format(
            "%s [%f %f %f]", record, location.x, location.y, location.z);
    }
}
