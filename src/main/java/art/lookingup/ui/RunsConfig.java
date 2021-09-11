package art.lookingup.ui;

import art.lookingup.ParameterFile;
import com.symmetrylabs.slstudio.SLStudioLX;
import heronarts.lx.LX;
import heronarts.lx.parameter.LXParameter;

/**
 * UI class for configuring the number of runs of butterflies and the number of runs of
 * flowers.
 *
 * TODO(tracy): Currently there is only an adhoc relationship between strand ID's and run ID's.
 * i.e. The strand ID's are allocated start at 0 as we build the runs of butterflies and the runs
 * of flowers in the model.  For example if we have 3 runs of butterflies and 4 runs of flowers then
 * runs 0, 1, 2 are butterflies and 3, 4, 5, 6 are flowers.  Butterfly runs must always be two strands
 * so butterfly stand ID 0 and 1 belong to butterfly run 0.  When configuring the strand lengths in
 * another UI component we only have strand IDs without reference to whether the strand ID is a
 * butterfly or flowers.  Since installation details are not nailed down at this point, it is better
 * to make this as configurable as possible.  This allows for a more site-specific install.
 */
public class RunsConfig extends UIConfig {
  public static final String BUTTERFLY_RUNS = "bf runs";
  public static final String FLOWER_RUNS = "fl runs";
  public static final String title = "runs";
  public static final String filename = "runs.json";
  public LX lx;
  private boolean parameterChanged = false;

  public RunsConfig(final SLStudioLX.UI ui, LX lx, ParameterFile parameterFile) {
    super(ui, title, filename, parameterFile);

    this.lx = lx;

    // Run 1
    registerStringParameter(BUTTERFLY_RUNS, "3");
    registerStringParameter(FLOWER_RUNS, "4");
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
    }
  }
}
