package com.symmetrylabs.layouts.cubes.topology;

import com.symmetrylabs.layouts.cubes.CubesModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;

public abstract class TopologyPattern extends SLPattern<CubesModel> {
    protected CubeTopology topology;

    public TopologyPattern(LX lx) {
        super(lx);
        topology = new CubeTopology(model);
    }
}
