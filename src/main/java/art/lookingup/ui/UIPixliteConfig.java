package art.lookingup.ui;

import art.lookingup.KaledoscopeOutput;
import com.symmetrylabs.slstudio.SLStudioLX;
import heronarts.lx.LX;
import heronarts.lx.parameter.LXParameter;

public class UIPixliteConfig extends UIConfig {
    public static final String BUTTERFLY_PIXLITE_IP = "bf_ip";
    public static final String BUTTERFLY_PIXLITE_PORT = "bf_port";
    public static final String FLOWER_PIXLITE_IP = "flw_ip";
    public static final String FLOWER_PIXLITE_PORT = "flw_port";

    public static final String title = "pixlite IP";
    public static final String filename = "pixliteconfig.json";
    public LX lx;
    private boolean parameterChanged = false;

    public UIPixliteConfig(final SLStudioLX.UI ui, LX lx) {
        super(ui, title, filename);
        int contentWidth = (int)ui.leftPane.global.getContentWidth();
        this.lx = lx;

        registerStringParameter(BUTTERFLY_PIXLITE_IP, "127.0.0.1");
        registerStringParameter(BUTTERFLY_PIXLITE_PORT, "6454");
        registerStringParameter(FLOWER_PIXLITE_IP, "127.0.0.1");
        registerStringParameter(FLOWER_PIXLITE_PORT, "6454");

        save();

        buildUI(ui);
    }

    public String butterflyPixliteIp() {
        return getStringParameter(BUTTERFLY_PIXLITE_IP).getString();
    }

    public int butterflyPixlitePort() {
        return Integer.parseInt(getStringParameter(BUTTERFLY_PIXLITE_PORT).getString());
    }

    public String flowerPixliteIp() {
        return getStringParameter(FLOWER_PIXLITE_IP).getString();
    }

    public int flowerPixlitePort() {
        return Integer.parseInt(getStringParameter(FLOWER_PIXLITE_PORT).getString());
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
