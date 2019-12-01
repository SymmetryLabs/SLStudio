package com.symmetrylabs.shows.banyan;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.EnumParameter;

import com.symmetrylabs.slstudio.effect.SLEffect;
import com.symmetrylabs.shows.tree.TreeModel;


public class BanyanMask extends SLEffect<BanyanModel> {
 
	public enum Mode {
		TREE, STAR 
	}

	public final EnumParameter<Mode> mode = new EnumParameter<>("mode", Mode.STAR);

	public BanyanMask(LX lx) {
        super(lx);
        addParameter(mode);
    }

    @Override
    public void run(double deltaMs, double amount) {
    	BanyanModel tree = (BanyanModel) model;

    	switch (mode.getEnum()) {
    		case TREE:
    			for (LXPoint p : BanyanModel.star.points) {
    				colors[p.index] = 0;
    			}
    			break;
    		case STAR:
    			for (TreeModel.Branch branch : tree.branches) {
    				for (LXPoint p : branch.points) {
    					colors[p.index] = 0;
    				}
    			}
    			break;
    	}
    }
}
