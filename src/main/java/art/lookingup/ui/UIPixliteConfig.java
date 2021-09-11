package art.lookingup.ui;

import com.symmetrylabs.slstudio.SLStudioLX;
import heronarts.lx.LX;
import heronarts.lx.parameter.LXParameter;

public class UIPixliteConfig extends UIConfig {
  public static final String PIXLITE_1_IP = "ip1";
  public static final String PIXLITE_1_PORT = "port1";

  public static final String title = "pixlite";
  public static final String filename = "pixliteconfig.json";
  public LX lx;
  private boolean parameterChanged = false;

  public UIPixliteConfig(final SLStudioLX.UI ui, LX lx) {
    super(ui, title, filename);
    int contentWidth = (int)ui.leftPane.global.getContentWidth();
    this.lx = lx;

    registerStringParameter(PIXLITE_1_IP, "192.168.2.134");
    registerStringParameter(PIXLITE_1_PORT, "6454");

    save();

    buildUI(ui);
  }

  @Override
  public void onParameterChanged(LXParameter p) {
    parameterChanged = true;
  }

  @Override
  public void onSave() {
    // Only reconfigure if a parameter changed.
    if (parameterChanged) {
        /*
      boolean originalEnabled = lx.engine.output.enabled.getValueb();
      lx.engine.output.enabled.setValue(false);
      for (LXOutput child : lx.engine.output.children) {
        lx.engine.output.removeChild(child);
      }
      Output.configurePixliteOutput(lx);
      parameterChanged = false;
      lx.engine.output.enabled.setValue(originalEnabled);
      */
    }
  }
}
