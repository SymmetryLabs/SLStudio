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
        float zSpacing = 15f * 12f; // 15 feet in Z
        float xOffset = 5f * 12f; // 5 feet offset horizontally from center.
        if (treeNum % 2 == 1) {
            xOffset = -xOffset;
        }
        p.x = anchorTreeParamFile.getStringParameterF(TREE_BASE + treeNum + TREE_X, "" + xOffset);
        p.z = anchorTreeParamFile.getStringParameterF(TREE_BASE + treeNum + TREE_Z, "" + (treeNum * zSpacing));
        int radius = 12;
        if (treeNum % 2 == 1)
            radius = 24;
        p.radius = anchorTreeParamFile.getStringParameterF(TREE_BASE + treeNum + TREE_RADIUS, "" + radius);

        // Flowers are pre-mounted on cages wrapped around the trees.  We allow for the cage-specific radius to be
        // specified because the radius at the anchor cable mounting might be different than the radius where the cage
        // is mounted.
        p.fw1Top = anchorTreeParamFile.getStringParameterF(TREE_BASE + treeNum + TREE_FW1_TOP, "120");
        p.fw1Radius = anchorTreeParamFile.getStringParameterF(TREE_BASE + treeNum + TREE_FW1_RADIUS, "" + radius);
        p.fw2Top = anchorTreeParamFile.getStringParameterF(TREE_BASE + treeNum + TREE_FW2_TOP, "72");
        p.fw2Radius = anchorTreeParamFile.getStringParameterF(TREE_BASE + treeNum + TREE_FW2_RADIUS, "" + radius);

        p.isButterflyAnchor = Integer.parseInt(anchorTreeParamFile.getStringParameter(TREE_BASE + treeNum + TREE_BF, "1").getString()) != 0;

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
