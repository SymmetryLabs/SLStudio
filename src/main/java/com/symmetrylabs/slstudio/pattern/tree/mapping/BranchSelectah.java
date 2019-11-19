package com.symmetrylabs.slstudio.pattern.tree.mapping;

import com.symmetrylabs.shows.treeV2.TreeModel_v2;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;

import java.util.List;

import static heronarts.lx.PolyBuffer.Space.SRGB8;

public class BranchSelectah extends SLPattern<SLModel> {

    private final TreeModel_v2 model;
    private final DiscreteParameter selectedBrancy;

    private final CompoundParameter bright = new CompoundParameter("bright", 100);

    public BranchSelectah(LX lx) {
        super(lx);
        this.model = (TreeModel_v2) lx.model;
        this.selectedBrancy = new DiscreteParameter("branch", 0, model.branches.size());
        addParameter(selectedBrancy);
        addParameter(bright);
    }

    public void run(double deltaMs, PolyBuffer.Space space) {
        int[] colors = (int[]) getArray(SRGB8);
        setColors(0);

        TreeModel_v2.Branch branch = model.branches2.get(selectedBrancy.getValuei());

        for (LXPoint p : branch.points){
            colors[p.index] = LXColor.hsb(0, 0, bright.getValue());
        }
        markModified(SRGB8);
    }
}
