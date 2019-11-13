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
import heronarts.lx.parameter.LXParameter;

import java.util.ArrayList;
import java.util.List;

import static heronarts.lx.PolyBuffer.Space.SRGB8;

public class LimbSelectah extends SLPattern<SLModel> {

    private final TreeModel model;
    private final DiscreteParameter selectedLimb;
    private final DiscreteParameter skippy;

    public LimbSelectah(LX lx) {
        super(lx);
        this.model = (TreeModel) lx.model;
        this.selectedLimb = new DiscreteParameter("limb", 0, model.limbs.size());
        this.skippy = new DiscreteParameter("every n pixel", 1, 5);
        addParameter(selectedLimb);
        addParameter(skippy);
    }

    public void run(double deltaMs, PolyBuffer.Space space) {
        int[] colors = (int[]) getArray(SRGB8);
        setColors(0);

        TreeModel.Limb limb = model.limbs.get(selectedLimb.getValuei());
        List<TreeModel.Limb> sublimbs = limb.limbs;
        int skip = 0;
        for (LXPoint p : limb.points) {
            int c = palette.getColor();
            colors[p.index] = skip++%skippy.getValuei() == 0 ? c : 0;
        }

        int sublimHueRotate = 0;
        for (TreeModel.Limb l : sublimbs){
            for (LXPoint p : l.points) {
                colors[p.index] = LXColor.hsb(60 + sublimHueRotate * 120, 100, 100);
            }
            sublimHueRotate++;
        }

        markModified(SRGB8);
    }
}
