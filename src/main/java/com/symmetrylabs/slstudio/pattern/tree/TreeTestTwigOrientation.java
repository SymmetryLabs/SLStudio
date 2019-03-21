package com.symmetrylabs.slstudio.pattern.tree;

import java.util.List;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.DiscreteParameter;

import com.symmetrylabs.shows.tree.*;


public class TreeTestTwigOrientation extends TreePattern {

    private final DiscreteParameter selectedBranch;
    private final BooleanParameter allBranches;

    public TreeTestTwigOrientation(LX lx) {
        super(lx);
        this.selectedBranch = new DiscreteParameter("branch", 0, 0, tree.getBranches().size()-1);
        this.allBranches = new BooleanParameter("all", false);

        addParameter(selectedBranch);
        addParameter(allBranches);
    }

    public void run(double deltaMs) {
        setColors(0);

        if (allBranches.isOn()) {
            for (TreeModel.Branch branch : tree.getBranches()) {
               lightUpBranch(branch);
            }
        } else {
            TreeModel.Branch branch = tree.getBranches().get(selectedBranch.getValuei());
            lightUpBranch(branch);
        }
    }

    private void lightUpBranch(TreeModel.Branch branch) {
        for (TreeModel.Twig twig : branch.getTwigs()) {
            int i = 0;

            for (LXPoint p : twig.points) {
                colors[p.index] = i++ > TreeModel.Twig.NUM_LEDS / 2 ? LXColor.RED : LXColor.BLUE;
            }
        }
    }
}
