package art.lookingup.ui;

import art.lookingup.KaledoscopeModel;
import art.lookingup.LUFlower;
import art.lookingup.ParameterFile;
import com.symmetrylabs.slstudio.SLStudioLX;
import heronarts.lx.LX;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.StringParameter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/*
 * Allow position specification for each flower.
 */
public class ButterfliesConfig extends UIConfig {
    private static final Logger logger = Logger.getLogger(FlowersConfig.class.getName());
    public static final String title = "butterflies";
    public static final String filename = "butterflies.json";
    public LX lx;
    private boolean parameterChanged = false;
    static public final int MAX_BUTTERFLIES_PER_RUN = 140;
    static public ParameterFile butterfliesParamFile;

    public ButterfliesConfig(final SLStudioLX.UI ui, LX lx, ParameterFile paramFile) {
        super(ui, title, filename, paramFile);
        int contentWidth = (int)ui.leftPane.global.getContentWidth();
        this.lx = lx;

        // Four anchor trees
        for (int i = 0; i < MAX_BUTTERFLIES_PER_RUN; i++) {
            registerStringParameter("B." + i, "");
        }
        save();
        buildUI(ui);
    }

    /**
     * For each butterfly, specify the tree number, the cable number, and the distance to previous butterfly on that
     * cable.  Distance is just in integer inches. -1 tree number means unused.
     * @param butterflyNum
     * @return
     */
    static public int[] getButterflyConfig(int butterflyNum) {
        String butterflyAddress = "B." + butterflyNum;
        if (butterfliesParamFile == null) {
            butterfliesParamFile = ParameterFile.instantiateAndLoad(filename);
        }
        // The default is ring 0, and 360f * flowerNum / (MAX_FLOWERS_PER_RUN) degrees and vertical displacement of 0.
        // TODO(tracy): We need to compute a reasonable default until exact measurements are in.
        // Will dump the current generated version.
        String val = butterfliesParamFile.getStringParameter(butterflyAddress, "-1,0,0").getString();
        String[] posVals = val.split(",");
        int[] vals = new int[3];
        try {
            vals[0] = Integer.parseInt(posVals[0]);
            vals[1] = Integer.parseInt(posVals[1]);
            vals[2] = Integer.parseInt(posVals[2]);
        } catch (Exception ex) {
            logger.severe("Badly formatted butterfly.  Num: " + butterflyNum + " val=" + val);
        }
        return vals;
    }

    /**
     * Adding this utility method to pre-populate the configs from the generated model so that we have something
     * approximate until the real measurements are in.  butterfliesParamFile.save() should be called after all
     * butterflies are updated.
     * @param butterflyNum
     * @param treeNum
     * @param whichCable
     * @param inches
     */
    static public void setButterflyConfig(int butterflyNum, int treeNum, int whichCable, int inches) {
        String butterflyAddress = "B." + butterflyNum;
        StringParameter sp = butterfliesParamFile.getStringParameter(butterflyAddress, "");
        sp.setValue("" + treeNum + "," + whichCable + "," + inches);
    }

    /**
     * Utility method that will be called by the model creation code to populate the butterfly configs with some
     * reasonable defaults.
     */
    static public void saveUpdatedButterflyConfigs() {
        if (butterfliesParamFile == null)
            return;
        try {
            butterfliesParamFile.save();
        } catch (IOException ioex) {
            logger.severe("Error attempting to save butterflies.json configuration: " + ioex.getMessage());
        }
    }

    /**
     * Returns a new list of the butterflies for a particular tree.
     *
     * @param allButterflies The list of all flower configs.
     * @param treeNum Tree index / ID
     * @return
     */
    static public List<int[]> getButterflyConfigs(List<int[]> allButterflies, int treeNum) {
        List<int[]> butterflyConfigs = new ArrayList<int[]>();
        for (int[] butterflyConfig : allButterflies) {
            if (butterflyConfig[0] == treeNum) {
                butterflyConfigs.add(butterflyConfig);
            }
        }
        return butterflyConfigs;
    }

    /**
     * Pre-load all the butterfly config values.
     *
     * @return A list of all flower positions configurations.  Will initialize defaults values if no configs
     * already exist.
     */
    static public List<int[]> getAllButterflyConfigs() {
        List<int[]> configs = new ArrayList<int[]>(0);
        for (int butterflyNum = 0; butterflyNum < MAX_BUTTERFLIES_PER_RUN; butterflyNum++) {
            int[] bConfig = getButterflyConfig(butterflyNum);
            configs.add(bConfig);
        }
        return configs;
    }

    /**
     * For a given butterfly number on the run, return the cable it should be on.
     * @param butterflyRunIndex
     * @return
     */
    static public int getCableForButterflyRunIndex(int butterflyRunIndex) {
        int[] config = ButterfliesConfig.getButterflyConfig(butterflyRunIndex);
        return config[1];
        // NOTE(tracy): GENERATE BUTTERFLY POSITIONS use this commented out code.
        //return butterflyRunIndex % 3;
    }

    static public float getCableDistancePrevButterfly(int butterflyRunIndex) {
        int[] config = ButterfliesConfig.getButterflyConfig(butterflyRunIndex);
        return (float) config[2];
        // NOTE(tracy): GENERATE BUTTERFLY POSITIONS use this commented out code.
        //return 12f;
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
            // We should update butterfly positions without require a restart.
            KaledoscopeModel.updateButterflyPositions(lx);
        }
    }
}
