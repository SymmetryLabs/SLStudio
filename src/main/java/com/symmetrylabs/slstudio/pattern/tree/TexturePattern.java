package com.symmetrylabs.slstudio.pattern.tree;

import heronarts.lx.LX;

import com.symmetrylabs.shows.tree.TreeModel;
import heronarts.lx.parameter.BooleanParameter;

public abstract class TexturePattern extends TreePattern {

    public final BooleanParameter rotate =
        new BooleanParameter("Rotate", false)
        .setDescription("Rotates texture masks between elements");

    public TexturePattern(LX lx) {
        super(lx);
        addParameter(rotate);
    }

    protected void setLeafMask(int[] leafMask) {
        int offset = 0;
        boolean rot = rotate.getValueb();
        for (TreeModel.Leaf leaf : model.leaves) {
            for (int i = 0; i < TreeModel.Leaf.NUM_LEDS; ++i) {
                colors[leaf.point.index + i] = leafMask[(i + offset) % leafMask.length];
            }
            if (rot) {
                offset++;
            }
        }
    }

    protected void setTwigMask(int[] twigMask) {
        int offset = 0;
        boolean rot = rotate.getValueb();
        for (TreeModel.Twig twig : model.getTwigs()) {
            for (int i = 0; i < twig.points.length; ++i) {
                colors[twig.points[i].index] = twigMask[(i + offset) % twigMask.length];
            }
            if (rot) {
                offset++;
            }
        }
    }

    protected void setBranchMask(int[] branchMask) {
        int offset = 0;
        boolean rot = rotate.getValueb();
        for (TreeModel.Branch branch : model.getBranches()) {
            for (int i = 0; i < branch.points.length; ++i) {
                colors[branch.points[i].index] = branchMask[(i + offset) % branchMask.length];
            }
            if (rot) {
                offset++;
            }
        }
    }
}
