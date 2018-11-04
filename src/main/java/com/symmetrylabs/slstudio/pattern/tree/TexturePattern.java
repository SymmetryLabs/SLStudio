package com.symmetrylabs.slstudio.pattern.tree;

import heronarts.lx.LX;

import com.symmetrylabs.layouts.tree.TreeModel;


public abstract class TexturePattern extends TreePattern {
    public TexturePattern(LX lx) {
        super(lx);
    }
            
    protected void setLeafMask(int[] leafMask) { 
        for (TreeModel.Leaf leaf : model.leaves) {
            for (int i = 0; i < TreeModel.Leaf.NUM_LEDS; ++i) {
                colors[leaf.point.index + i] = leafMask[i];
            }
        }
    }
    
    protected void setTwigMask(int[] twigMask) {
        for (TreeModel.Twig twig : model.getTwigs()) {
            for (int i = 0; i < twig.points.length; ++i) {
                colors[twig.points[i].index] = twigMask[i];
            }
        }
    }
    
    protected void setBranchMask(int[] branchMask) {
        for (TreeModel.Branch branch : model.getBranches()) {
            for (int i = 0; i < branch.points.length; ++i) {
                colors[branch.points[i].index] = branchMask[i];
            }
        }
    }
}