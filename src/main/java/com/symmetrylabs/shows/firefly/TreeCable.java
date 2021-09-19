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
    BooleanParameter tracer = new BooleanParameter("tracer", false);
    int currentIndexIn = 0;
    int currentIndexOut = 0;
    int currentIndexTreeIn = 0;
    int currentIndexTreeOut = 0;

    public TreeCable(LX lx) {
        super(lx);
        addParameter("tree", treeNum);
        addParameter("cable", cableNum);
        addParameter("tracer", tracer);
    }

    @Override
    protected void run(double deltaMs) {
        for (LXPoint p : model.points) {
            colors[p.index] = LXColor.rgb(0, 0, 0);
        }
        AnchorTree tree = KaledoscopeModel.anchorTrees.get(treeNum.getValuei());
        if (!tree.p.isButterflyAnchor)
            return;
        if (cableNum.getValuei() != -1) {
            for (int i = 0; i < tree.inCables.length; i++) {
                if (cableNum.getValuei() == i) {
                    if (tree.inCables[i] != null) {
                        int j = 0;
                        for (LXPoint p : tree.inCables[i].points) {
                            if (currentIndexIn >= tree.inCables[i].points.size())
                                currentIndexIn = 0;
                            if (!tracer.getValueb() || tracer.getValueb() && currentIndexIn == j)
                                colors[p.index] = LXColor.rgb(255, 255, 255);
                            j++;
                        }
                    }
                }
            }
            for (int i = 0; i < tree.outCables.length; i++) {
                if (cableNum.getValuei() == i) {
                    int j = 0;
                    if (tree.outCables[i] != null) {
                        if (currentIndexOut >= tree.outCables[i].points.size())
                            currentIndexOut = 0;
                        for (LXPoint p : tree.outCables[i].points) {
                            if (!tracer.getValueb() || tracer.getValueb() && currentIndexOut == j)
                                colors[p.index] = LXColor.rgb(255, 255, 255);
                            j++;
                        }
                    }
                }
            }
            currentIndexIn++;
            currentIndexOut++;
        } else {
            int j = 0;
            if (currentIndexTreeIn >= tree.inPoints.size())
                currentIndexTreeIn = 0;
            if (currentIndexTreeOut >= tree.outPoints.size())
                currentIndexTreeOut = 0;
            for (LXPoint p : tree.inPoints) {
                if (!tracer.getValueb() || tracer.getValueb() && currentIndexTreeIn == j)
                    colors[p.index] = LXColor.rgb(255, 255, 255);
                j++;
            }
            j = 0;
            for (LXPoint p : tree.outPoints) {
                if (!tracer.getValueb() || tracer.getValueb() && currentIndexTreeOut == j)
                    colors[p.index] = LXColor.rgb(255, 255, 255);
                j++;
            }
            currentIndexTreeIn++;
            currentIndexTreeOut++;
        }
    }
}
