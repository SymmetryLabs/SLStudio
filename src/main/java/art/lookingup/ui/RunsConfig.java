package art.lookingup.ui;

import art.lookingup.ParameterFile;
import com.symmetrylabs.slstudio.SLStudioLX;
import heronarts.lx.LX;
import heronarts.lx.parameter.LXParameter;

import java.util.ArrayList;
import java.util.List;

/**
 * UI class for configuring the number of runs of butterflies and the number of runs of
 * flowers.  We don't specify the default values here because they must be created before
 * the model is created in FireflyShow which is before the UI is built.
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
  public static final String BF_STRANDS_RUN1 = "brun1 s#";
  public static final String BF_STRANDS_RUN2 = "brun2 s#";
  public static final String BF_STRANDS_RUN3 = "brun3 s#";
  public static final String BF_STRANDS_RUN4 = "brun4 s#";
  public static final String FLOWER_RUNS = "fl runs";
  public static final String title = "runs";
  public static final String filename = "runs.json";
  public LX lx;
  private boolean parameterChanged = false;

  public RunsConfig(final SLStudioLX.UI ui, LX lx, ParameterFile parameterFile) {
    super(ui, title, filename, parameterFile);

    this.lx = lx;

    // Run 1
    registerStringParameter(BUTTERFLY_RUNS, null);
    registerStringParameter(BF_STRANDS_RUN1, null);
    registerStringParameter(BF_STRANDS_RUN2, null);
    registerStringParameter(BF_STRANDS_RUN3, null);
    registerStringParameter(BF_STRANDS_RUN4, null);
    registerStringParameter(FLOWER_RUNS, null);
    save();

    buildUI(ui);
  }

  static public List<Integer> getRunsNumStrands(ParameterFile pFile) {
      List<Integer> numStrands = new ArrayList<Integer>(0);
      numStrands.add(Integer.parseInt(pFile.getStringParameter(BF_STRANDS_RUN1, "4").getString()));
      numStrands.add(Integer.parseInt(pFile.getStringParameter(BF_STRANDS_RUN2, "4").getString()));
      numStrands.add(Integer.parseInt(pFile.getStringParameter(BF_STRANDS_RUN3, "4").getString()));
      numStrands.add(Integer.parseInt(pFile.getStringParameter(BF_STRANDS_RUN4, "4").getString()));
      return numStrands;
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
