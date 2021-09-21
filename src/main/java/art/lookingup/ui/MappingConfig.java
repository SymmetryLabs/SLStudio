package art.lookingup.ui;

import art.lookingup.KaledoscopeOutput;
import com.symmetrylabs.slstudio.SLStudioLX;
import heronarts.lx.LX;
import heronarts.lx.parameter.LXParameter;

import java.util.Map;

/**
 * UIMappingConfig provides mapping configuration for LED strands.
 * Here you can reassign strand numbers for particular Pixlite outputs.
 * A run of butterflies can potentially consist of multiple strands so
 * this will allow you to re-order and re-assign them to outputs
 * without changing the physical wiring.  It will also rebuild and restart the
 * LX Output so that it can be re-configured at runtime.
 */
public class MappingConfig extends UIConfig {
    public static final String BF_PIXLITE_BASE = "bf_out_";
    public static final int BF_NUM_OUTPUTS = 16;
    public static final String BF_PIXLITE1 = "bf_out_1";
    public static final String BF_PIXLITE2 = "bf_out_2";
    public static final String BF_PIXLITE3 = "bf_out_3";
    public static final String BF_PIXLITE4 = "bf_out_4";
    public static final String BF_PIXLITE5 = "bf_out_5";
    public static final String BF_PIXLITE6 = "bf_out_6";
    public static final String BF_PIXLITE7 = "bf_out_7";
    public static final String BF_PIXLITE8 = "bf_out_8";
    public static final String BF_PIXLITE9 = "bf_out_9";
    public static final String BF_PIXLITE10 = "bf_out_10";
    public static final String BF_PIXLITE11 = "bf_out_11";
    public static final String BF_PIXLITE12 = "bf_out_12";
    public static final String BF_PIXLITE13 = "bf_out_13";
    public static final String BF_PIXLITE14 = "bf_out_14";
    public static final String BF_PIXLITE15 = "bf_out_15";
    public static final String BF_PIXLITE16 = "bf_out_16";

    public static final String F_PIXLITE_BASE = "f_out_";
    public static final int F_NUM_OUTPUTS = 16;
    public static final String F_PIXLITE1 = "f_out_1";
    public static final String F_PIXLITE2 = "f_out_2";
    public static final String F_PIXLITE3 = "f_out_3";
    public static final String F_PIXLITE4 = "f_out_4";
    public static final String F_PIXLITE5 = "f_out_5";
    public static final String F_PIXLITE6 = "f_out_6";
    public static final String F_PIXLITE7 = "f_out_7";
    public static final String F_PIXLITE8 = "f_out_8";
    public static final String F_PIXLITE9 = "f_out_9";
    public static final String F_PIXLITE10 = "f_out_10";
    public static final String F_PIXLITE11 = "f_out_11";
    public static final String F_PIXLITE12 = "f_out_12";
    public static final String F_PIXLITE13 = "f_out_13";
    public static final String F_PIXLITE14 = "f_out_14";
    public static final String F_PIXLITE15 = "f_out_15";
    public static final String F_PIXLITE16 = "f_out_16";

    public static final String title = "pixlite outputs";
    public static final String filename = "mappingconfig.json";
    public LX lx;
    private boolean parameterChanged = false;

    public MappingConfig(final SLStudioLX.UI ui, LX lx) {
        super(ui, title, filename);
        int contentWidth = (int)ui.leftPane.global.getContentWidth();
        this.lx = lx;

        registerStringParameter(BF_PIXLITE1, "0.0");
        registerStringParameter(BF_PIXLITE2, "0.1");
        registerStringParameter(BF_PIXLITE3, "0.2");
        registerStringParameter(BF_PIXLITE4, "0.3");
        registerStringParameter(BF_PIXLITE5, "-1.0");
        registerStringParameter(BF_PIXLITE6, "-1.1");
        registerStringParameter(BF_PIXLITE7, "-1.2");
        registerStringParameter(BF_PIXLITE8, "-1.3");
        registerStringParameter(BF_PIXLITE9, "-1.0");
        registerStringParameter(BF_PIXLITE10, "-1.1");
        registerStringParameter(BF_PIXLITE11, "-1.2");
        registerStringParameter(BF_PIXLITE12, "-1.3");
        registerStringParameter(BF_PIXLITE13, "-1.0");
        registerStringParameter(BF_PIXLITE14, "-1.1");
        registerStringParameter(BF_PIXLITE15, "-1.2");
        registerStringParameter(BF_PIXLITE16, "-1.3");

        registerStringParameter(F_PIXLITE1, "0.0");
        registerStringParameter(F_PIXLITE2, "0.1");
        registerStringParameter(F_PIXLITE3, "1.0");
        registerStringParameter(F_PIXLITE4, "1.1");
        registerStringParameter(F_PIXLITE5, "2.0");
        registerStringParameter(F_PIXLITE6, "3.0");
        registerStringParameter(F_PIXLITE7, "4.0");
        registerStringParameter(F_PIXLITE8, "4.1");
        registerStringParameter(F_PIXLITE9, "-1.-1");
        registerStringParameter(F_PIXLITE10, "-1.-1");
        registerStringParameter(F_PIXLITE11, "-1.-1");
        registerStringParameter(F_PIXLITE12, "-1.-1");
        registerStringParameter(F_PIXLITE13, "-1.-1");
        registerStringParameter(F_PIXLITE14, "-1.-1");
        registerStringParameter(F_PIXLITE15, "-1.-1");
        registerStringParameter(F_PIXLITE16, "-1.-1");


        save();

        buildUI(ui);
    }


    /**
     * Return the Pixlite output for a given butterfly strand.
     * NOTE: The UI shows Pixlite outputs as 1 indexed but ArtNet is really 0 indexed so we return
     * 0 for Pixlite Output 1, etc. This is just to keep it consisted with Advatek Assistant UI.
     *
     * @param strandNum
     * @return
     */
    public int butterflyPixliteOutputForStrand(int strandNum) {
        for (int i = 1; i < BF_NUM_OUTPUTS + 1; i++) {
            String outputKey = BF_PIXLITE_BASE + i;
            int strand = Integer.parseInt(getStringParameter(outputKey).getString());
            if (strand == strandNum) {
                return i - 1;
            }
        }
        return 0;
    }

    /**
     * Return the Pixlite output for a given flower strand.
     * NOTE: The UI shows Pixlite outputs as 1 indexed but ArtNet is really 0 indexed so we return
     * 0 for Pixlite Output 1, etc. This is just to keep it consisted with Advatek Assistant UI.
     *
     * @param strandNum
     * @return
     */
    public int flowerPixliteOutputForStrand(int strandNum) {
        for (int i = 1; i < F_NUM_OUTPUTS + 1; i++) {
            String outputKey = F_PIXLITE_BASE + i;
            int strand = Integer.parseInt(getStringParameter(outputKey).getString());
            if (strand == strandNum) {
                return i - 1;
            }
        }
        return 0;
    }

    @Override
    public void onParameterChanged(LXParameter p) {
        parameterChanged = true;
    }

    @Override
    public void onSave() {
        // Only reconfigure if a parameter changed.
        if (parameterChanged) {
            boolean originalEnabled = lx.engine.output.enabled.getValueb();
            lx.engine.output.enabled.setValue(false);
            /*
            for (LXOutput child : lx.engine.output.children) {
                lx.engine.output.removeChild(child);
            } */
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
