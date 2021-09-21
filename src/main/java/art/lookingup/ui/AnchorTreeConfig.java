package art.lookingup.ui;

import art.lookingup.AnchorTree;
import art.lookingup.KaledoscopeModel;
import art.lookingup.ParameterFile;
import com.symmetrylabs.slstudio.SLStudioLX;
import heronarts.lx.LX;
import heronarts.lx.parameter.LXParameter;

public class AnchorTreeConfig extends UIConfig {
    public static final String TREE_BASE = "t";
    public static final String TREE_X = "_x";
    public static final String TREE_Z = "_z";
    public static final String TREE_BF = "_butterfly";  // 1 = butterfly anchor, 0 = not
    public static final String TREE_RADIUS = "_radius";
    public static final String TREE_C1_Y = "_c1_y";  // cable mounting height.
    public static final String TREE_C2_Y = "_c2_y";
    public static final String TREE_C3_Y = "_c3_y";
    public static final String TREE_FW1_TOP = "_fw1_top";
    public static final String TREE_FW1_RADIUS = "_fw1_rad";
    public static final String TREE_FW2_TOP = "_fw2_top";
    public static final String TREE_FW2_RADIUS = "_fw2_rad";

    public static final String title = "trees";
    public static final String filename = "trees.json";
    public LX lx;
    private boolean parameterChanged = false;
    static public ParameterFile anchorTreeParamFile;

    // Rough defaults based on install.
    // Removed Tree 2: 6 * 12, 12 * 12, 1, 0, 8
    public static float[] treesX = {8 * 12, -3 * 12, -6 * 12, 0 * 12};
    public static float[] treesZ = {10 * 12, 19 * 12, 25 * 12, 35 * 12};
    public static float[] treesFlowers = {2, 2, 1, 1, 2};
    public static int[] treesButterflies = {1, 1, 1, 1, 1};
    public static int[] treeRadius = {12, 12, 12, 12, 12};



    public AnchorTreeConfig(final SLStudioLX.UI ui, LX lx, ParameterFile parameterFile) {
        super(ui, title, filename, parameterFile);

        this.lx = lx;

        for (int treeNum = 0; treeNum < KaledoscopeModel.NUM_ANCHOR_TREES; treeNum++) {
            registerStringParameter(TREE_BASE + treeNum + TREE_X, null);
            registerStringParameter(TREE_BASE + treeNum + TREE_Z, null);
            registerStringParameter(TREE_BASE + treeNum + TREE_BF, null);
            registerStringParameter(TREE_BASE + treeNum + TREE_RADIUS, null);
            registerStringParameter(TREE_BASE + treeNum + TREE_C1_Y, null);
            registerStringParameter(TREE_BASE + treeNum + TREE_C2_Y, null);
            registerStringParameter(TREE_BASE + treeNum + TREE_C3_Y, null);
            registerStringParameter(TREE_BASE + treeNum + TREE_FW1_TOP, null);
            registerStringParameter(TREE_BASE + treeNum + TREE_FW1_RADIUS, null);
            registerStringParameter(TREE_BASE + treeNum + TREE_FW2_TOP, null);
            registerStringParameter(TREE_BASE + treeNum + TREE_FW2_RADIUS, null);
        }

        save();

        buildUI(ui);
    }

    static public AnchorTree.AnchorTreeParams getAnchorTree(int treeNum) {
        if (anchorTreeParamFile == null)
            anchorTreeParamFile = ParameterFile.instantiateAndLoad(filename);
        AnchorTree.AnchorTreeParams p = new AnchorTree.AnchorTreeParams();
        float defaultX = 0f;
        float defaultZ = 0f;
        if (treeNum > 0) {
            defaultX = treesX[treeNum-1];
            defaultZ = treesZ[treeNum-1];
        }
        p.x = anchorTreeParamFile.getStringParameterF(TREE_BASE + treeNum + TREE_X, "" + defaultX);
        p.z = anchorTreeParamFile.getStringParameterF(TREE_BASE + treeNum + TREE_Z, "" + defaultZ);
        int radius = treeRadius[treeNum];
        p.radius = anchorTreeParamFile.getStringParameterF(TREE_BASE + treeNum + TREE_RADIUS, "" + radius);

        // Flowers are pre-mounted on cages wrapped around the trees.  We allow for the cage-specific radius to be
        // specified because the radius at the anchor cable mounting might be different than the radius where the cage
        // is mounted.
        p.fw1Top = anchorTreeParamFile.getStringParameterF(TREE_BASE + treeNum + TREE_FW1_TOP, "96");
        p.fw1Radius = anchorTreeParamFile.getStringParameterF(TREE_BASE + treeNum + TREE_FW1_RADIUS, "" + radius);
        if (treesFlowers[treeNum] == 2)
            p.fw2Top = anchorTreeParamFile.getStringParameterF(TREE_BASE + treeNum + TREE_FW2_TOP, "60");
        else
            p.fw2Top = anchorTreeParamFile.getStringParameterF(TREE_BASE + treeNum + TREE_FW2_TOP, "0");
        p.fw2Radius = anchorTreeParamFile.getStringParameterF(TREE_BASE + treeNum + TREE_FW2_RADIUS, "" + radius);

        int treeHasButterfly = treesButterflies[treeNum];
        p.isButterflyAnchor = Integer.parseInt(anchorTreeParamFile.getStringParameter(TREE_BASE + treeNum + TREE_BF, "" + treeHasButterfly).getString()) != 0;

        p.c1Y = anchorTreeParamFile.getStringParameterF(TREE_BASE + treeNum + TREE_C1_Y, "120");
        p.c2Y = anchorTreeParamFile.getStringParameterF(TREE_BASE + treeNum + TREE_C2_Y, "114");
        p.c3Y = anchorTreeParamFile.getStringParameterF(TREE_BASE + treeNum + TREE_C3_Y, "120");
        return p;
    }

    @Override
    public void onParameterChanged(LXParameter p) {
        parameterChanged = true;
    }

    @Override
    public void onSave() {
        // Only reconfigure if a parameter changed.
        if (parameterChanged) {
        }
    }
}
