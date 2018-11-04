package com.symmetrylabs.layouts.tree;

import java.util.List;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.EnumParameter;
import heronarts.lx.parameter.DiscreteParameter;

import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import com.symmetrylabs.layouts.tree.*;
import com.symmetrylabs.layouts.tree.ui.*;


public class TreeTwigSelector extends SLPattern<TreeModel> {

    final DiscreteParameter selectedTwig = new DiscreteParameter("select", 1, 9);

    public TreeTwigSelector(LX lx) {
        super(lx);
        addParameter(selectedTwig);
    }

    public void run(double deltaMs) {
        setColors(0);

        for (TreeModel.Branch branch : ((TreeModel)lx.model).getBranches()) {
            TreeModel.Twig twig = branch.getTwigs().get(selectedTwig.getValuei()-1);

            for (LXPoint p : twig.points) {
                colors[p.index] = LXColor.GREEN;
            }
        }
    }
}
