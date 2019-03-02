package com.symmetrylabs.slstudio.pattern.tree;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.DiscreteParameter;

import com.symmetrylabs.shows.tree.TreeModel;


public class TreeTwigIndices extends TreePattern {

    private final DiscreteParameter selectedBranch;
    private final BooleanParameter allBranches;
    
    public TreeTwigIndices(LX lx) {
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
        float hue = 0;
        int bi = 0;
        for (TreeModel.Twig twig : branch.getTwigs()) {
            int ti = -1;
            for (TreeModel.Leaf leaf : twig.getLeaves()) {
                if (bi > ti++) {
                    for (LXPoint p : leaf.getPoints()) {
                        colors[p.index] = lx.hsb(hue, 100, 100);
                    }
                }
            }
            bi++;
            hue += 80;
        }
    }
}
