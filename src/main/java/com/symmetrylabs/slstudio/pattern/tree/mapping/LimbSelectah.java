package com.symmetrylabs.slstudio.pattern.tree.mapping;

import com.symmetrylabs.shows.treeV2.TreeModel;
import com.symmetrylabs.shows.treeV2.patterns.TreePattern;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.DiscreteParameter;

import static heronarts.lx.PolyBuffer.Space.SRGB8;

public class LimbSelectah extends SLPattern<SLModel> {

    private final TreeModel model;
    private final DiscreteParameter selectedLimb;
    private final DiscreteParameter skippy;

    public LimbSelectah(LX lx) {
        super(lx);
        this.model = (TreeModel) lx.model;
        this.selectedLimb = new DiscreteParameter("limb", 1, 1, model.limbs.size()+1);
        this.skippy = new DiscreteParameter("every n pixel", 1, 5);
        addParameter(selectedLimb);
        addParameter(skippy);
    }

    public void run(double deltaMs, PolyBuffer.Space space) {
        int[] colors = (int[]) getArray(SRGB8);
        setColors(0);

        TreeModel.Limb limb = model.limbs.get(selectedLimb.getValuei()-1);
        int skip = 0;
        for (LXPoint p : limb.points) {
            int c = palette.getColor();
            colors[p.index] = skip++%skippy.getValuei() == 0 ? c : 0;
        }
        markModified(SRGB8);
    }
}
