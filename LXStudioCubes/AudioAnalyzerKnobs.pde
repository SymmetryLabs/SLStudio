public class UIAudioAnalyzerKnobs extends UIModulator {

  private final static int TOP_PADDING = 4;

  public UIAudioAnalyzerKnobs(UI ui, LX lx, AudioAnalyzerKnobs audioAnalyzerKnobs, float x, float y, float w) {
    super(ui, lx, audioAnalyzerKnobs, x, y, w, UIKnob.HEIGHT + TOP_PADDING);
    setLayout(UI2dContainer.Layout.HORIZONTAL);
    setChildMargin(0);
    new UIKnob(audioAnalyzerKnobs.rms).setY(TOP_PADDING).addToContainer(this);
    new UIKnob(audioAnalyzerKnobs.pitch).setY(TOP_PADDING).addToContainer(this);
    new UIKnob(audioAnalyzerKnobs.spectrum).setY(TOP_PADDING).addToContainer(this);
    new UIKnob(audioAnalyzerKnobs.onset).setY(TOP_PADDING).addToContainer(this);
    new UIKnob(audioAnalyzerKnobs.mfcc).setY(TOP_PADDING).addToContainer(this);
    new UIKnob(audioAnalyzerKnobs.centroid).setY(TOP_PADDING).addToContainer(this);
  }

  @Override
  protected UIModulationSource getModulationSourceUI() {
    return null;
  }
}

public class AudioAnalyzerKnobs extends LXModulator {

  public final BoundedParameter rms = new BoundedParameter("rms")
    .setDescription("rms parameter");

  public final BoundedParameter pitch = new BoundedParameter("pitch")
    .setDescription("pitch parameter");

  public final BoundedParameter spectrum = new BoundedParameter("spectrum")
    .setDescription("spectrum parameter");

  public final BoundedParameter onset = new BoundedParameter("onset")
    .setDescription("onset parameter");

  public final BoundedParameter mfcc = new BoundedParameter("mfss")
    .setDescription("mfss parameter");

  public final BoundedParameter centroid = new BoundedParameter("centroid")
    .setDescription("centroid parameter");

  public AudioAnalyzerKnobs(String label) {
    super(label);
    addParameter("rms", this.rms);
    addParameter("pitch", this.pitch);
    addParameter("spectrum", this.spectrum);
    addParameter("onset", this.onset);
    addParameter("mfcc", this.mfcc);
    addParameter("centroid", this.centroid);
  }

  @Override
  protected double computeValue(double deltaMs) {
    // Not relevant
    return 0;
  }

}