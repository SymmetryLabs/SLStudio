package com.symmetrylabs.shows.composite;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

import heronarts.lx.model.LXPoint;
import heronarts.lx.model.LXAbstractFixture;

import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;

public class CompositeModel extends StripsModel<Strip> {

    public CompositeModel() {
        this(new ArrayList<>());
    }

    public CompositeModel(List<Strip> strips) {
        super(new Fixture(strips));
        Fixture fixture = (Fixture) this.fixtures.get(0);

        for (Strip strip : strips) {
            this.strips.add(strip);
            this.stripTable.put(strip.modelId, strip);
        }
    }

    private static class Fixture extends LXAbstractFixture {
        private Fixture(List<Strip> strips) {
            for (Strip strip : strips) {
                if (strip != null) {
                    for (LXPoint point : strip.points) {
                        this.points.add(point);
                    }
                }
            }
        }
    }
}
