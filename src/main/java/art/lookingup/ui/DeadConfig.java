package art.lookingup.ui;

import art.lookingup.KaledoscopeModel;
import art.lookingup.KaledoscopeOutput;
import art.lookingup.ParameterFile;
import com.symmetrylabs.slstudio.SLStudioLX;
import heronarts.lx.LX;
import heronarts.lx.parameter.LXParameter;

import java.util.ArrayList;
import java.util.List;

/**
 * Allow for the specification of dead butterflies and flowers.  If a fixture dies, it will be physically removed
 * from the strand.  Without any other changes, it would effectively break the mapping because the next fixture
 * would be receiving data for the broken fixture with it's associated location.  We handle this by having a list
 * of LXPoints to render per fixture and tracking a separate list of subset LXPoints that are addressable.  We
 * mark the fixture dead and then rebuild the network output mapping.
 */
public class DeadConfig extends UIConfig {

    public static final String DEAD_B_BASE = "b_";
    public static final String DEAD_F_BASE = "f_";
    public static final String title = "dead";
    public static final String filename = "dead.json";
    public LX lx;
    private boolean parameterChanged = false;
    static public ParameterFile deadParamFile;
    static public final int MAX_DEAD = 15;

    public DeadConfig(final SLStudioLX.UI ui, LX lx, ParameterFile parameterFile) {
        super(ui, title, filename, parameterFile);

        this.lx = lx;

        for (int deadNum = 0; deadNum < MAX_DEAD; deadNum++) {
            registerStringParameter(DEAD_B_BASE + deadNum, null);
        }

        for (int deadNum = 0; deadNum < MAX_DEAD; deadNum++) {
            registerStringParameter(DEAD_F_BASE + deadNum, null);
        }

        save();
        buildUI(ui);
    }

    static public void init() {
        if (deadParamFile == null)
            deadParamFile = ParameterFile.instantiateAndLoad(filename);
        for (int deadNum = 0; deadNum < MAX_DEAD; deadNum++) {
            deadParamFile.getStringParameter(DEAD_B_BASE + deadNum, "");
        }

        for (int deadNum = 0; deadNum < MAX_DEAD; deadNum++) {
            deadParamFile.getStringParameter(DEAD_F_BASE + deadNum, "");
        }
    }

    /**
     * Get dead butterfly addresses.  They are in the form of Strand-ID.ButterflyStrandIndex zero based.  so 1.3 is
     * the fourth butterfly on the second strand.
     * @return
     */
    static public List<String> deadButterflyAddresses() {
        List<String> deadButterflies = new ArrayList<String>();
        for (int deadNum = 0; deadNum < MAX_DEAD; deadNum++) {
            String address = deadParamFile.getStringParameter(DEAD_B_BASE + deadNum, "").getString();
            if (!"".equals(address)) {
                deadButterflies.add(address);
            }
        }
        return deadButterflies;
    }

    /**
     * Get dead flower addresses.  They are in the form of AnchorTree-ID.RunNum.RunIndex and zero-based.  So 1.0.5 is
     * the second tree, the first run, and the sixth flower.
     * @return
     */
    static public List<String> deadFlowerAddresses() {
        List<String> deadFlowers = new ArrayList<String>();
        for (int deadNum = 0; deadNum < MAX_DEAD; deadNum++) {
            String address = deadParamFile.getStringParameter(DEAD_F_BASE + deadNum, "").getString();
            if (!"".equals(address)) {
                deadFlowers.add(address);
            }
        }
        return deadFlowers;
    }

    @Override
    public void onParameterChanged(LXParameter p) {
        parameterChanged = true;
    }

    @Override
    public void onSave() {
        // Only reconfigure if a parameter changed.
        if (parameterChanged) {
            // We need to tell the model to mark the fixtures dead.
            KaledoscopeModel.markDeadButterflies(deadButterflyAddresses());
            KaledoscopeModel.markDeadFlowers(deadFlowerAddresses());
            KaledoscopeModel.updateDeadOnStrands();

            // TODO(tracy): Maybe only pause the output that something is being marked dead on?
            boolean originalEnabled = lx.engine.output.enabled.getValueb();
            lx.engine.output.enabled.setValue(false);
            // This version of LX doesn't provide access to the children variable so we will use
            // a static member variable we set when constructing the output.
            lx.engine.output.removeChild(KaledoscopeOutput.butterflyDatagramOutput);
            lx.engine.output.removeChild(KaledoscopeOutput.flowerDatagramOutput);
            KaledoscopeOutput.configurePixliteOutput(lx);
            parameterChanged = false;
            lx.engine.output.enabled.setValue(originalEnabled);
        }
    }
}
