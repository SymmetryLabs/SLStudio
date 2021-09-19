package art.lookingup.ui;

import art.lookingup.KaledoscopeModel;
import art.lookingup.ParameterFile;
import com.symmetrylabs.slstudio.SLStudioLX;
import heronarts.lx.LX;
import heronarts.lx.parameter.LXParameter;

/**
 * Allow for the number of LEDs to be configurable for each strand.
 * TODO(tracy): Make this changeable at runtime?  It would allow for
 * hot removing of dead butterflies although that might not be the
 * electrically safest thing to do.
 *
 * The flowers in the trees will have different strand lengths from
 * strands of butterflies.
 *
 * Length here refers to numbers of LEDs, not the number of fixtures.
 * I.e. Specify 32 LEDs, not 2 butterflies.
 */
public class StrandLengths extends UIConfig {
    public static final String title = "strand lens";
    public static final String filename = "strandlengths.json";
    public LX lx;
    private boolean parameterChanged = false;
    static public ParameterFile strandLengthsParamFile;

    public StrandLengths(final SLStudioLX.UI ui, LX lx, ParameterFile paramFile) {
        super(ui, title, filename, paramFile);
        int contentWidth = (int)ui.leftPane.global.getContentWidth();
        this.lx = lx;

        /* NOTE(tracy): Butterfly strand lengths should be specified by butterfly per-cable distance measures.
        for (int bfRunNum = 0; bfRunNum < 3; bfRunNum++) {
            for (int strandRunNum = 0; strandRunNum < 4; strandRunNum++) {
                registerStringParameter("bf_R" + bfRunNum + "_S" + strandRunNum, null);
            }
        }
        */

        for (int treeNum = 0; treeNum < KaledoscopeModel.NUM_ANCHOR_TREES_FLOWERS; treeNum++) {
            for (int runNum = 0; runNum < KaledoscopeModel.FLOWER_RUNS_PER_TREE; runNum++) {
                registerStringParameter("fw_T" + treeNum + "_R" + runNum, null);
            }
        }
        save();

        buildUI(ui);
    }

    /**
     * We need to preload the values before the UI attempts to render.  The model building should initialize any
     * values it needs, but depending on the model, there might be fields that were not used by the model but
     * need to be initialized before the UI renders.
     */
    static public void preloadDefaults() {
        /* NOTE(tracy): Butterfly strand lengths should be specified by butterfly per-cable distance measures.
        for (int bfRunNum = 0; bfRunNum < 3; bfRunNum++) {
            for (int strandRunNum = 0; strandRunNum < 4; strandRunNum++) {
                getNumButterflies(bfRunNum, strandRunNum);
            }
        }
        */
        for (int treeNum = 0; treeNum < KaledoscopeModel.NUM_ANCHOR_TREES_FLOWERS; treeNum++) {
            for (int runNum = 0; runNum < KaledoscopeModel.FLOWER_RUNS_PER_TREE; runNum++) {
                getNumFlowers(treeNum, runNum);
            }
        }
    }

    /* NOTE(tracy): Butterfly strand lengths should be specified by butterfly per-cable distance measures.
     * So this method should not be called.
     */
    static public int getNumButterfliesDeprecated(int runId, int strandId) {
        if (strandLengthsParamFile == null) {
            strandLengthsParamFile = ParameterFile.instantiateAndLoad(filename);
        }
        String key = "bf_R" + runId + "_S" + strandId;
        // By default currently, we only have 2 runs of butterflies.
        if (runId < 2)
            return Integer.parseInt(strandLengthsParamFile.getStringParameter(key, "20").getString());
        else
            return Integer.parseInt(strandLengthsParamFile.getStringParameter(key, "0").getString());
    }

    static public int getNumFlowers(int anchorTree, int runNum) {
        if (strandLengthsParamFile == null) {
            strandLengthsParamFile = ParameterFile.instantiateAndLoad(filename);
        }
        String key = "fw_T" + anchorTree + "_R" + runNum;
        return Integer.parseInt(strandLengthsParamFile.getStringParameter(key, "14").getString());
    }

    @Override
    public void onParameterChanged(LXParameter p) {
        parameterChanged = true;
    }

    @Override
    public void onSave() {
        // Only reconfigure if a parameter changed.
        if (parameterChanged) {
            // NOTE(tracy): Strand lengths are currently computed automatically based on trees.
            // We can't enable this for flowers because changing their strand lengths will change the model which
            // requires a restart.
            /*
            KaledoscopeModel.reassignButterflyStrands();
            boolean originalEnabled = lx.engine.output.enabled.getValueb();
            lx.engine.output.enabled.setValue(false);
            // This version of LX doesn't provide access to the children variable so we will use
            // a static member variable we set when constructing the output.
            lx.engine.output.removeChild(KaledoscopeOutput.butterflyDatagramOutput);
            lx.engine.output.removeChild(KaledoscopeOutput.flowerDatagramOutput);
            KaledoscopeOutput.configurePixliteOutput(lx);
            parameterChanged = false;
            lx.engine.output.enabled.setValue(originalEnabled);
            */
        }
    }
}
