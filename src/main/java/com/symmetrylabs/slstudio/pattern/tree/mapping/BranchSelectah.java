package com.symmetrylabs.slstudio.pattern.tree.mapping;

import com.symmetrylabs.shows.tree.TreeModel;
import com.symmetrylabs.shows.treeV2.TreeModel_v2;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;

import java.util.List;

import static heronarts.lx.PolyBuffer.Space.SRGB8;

public class BranchSelectah extends SLPattern<SLModel> {

    private final TreeModel model;
    private final DiscreteParameter selectedBrancy;

    private final CompoundParameter bright = new CompoundParameter("bright", 0, 100);
    private final CompoundParameter pulseFrequency = new CompoundParameter("pulseFrequency", 50, 500);

    final SinLFO x = new SinLFO(0, 100, 200);

    public BranchSelectah(LX lx) {
        super(lx);
        this.model = (TreeModel) lx.model;
        this.selectedBrancy = new DiscreteParameter("branch", 0, model.branches.size());
        addParameter(selectedBrancy);
        addParameter(pulseFrequency);
        addParameter(bright);
        addModulator(x).trigger();
    }

    public void onParameterChanged(LXParameter p) {
        if (p == pulseFrequency) {
            x.setPeriod(p.getValuef());
        }
    }

    public void run(double deltaMs, PolyBuffer.Space space) {
        int[] colors = (int[]) getArray(SRGB8);
        setColors(0);

        TreeModel.Branch branch = model.branches.get(selectedBrancy.getValuei());

        for (LXPoint p : branch.points){
            colors[p.index] = LXColor.hsb(0, 0, x.getValuef() * bright.getNormalizedf());
//            colors[p.index] = LXColor.rgb(0, 0, x.getValuef() * bright.getNormalizedf());
        }
        markModified(SRGB8);
    }
}
