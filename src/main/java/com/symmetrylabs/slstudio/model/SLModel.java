package com.symmetrylabs.slstudio.model;

import java.util.List;

import heronarts.lx.model.LXFixture;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;

public abstract class SLModel extends LXModel {

        public SLModel(List<LXPoint> points) {
                super(points);
        }

        public SLModel(LXFixture fixture) {
                super(fixture);
        }

        public SLModel(LXFixture[] fixtures) {
                super(fixtures);
        }

}
