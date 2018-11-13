package com.symmetrylabs.shows.flowers;

import heronarts.lx.transform.LXVector;

public class FlowerData {
    public LXVector location;
    public FlowerRecord record;

    public FlowerData(LXVector location) {
        this.record = null;
        this.location = location;
    }

    public FlowerData(FlowerRecord record, LXVector location) {
        this.record = record;
        this.location = location;
    }

    @Override
    public String toString() {
        return String.format(
            "%s [%f %f %f]", record, location.x, location.y, location.z);
    }
}
