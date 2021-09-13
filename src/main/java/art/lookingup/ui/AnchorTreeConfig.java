package art.lookingup.ui;

import art.lookingup.ParameterFile;
import com.symmetrylabs.slstudio.SLStudioLX;
import heronarts.lx.LX;
import heronarts.lx.parameter.LXParameter;

import java.util.ArrayList;
import java.util.List;

public class AnchorTreeConfig extends UIConfig {
    public static final String TREE1_X = "tree1_x";
    public static final String TREE1_Z = "tree1_z";
    public static final String TREE1_RADIUS = "tree1_radius";
    public static final String TREE2_X = "tree2_x";
    public static final String TREE2_Z = "tree2_z";
    public static final String TREE2_RADIUS = "tree2_radius";
    public static final String TREE3_X = "tree3_x";
    public static final String TREE3_Z = "tree3_z";
    public static final String TREE3_RADIUS = "tree3_radius";
    public static final String TREE4_X = "tree4_x";
    public static final String TREE4_Z = "tree4_z";
    public static final String TREE4_RADIUS = "tree4_radius";
    public static final String TREE5_X = "tree5_x";
    public static final String TREE5_Z = "tree5_z";
    public static final String TREE5_RADIUS = "tree5_radius";
    public static final String title = "trees";
    public static final String filename = "trees.json";
    public LX lx;
    private boolean parameterChanged = false;

    public AnchorTreeConfig(final SLStudioLX.UI ui, LX lx, ParameterFile parameterFile) {
        super(ui, title, filename, parameterFile);

        this.lx = lx;

        registerStringParameter(TREE1_X, null);
        registerStringParameter(TREE1_Z, null);
        registerStringParameter(TREE1_RADIUS, null);
        registerStringParameter(TREE2_X, null);
        registerStringParameter(TREE2_Z, null);
        registerStringParameter(TREE2_RADIUS, null);
        registerStringParameter(TREE3_X, null);
        registerStringParameter(TREE3_Z, null);
        registerStringParameter(TREE3_RADIUS, null);
        registerStringParameter(TREE4_X, null);
        registerStringParameter(TREE4_Z, null);
        registerStringParameter(TREE4_RADIUS, null);
        registerStringParameter(TREE5_X, null);
        registerStringParameter(TREE5_Z, null);
        registerStringParameter(TREE5_RADIUS, null);

        save();

        buildUI(ui);
    }

    static public List<Float> getTreesPos(ParameterFile pFile) {
        List<Float> treesPos = new ArrayList<Float>(0);
        /*
         * Five generated tree coordinates:
         * 60.0,12.0 : -60.0,252.0 : 60.0,492.0 : -60.0,732.0 : 60.0,972.0
         */
        treesPos.add(Float.parseFloat(pFile.getStringParameter(TREE1_X, "60.0").getString()));
        treesPos.add(Float.parseFloat(pFile.getStringParameter(TREE1_Z, "12.0").getString()));
        treesPos.add(Float.parseFloat(pFile.getStringParameter(TREE2_X, "-60.0").getString()));
        treesPos.add(Float.parseFloat(pFile.getStringParameter(TREE2_Z, "252.0").getString()));
        treesPos.add(Float.parseFloat(pFile.getStringParameter(TREE3_X, "60.0").getString()));
        treesPos.add(Float.parseFloat(pFile.getStringParameter(TREE3_Z, "492.0").getString()));
        treesPos.add(Float.parseFloat(pFile.getStringParameter(TREE4_X, "-60.0").getString()));
        treesPos.add(Float.parseFloat(pFile.getStringParameter(TREE4_Z, "732.0").getString()));
        treesPos.add(Float.parseFloat(pFile.getStringParameter(TREE5_X, "60.0").getString()));
        treesPos.add(Float.parseFloat(pFile.getStringParameter(TREE5_Z, "972.0").getString()));
        return treesPos;
    }

    static public List<Float> getTreesRadii(ParameterFile pFile) {
        List<Float> treesRadii = new ArrayList<Float>(0);

        treesRadii.add(Float.parseFloat(pFile.getStringParameter(TREE1_RADIUS, "12.0").getString()));
        treesRadii.add(Float.parseFloat(pFile.getStringParameter(TREE2_RADIUS, "24.0").getString()));
        treesRadii.add(Float.parseFloat(pFile.getStringParameter(TREE3_RADIUS, "12.0").getString()));
        treesRadii.add(Float.parseFloat(pFile.getStringParameter(TREE4_RADIUS, "24.0").getString()));
        treesRadii.add(Float.parseFloat(pFile.getStringParameter(TREE5_RADIUS, "12.0").getString()));
        return treesRadii;
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
