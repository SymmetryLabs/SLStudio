package art.lookingup.ui;

import art.lookingup.*;
import com.symmetrylabs.slstudio.SLStudioLX;
import heronarts.lx.LX;
import heronarts.lx.parameter.LXParameter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

/**
 * Allow position specification for each flower.
 */
public class FlowersConfig extends UIConfig {
    private static final Logger logger = Logger.getLogger(FlowersConfig.class.getName());
    public static final String title = "flowers";
    public static final String filename = "flowers.json";
    public LX lx;
    private boolean parameterChanged = false;
    static public final int MAX_FLOWERS_PER_RUN = 14;
    static public ParameterFile flowersParamFile;

    public FlowersConfig(final SLStudioLX.UI ui, LX lx, ParameterFile paramFile) {
        super(ui, title, filename, paramFile);
        int contentWidth = (int)ui.leftPane.global.getContentWidth();
        this.lx = lx;

        for (int i = 0; i < KaledoscopeModel.NUM_ANCHOR_TREES; i++) {
            // 2 separate strands of flowers per tree
            for (int j = 0; j < 2; j++) {
                // Maximum 15 flowers per strand
                for (int k = 0; k < MAX_FLOWERS_PER_RUN; k++) {
                    registerStringParameter("F." + i + "." + j + "." + k, "");
                }
            }
        }
        save();
        buildUI(ui);
    }

    /**
     * FlowerPos pattern will use this method to update flower positions.
     * @param treeNum
     * @param treeRunNum
     * @param flowerNum
     * @param flowerConfig
     */
    static public void setFlowerConfig(int treeNum, int treeRunNum, int flowerNum, LUFlower.FlowerConfig flowerConfig) {
        String flowerAddress = "F." + treeNum + "." + treeRunNum + "." + flowerNum;
        if (flowersParamFile == null) {
            flowersParamFile = ParameterFile.instantiateAndLoad(filename);
        }
        String val = flowerConfig.azimuth + "," + flowerConfig.verticalDisplacement;
        flowersParamFile.getStringParameter(flowerAddress, "").setValue(val);
    }

    static public LUFlower.FlowerConfig getFlowerConfig(int treeNum, int treeRunNum, int flowerNum) {
        String flowerAddress = "F." + treeNum + "." + treeRunNum + "." + flowerNum;
        if (flowersParamFile == null) {
            flowersParamFile = ParameterFile.instantiateAndLoad(filename);
        }

        float azimuth = 360f * (float)Math.random();
        int verticalDisplacement = ThreadLocalRandom.current().nextInt(33);
        String val = flowersParamFile.getStringParameter(flowerAddress, "" + (int)azimuth + "," + verticalDisplacement).getString();
        String[] posVals = val.split(",");
        LUFlower.FlowerConfig flowerConfig = new LUFlower.FlowerConfig();
        flowerConfig.treeNum = treeNum;
        flowerConfig.treeRunNum = treeRunNum;
        flowerConfig.indexOnRun = flowerNum;
        if (posVals.length > 0) {
            flowerConfig.azimuth = Float.parseFloat(posVals[0]);
            if (posVals.length > 1) {
                flowerConfig.verticalDisplacement = Float.parseFloat(posVals[1]);
            } else {
            logger.severe("Badly formatted flower config, expect 2 values, got 1: " + flowerAddress + " = " + val);
            }
        } else {
            logger.severe("Badly formatted flower config, expect 2 values, got 0: " + flowerAddress + " = " + val);
        }
        return flowerConfig;
    }

    /**
     * Returns a new list of the flowers for a particular Tree and either Run 0, or Run 1 of that tree.
     * TODO(tracy): Just make this all static with maps.
     *
     * @param allFlowers The list of all flower configs.
     * @param treeNum Tree index / ID
     * @param treeRunNum Tree-local run number, either 0 or 1.
     * @return
     */
    static public List<LUFlower.FlowerConfig> getFlowerConfigs(List<LUFlower.FlowerConfig> allFlowers, int treeNum, int treeRunNum) {
        List<LUFlower.FlowerConfig> flowerConfigs = new ArrayList<LUFlower.FlowerConfig>();
        for (LUFlower.FlowerConfig flowerConfig : allFlowers) {
            if (flowerConfig.treeNum == treeNum || flowerConfig.treeRunNum == treeRunNum) {
                flowerConfigs.add(flowerConfig);
            }
        }
        return flowerConfigs;
    }

    /**
     * This method is used before model creation which is before UI is ready so we specify the defaults here
     * instead of in the constructor above.  We also create the ParameterFile before the UI is ready so when
     * building the UI we just pass the ParameterFile into the constructor instead of creating it during
     * object instantiation.
     *
     * @return A list of all flower positions configurations.  Will initialize defaults values if no configs
     * already exist.
     */
    static public List<LUFlower.FlowerConfig> getAllFlowerConfigs() {
        List<LUFlower.FlowerConfig> configs = new ArrayList<LUFlower.FlowerConfig>(0);
        for (int treeNum = 0; treeNum < KaledoscopeModel.NUM_ANCHOR_TREES; treeNum++) {
            // 2 separate strands of flowers per tree
            AnchorTree.AnchorTreeParams tree = AnchorTreeConfig.getAnchorTree(treeNum);
            int numRuns = 2;
            if (tree.fw2Top < 1f)
                numRuns = 1;
            for (int treeRunNum = 0; treeRunNum < numRuns; treeRunNum++) {
                // Maximum 15 flowers per strand
                int numFlowers = StrandLengths.getNumFlowers(treeNum, treeRunNum);
                for (int flowerNum = 0; flowerNum < numFlowers; flowerNum++) {
                    LUFlower.FlowerConfig fConfig = getFlowerConfig(treeNum, treeRunNum, flowerNum);
                    configs.add(fConfig);
                }
            }
        }
        return configs;
    }

    /**
     * Utility method that will be called by the FlowerPos pattern to update and save flower positions.
     */
    static public void saveUpdatedFlowerConfigs() {
        if (flowersParamFile == null)
            return;
        try {
            flowersParamFile.save();
        } catch (IOException ioex) {
            logger.severe("Error attempting to save butterflies.json configuration: " + ioex.getMessage());
        }
    }

    @Override
    public void onParameterChanged(LXParameter p) {
        parameterChanged = true;
    }

    @Override
    public void onSave() {
        // Only reconfigure if a parameter changed.
        if (parameterChanged) {
            // TODO(tracy):
            // Based on the flower address, find the flower in KaledoscopeModel and update it's position.
            List<LUFlower.FlowerConfig> allConfigs = getAllFlowerConfigs();
            for (LUFlower.FlowerConfig flowerConfig : allConfigs) {
                LUFlower flower = KaledoscopeModel.anchorTrees.get(flowerConfig.treeNum)
                    .flowerRuns.get(flowerConfig.treeRunNum).flowers.get(flowerConfig.indexOnRun);
                flower.updatePosition(flowerConfig);
            }
            lx.model.update(true, true);
        }
    }
}
