package com.symmetrylabs.shows.firefly;

import art.lookingup.KaledoscopeModel;
import art.lookingup.LUButterfly;
import art.lookingup.LUFlower;
import art.lookingup.ui.FlowersConfig;
import com.symmetrylabs.shows.firefly.FireflyShow;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.*;

import java.util.logging.Logger;

public class FlowerPos extends SLPattern {
    public static final String GROUP_NAME = FireflyShow.SHOW_NAME;
    private static final Logger logger = Logger.getLogger(FlowerPos.class.getName());

    DiscreteParameter treeNum = new DiscreteParameter("tree", 0, 0, KaledoscopeModel.NUM_ANCHOR_TREES);
    DiscreteParameter runNum = new DiscreteParameter("run", 0, 0, 2);
    BooleanParameter tracer = new BooleanParameter("tracer", false);
    DiscreteParameter index = new DiscreteParameter("index", -1, -1, 14);
    CompoundParameter azimuth = new CompoundParameter("az", 0f, 0f, 360f);
    CompoundParameter vertical = new CompoundParameter("vert", 0f, 0f, 240f);

    int currentIndex = 0;
    boolean activated = false;
    boolean disableListener = false;

    public FlowerPos(LX lx) {
        super(lx);
        addParameter("tree", treeNum);
        addParameter("run", runNum);
        addParameter("index", index);
        addParameter("az", azimuth);
        addParameter("vert", vertical);
        addParameter("tracer", tracer);
        LXParameterListener posListener = new LXParameterListener() {
            public void onParameterChanged(LXParameter p) {
                // When changing the index, we initialize the azimuth and vertical displacement.  When that
                // happens we don't want to update flower config.
                if (!disableListener)
                    updateFlowerPos();
            }
        };
        azimuth.addListener(posListener);
        vertical.addListener(posListener);

        index.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter p) {
                int flowerNum = ((DiscreteParameter)p).getValuei();
                if (flowerNum == -1)
                    return;
                LUFlower.FlowerConfig fc = FlowersConfig.getFlowerConfig(treeNum.getValuei(), runNum.getValuei(), flowerNum);
                disableListener = true;
                azimuth.setValue(fc.azimuth);
                vertical.setValue(fc.verticalDisplacement);
                disableListener = false;
            }
        });
    }

    /**
     * This method should be called whenever the azimuth or vertical knobs are updated.
     * // TODO(tracy): Maybe we shouldn't save on every update.
     */
    public void updateFlowerPos() {
        // Don't bother updating flower positions when the pattern initializes.  Wait until it is active.
        // Shouldn't happen because defaults are set to no specific flower selected but if you saved a show
        // file with this pattern it would re-position the selected flower which probably won't make a difference
        // but we don't want random .lxp files messing with the flower configs.
        if (!activated)
            return;
        int tree = treeNum.getValuei();
        int run = runNum.getValuei();
        int flowerNum = index.getValuei();
        if (run >= KaledoscopeModel.anchorTrees.get(tree).flowerRuns.size())
            return;
        if (flowerNum < 0 || flowerNum >= KaledoscopeModel.anchorTrees.get(tree).flowerRuns.get(run).flowers.size())
            return;
        LUFlower.FlowerConfig flowerConfig = FlowersConfig.getFlowerConfig(tree, run, flowerNum);
        flowerConfig.azimuth = (int)(azimuth.getValuef());
        flowerConfig.verticalDisplacement = (int)(vertical.getValuef());
        FlowersConfig.setFlowerConfig(tree, run, flowerNum, flowerConfig);
        LUFlower flower = KaledoscopeModel.anchorTrees.get(tree).flowerRuns.get(run).flowers.get(flowerNum);
        flower.updatePosition(flowerConfig);
        FlowersConfig.saveUpdatedFlowerConfigs();
        lx.model.update(true, true);
    }

    @Override
    public void onActive() {
        activated = true;
    }

    @Override
    protected void run(double deltaMs) {
        for (LXPoint p : model.points) {
            colors[p.index] = LXColor.rgb(0, 0, 0);
        }

        // select flowers
        if (runNum.getValuei() >= KaledoscopeModel.anchorTrees.get(treeNum.getValuei()).flowerRuns.size())
            return;
        KaledoscopeModel.Run run = KaledoscopeModel.anchorTrees.get(treeNum.getValuei()).flowerRuns.get(runNum.getValuei());
        if (tracer.getValueb()) {
            for (int i = 0; i < run.flowers.size(); i++) {
                if (currentIndex == i)
                    run.flowers.get(i).setColor(colors, LXColor.rgb(255, 255, 255));
            }
            currentIndex = (currentIndex + 1) % run.flowers.size();
        } else {
            int i = 0;
            for (LUFlower flower : run.flowers) {
                if (index.getValuei() == i || index.getValuei() == -1)
                    flower.setColor(colors, LXColor.rgb(255, 255, 255));
                else
                    flower.setColor(colors, LXColor.rgb(0, 0, 0));
                i++;
            }
        }
    }
}
