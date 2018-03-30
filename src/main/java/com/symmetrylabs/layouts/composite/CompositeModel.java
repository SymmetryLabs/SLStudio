package com.symmetrylabs.layouts.composite;

import java.util.List;

import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.model.LXAbstractFixture;

public class CompositeModel extends StripsModel<Strip> {
 
    private final Map<String, Strip> stripTable = new HashMap<>();

    public CompositeModel() {
        this(new ArrayList<>(), new Strip[0]);
    }

    public CompositeModel(List<Strip> strips) {
        super(new Fixture(strips));
        Fixture fixture = (Fixture) this.fixtures.get(0);

        for (Strip strip : strips) {
            this.strips.add(strip);
            this.stripTable.put(strip.id, strip);
        }
    }

    public Strip getStripById(String id) {
        return this.stripTable.get(id);
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