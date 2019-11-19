package com.symmetrylabs.slstudio.pattern.tree.mapping;

import com.symmetrylabs.shows.treeV2.TreeModel_v2;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.DiscreteParameter;

import java.util.List;

import static heronarts.lx.PolyBuffer.Space.SRGB8;

public class LimbSelectah extends SLPattern<SLModel> {

    private final TreeModel_v2 model;
    private final DiscreteParameter selectedLimb;
    private final DiscreteParameter skippy;

    private final BooleanParameter directionalityTwigs;

    public LimbSelectah(LX lx) {
        super(lx);
        this.model = (TreeModel_v2) lx.model;
        this.selectedLimb = new DiscreteParameter("limb", 0, model.limbs2.size());
        this.skippy = new DiscreteParameter("every n pixel", 1, 5);
        this.directionalityTwigs = new BooleanParameter("twig directionality", false);
        addParameter(directionalityTwigs);
        addParameter(selectedLimb);
        addParameter(skippy);
    }

    public void run(double deltaMs, PolyBuffer.Space space) {
        int[] colors = (int[]) getArray(SRGB8);
        setColors(0);

        TreeModel_v2.Limb limb = model.limbs2.get(selectedLimb.getValuei());
        List<TreeModel_v2.Limb> sublimbs = limb.limbs;
        int skip = 0;

        if (directionalityTwigs.isOn()){
            for (TreeModel_v2.Branch branch : limb.branches){
                int bright = 100;
                for (TreeModel_v2.Twig twig : branch.twigs){
                    for (LXPoint p : twig.points){
                        colors[p.index] = LXColor.hsb(120, bright, bright);
                    }
                    bright -= 13;
                }
            }
        }
        else{
            for (LXPoint p : limb.points) {
                int c = palette.getColor();
                colors[p.index] = skip++%skippy.getValuei() == 0 ? c : 0;
            }
        }

        int sublimHueRotate = 0;
        for (TreeModel_v2.Limb l : sublimbs){
            for (LXPoint p : l.points) {
                colors[p.index] = LXColor.hsb(60 + sublimHueRotate * 120, 100, 100);
            }
            sublimHueRotate++;
            if (directionalityTwigs.isOn()){
                for (TreeModel_v2.Branch branch : l.branches){
                    int bright = 100;
                    for (TreeModel_v2.Twig twig : branch.twigs){
                        for (LXPoint p : twig.points){
                            colors[p.index] = LXColor.hsb(60 + sublimHueRotate * 120, bright, bright);
                        }
                        bright -= 13;
                    }
                }
            }
        }

        markModified(SRGB8);
    }
}
