package art.lookingup.ui;

import art.lookingup.KaledoscopeOutput;
import com.symmetrylabs.slstudio.SLStudioLX;
import heronarts.lx.LX;
import heronarts.lx.parameter.LXParameter;

/**
 * UIMappingConfig provides mapping configuration for LED strands.
 * Here you can reassign strand numbers for particular Pixlite outputs.
 * A run of butterflies can potentially consist of multiple strands so
 * this will allow you to re-order and re-assign them to outputs
 * without changing the physical wiring.  It will also rebuild and restart the
 * LX Output so that it can be re-configured at runtime.
 */
public class MappingConfig extends UIConfig {
  public static final String OUTPUT1 = "output1";
  public static final String OUTPUT2 = "output2";
  public static final String OUTPUT3 = "output3";
  public static final String OUTPUT4 = "output4";
  public static final String OUTPUT5 = "output5";
  public static final String OUTPUT6 = "output6";
  public static final String OUTPUT7 = "output7";
  public static final String OUTPUT8 = "output8";
  public static final String OUTPUT9 = "output9";
  public static final String OUTPUT10 = "output10";
  public static final String OUTPUT11 = "output11";
  public static final String OUTPUT12 = "output12";
  public static final String OUTPUT13 = "output13";
  public static final String OUTPUT14 = "output14";
  public static final String OUTPUT15 = "output15";
  public static final String OUTPUT16 = "output16";

  public static final String title = "mapping";
  public static final String filename = "mappingconfig.json";
  public LX lx;
  private boolean parameterChanged = false;

  public MappingConfig(final SLStudioLX.UI ui, LX lx) {
    super(ui, title, filename);
    int contentWidth = (int)ui.leftPane.global.getContentWidth();
    this.lx = lx;

    registerStringParameter(OUTPUT1, "0");
    registerStringParameter(OUTPUT2, "1");
    registerStringParameter(OUTPUT3, "2");
    registerStringParameter(OUTPUT4, "3");
    registerStringParameter(OUTPUT5, "4");
    registerStringParameter(OUTPUT6, "5");
    registerStringParameter(OUTPUT7, "6");
    registerStringParameter(OUTPUT8, "7");
    registerStringParameter(OUTPUT9, "8");
    registerStringParameter(OUTPUT10, "9");
    registerStringParameter(OUTPUT11, "10");
    registerStringParameter(OUTPUT12, "11");
    registerStringParameter(OUTPUT13, "12");
    registerStringParameter(OUTPUT14, "13");
    registerStringParameter(OUTPUT15, "14");
    registerStringParameter(OUTPUT16, "15");


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
