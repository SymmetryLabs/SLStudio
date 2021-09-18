package com.symmetrylabs.shows.firefly;

import art.lookingup.AnchorTree;
import art.lookingup.KaledoscopeModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.DiscreteParameter;

import java.util.logging.Logger;

public class TreeCable extends SLPattern {
    public static final String GROUP_NAME = FireflyShow.SHOW_NAME;
    private static final Logger logger = Logger.getLogger(TreeCable.class.getName());

    DiscreteParameter treeNum = new DiscreteParameter("tree", 0, 0, KaledoscopeModel.NUM_ANCHOR_TREES);
    DiscreteParameter cableNum = new DiscreteParameter("cable", -1, -1, 3);

    public TreeCable(LX lx) {
        super(lx);
        addParameter("tree", treeNum);
        addParameter("cable", cableNum);
    }

    @Override
    protected void run(double deltaMs) {
        for (LXPoint p : model.points) {
            colors[p.index] = LXColor.rgb(0, 0, 0);
        }
        AnchorTree tree = KaledoscopeModel.anchorTrees.get(treeNum.getValuei());
        if (!tree.p.isButterflyAnchor)
            return;
        for (int i = 0; i < tree.inCables.length; i++) {
            if (cableNum.getValuei() == -1 || cableNum.getValuei() == i) {
                if (tree.inCables[i] != null) {
                    for (LXPoint p : tree.inCables[i].points) {
                        colors[p.index] = LXColor.rgb(255, 255, 255);
                    }
                }
            }
        }
        for (int i = 0; i < tree.outCables.length; i++) {
            if (cableNum.getValuei() == -1 || cableNum.getValuei() == i) {
                if (tree.outCables[i] != null) {
                    for (LXPoint p : tree.outCables[i].points) {
                        colors[p.index] = LXColor.rgb(255, 255, 255);
                    }
                }
            }
        }
    }
}
