package com.symmetrylabs.shows.firefly;


import art.lookingup.KaledoscopeModel;
import art.lookingup.LUButterfly;
import art.lookingup.LUFlower;
import art.lookingup.ui.ButterfliesConfig;
import art.lookingup.ui.FlowersConfig;
import com.symmetrylabs.shows.firefly.FireflyShow;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.*;

import java.util.logging.Logger;

public class ButterflyPos extends SLPattern {
    public static final String GROUP_NAME = FireflyShow.SHOW_NAME;
    private static final Logger logger = Logger.getLogger(FlowerPos.class.getName());

    DiscreteParameter treeNum = new DiscreteParameter("tree", 0, 0, KaledoscopeModel.NUM_ANCHOR_TREES);
    DiscreteParameter distance = new DiscreteParameter("dist", 0, 0, 240);
    BooleanParameter tracer = new BooleanParameter("tracer", false);
    DiscreteParameter index = new DiscreteParameter("index", -1, -1, 14);
    DiscreteParameter cable = new DiscreteParameter("cable", 0, 0, 3);
    DiscreteParameter vertical = new DiscreteParameter("vert", 0, 0, 24);
    CompoundParameter cDistance = new CompoundParameter("cdist", 0f, 0f, 1000f);

    int currentIndex = 0;
    boolean activated = false;
    boolean disableListener = false;

    public ButterflyPos(LX lx) {
        super(lx);
        addParameter("index", index);
        addParameter("tree", treeNum);
        addParameter("cable", cable);
        addParameter("distance", distance);
        addParameter("vert", vertical);
        addParameter("cDistance", cDistance);
        addParameter("tracer", tracer);
        LXParameterListener posListener = new LXParameterListener() {
            public void onParameterChanged(LXParameter p) {
                // When changing the index, we initialize the azimuth and vertical displacement.  When that
                // happens we don't want to update flower config.
                if (!disableListener)
                    updateButterflyPos();
            }
        };
        LXParameterListener cableListener = new LXParameterListener() {
            public void onParameterChanged(LXParameter p) {
                if (!disableListener) {
                    int whichCable = ((DiscreteParameter) p).getValuei();
                    switchToCable(whichCable);
                }
            }
        };
        cable.addListener(cableListener);
        distance.addListener(posListener);
        LXParameterListener verticalListener = new LXParameterListener() {
            public void onParameterChanged(LXParameter p) {
                if (!disableListener) {
                    updateVertical();
                }
            }
        };
        vertical.addListener(verticalListener);


        index.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter p) {
                int butterflyNum = ((DiscreteParameter)p).getValuei();
                if (butterflyNum == -1)
                    return;

                int[] bc = ButterfliesConfig.getButterflyConfig(butterflyNum);
                disableListener = true;
                treeNum.setValue(bc[0]);
                cable.setValue(bc[1]);
                distance.setValue(bc[2]);
                vertical.setValue(bc[3]);
                LUButterfly butterfly = KaledoscopeModel.allButterflies.get(butterflyNum);
                cDistance.setValue(butterfly.cableDistance);
                disableListener = false;
            }
        });
    }

    /**
     * Moves a butterfly to a new cable.
     */
    public void switchToCable(int whichCable) {
        if (index.getValuei() == -1)
            return;
        LUButterfly butterfly = KaledoscopeModel.allButterflies.get(index.getValuei());
        int[] bConfig = ButterfliesConfig.getButterflyConfig(butterfly.runIndex);
        KaledoscopeModel.Cable oldCable = butterfly.cable;
        oldCable.removeButterfly(butterfly);
        KaledoscopeModel.Cable cable = butterfly.cable.startTree.outCables[whichCable];
        LUButterfly prevButterfly = butterfly.findPreviousButterflyOnTargetCable(whichCable);
        cable.insertButterflyAfterButterfly(butterfly, prevButterfly);
        bConfig[1] = whichCable;
        ButterfliesConfig.setButterflyConfig(butterfly.runIndex, bConfig[0], bConfig[1], bConfig[2], bConfig[3]);
        lx.model.update(true, true);
    }

    /**
     * Changes the vertical height of a butterfly
     */
    public void updateVertical() {
        if (index.getValuei() == -1)
            return;
        LUButterfly butterfly = KaledoscopeModel.allButterflies.get(index.getValuei());
        float oldY = butterfly.y;
        int[] bConfig =  ButterfliesConfig.getButterflyConfig(butterfly.runIndex);
        float oldVertical = bConfig[3];
        // Back up to the cable and then back down.
        float newY = oldY + oldVertical - vertical.getValuef();
        butterfly.updatePosition3D(butterfly.x, newY, butterfly.z);
        bConfig[3] = vertical.getValuei();
        ButterfliesConfig.setButterflyConfig(butterfly.runIndex, bConfig[0], bConfig[1], bConfig[2], bConfig[3]);
        lx.model.update(true, true);
    }

    /**
     * This method should be called whenever the azimuth or vertical knobs are updated.
     * // TODO(tracy): Maybe we shouldn't save on every update.
     */
    public void updateButterflyPos() {
        // Don't bother updating flower positions when the pattern initializes.  Wait until it is active.
        // Shouldn't happen because defaults are set to no specific flower selected but if you saved a show
        // file with this pattern it would re-position the selected flower which probably won't make a difference
        // but we don't want random .lxp files messing with the flower configs.
        if (!activated)
            return;
        int butterflyNum = index.getValuei();
        if (butterflyNum < 0 || butterflyNum >= KaledoscopeModel.allButterflies.size())
            return;

        LUButterfly butterfly = KaledoscopeModel.allButterflies.get(butterflyNum);
        int[] bc = ButterfliesConfig.getButterflyConfig(butterflyNum);
        int oldDistance = bc[2];
        butterfly.cable.updateDistance(butterfly, oldDistance, distance.getValuei());
        lx.model.update(true, true);

        //flower.updatePosition(flowerConfig);
        //ButterfliesConfig.setButterflyConfig(butterflyNum, treeNum, cable.getValuei(),  inches,  vertical)
        //FlowersConfig.saveUpdatedFlowerConfigs();
        //lx.model.update(true, true);
    }

    @Override
    public void onActive() {
        activated = true;
        index.setRange(-1, KaledoscopeModel.allButterflies.size());
    }

    @Override
    protected void run(double deltaMs) {
        for (LXPoint p : model.points) {
            colors[p.index] = LXColor.rgb(0, 0, 0);
        }

        int i = 0;
        for (LUButterfly butterfly : KaledoscopeModel.allButterflies) {
            if (index.getValuei() == i || index.getValuei() == -1)
                butterfly.setColor(colors, LXColor.rgb(255, 255, 255));
            else
                butterfly.setColor(colors, LXColor.rgb(0, 0, 0));
            i++;
        }
    }
}
