package com.symmetrylabs.shows.icicles;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.model.LXAbstractFixture;
import heronarts.lx.transform.LXTransform;

import static com.symmetrylabs.util.MathUtils.*;
import static com.symmetrylabs.util.DistanceConstants.*;
import static com.symmetrylabs.util.MathConstants.*;

public class IciclesModel extends StripsModel<Strip> {

    protected final List<Icicle> icicles = new ArrayList<>();
    protected final Map<String, Icicle> icicleTable = new HashMap<>();
    protected final Icicle[] iciclesArr;

    public IciclesModel() {
        this(new ArrayList<>());
    }

    public IciclesModel(List<Icicle> icicles) {
        super(new Fixture(icicles));
        Fixture fixture = (Fixture) this.fixtures.get(0);

        this.iciclesArr = new Icicle[icicles.size()];
        for (int i = 0; i < icicles.size(); i++) {
            iciclesArr[i] = icicles.get(i);
        }

        for (Icicle icicle : icicles) {
            this.icicles.add(icicle);
            this.icicleTable.put(icicle.id, icicle);
            this.strips.addAll(icicle.getStrips());
        }
    }

    public Icicle getIcicleByRawIndex(int index) {
        return iciclesArr[index];
    }

    public Icicle getIcicleById(String id) {
        return this.icicleTable.get(id);
    }

    private static class Fixture extends LXAbstractFixture {
        private Fixture(List<Icicle> icicles) {
            for (Icicle icicle : icicles) {
                if (icicle != null) {
                    for (LXPoint point : icicle.points) {
                        this.points.add(point);
                    }
                }
            }
        }
    }
}
