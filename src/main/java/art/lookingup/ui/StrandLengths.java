package art.lookingup.ui;

import art.lookingup.ParameterFile;
import com.symmetrylabs.slstudio.SLStudioLX;
import heronarts.lx.LX;
import heronarts.lx.parameter.LXParameter;

import java.util.ArrayList;
import java.util.List;

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
  public static final String STRAND1 = "strand1length";
  public static final String STRAND2 = "strand2length";
  public static final String STRAND3 = "strand3length";
  public static final String STRAND4 = "strand4length";
  public static final String STRAND5 = "strand5length";
  public static final String STRAND6 = "strand6length";
  public static final String STRAND7 = "strand7length";
  public static final String STRAND8 = "strand8length";
  public static final String STRAND9 = "strand9length";
  public static final String STRAND10 = "strand10length";
  public static final String STRAND11 = "strand11length";
  public static final String STRAND12 = "strand12length";
  public static final String STRAND13 = "strand13length";
  public static final String STRAND14 = "strand14length";
  public static final String STRAND15 = "strand15length";
  public static final String STRAND16 = "strand16length";

  public static final String title = "strand lens";
  public static final String filename = "strandlengths.json";
  public LX lx;
  private boolean parameterChanged = false;

  public StrandLengths(final SLStudioLX.UI ui, LX lx, ParameterFile paramFile) {
    super(ui, title, filename, paramFile);
    int contentWidth = (int)ui.leftPane.global.getContentWidth();
    this.lx = lx;

    // Run 1
    registerStringParameter(STRAND1, "20");
    registerStringParameter(STRAND2, "20");
    // Run 2
    registerStringParameter(STRAND3, "20");
    registerStringParameter(STRAND4, "20");

    // Run 3
    registerStringParameter(STRAND5, "20");
    // For now, we will make the final strand on the third run of Butterflies have one less butterfly
    registerStringParameter(STRAND6, "10");

    // Three runs of flowers with each run having 5 flowers aka 10 addressable LEDs on a single strand
    // Run 1
    registerStringParameter(STRAND7, "5");
    // Run 2
    registerStringParameter(STRAND8, "5");
    // Run 3
    registerStringParameter(STRAND9, "5");
    // Run 4
    registerStringParameter(STRAND10, "4");

    registerStringParameter(STRAND11, "0");
    registerStringParameter(STRAND12, "0");
    registerStringParameter(STRAND13, "0");
    registerStringParameter(STRAND14, "0");
    registerStringParameter(STRAND15, "0");
    registerStringParameter(STRAND16, "0");

    save();

    buildUI(ui);
  }

  static public List<Integer> getAllStrandLengths(ParameterFile pFile) {
    List<Integer> lengths = new ArrayList<Integer>(0);
    // Butterfly Run 1
    lengths.add(Integer.parseInt(pFile.getStringParameter(STRAND1, "20").getString()));
    lengths.add(Integer.parseInt(pFile.getStringParameter(STRAND2, "20").getString()));
    // Run 2
    lengths.add(Integer.parseInt(pFile.getStringParameter(STRAND3, "20").getString()));
    lengths.add(Integer.parseInt(pFile.getStringParameter(STRAND4, "20").getString()));
    // Run 3
    lengths.add(Integer.parseInt(pFile.getStringParameter(STRAND5, "20").getString()));
    lengths.add(Integer.parseInt(pFile.getStringParameter(STRAND6, "10").getString()));

    // Flowers Run 1
    lengths.add(Integer.parseInt(pFile.getStringParameter(STRAND7, "5").getString()));
    // Flowers Run 2
    lengths.add(Integer.parseInt(pFile.getStringParameter(STRAND8, "5").getString()));
    // Flowers Run 3
    lengths.add(Integer.parseInt(pFile.getStringParameter(STRAND9, "5").getString()));
    // Flowers run 4
    lengths.add(Integer.parseInt(pFile.getStringParameter(STRAND10, "4").getString()));

    lengths.add(Integer.parseInt(pFile.getStringParameter(STRAND11, "0").getString()));
    lengths.add(Integer.parseInt(pFile.getStringParameter(STRAND12, "0").getString()));
    lengths.add(Integer.parseInt(pFile.getStringParameter(STRAND13, "0").getString()));
    lengths.add(Integer.parseInt(pFile.getStringParameter(STRAND14, "0").getString()));
    lengths.add(Integer.parseInt(pFile.getStringParameter(STRAND15, "0").getString()));
    lengths.add(Integer.parseInt(pFile.getStringParameter(STRAND16, "0").getString()));
    return lengths;
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
