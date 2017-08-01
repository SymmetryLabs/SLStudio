import heronarts.p3lx.ui.studio.modulation.UIModulator;

public class UIAudioAnalyzerKnobs extends UIModulator {

  private final static int TOP_PADDING = 4;
  private final static int X_SPACING = UIKnob.WIDTH + 1;
  private final static int Y_SPACING = UIKnob.HEIGHT + TOP_PADDING;

  public UIAudioAnalyzerKnobs(UI ui, LX lx, AudioAnalyzerKnobs audioAnalyzerKnobs, float x, float y, float w) {
    super(ui, lx, audioAnalyzerKnobs, true, x, y, w, (UIKnob.HEIGHT + TOP_PADDING)*2f);
    //setLayout(UI2dContainer.Layout.HORIZONTAL);
    setChildMargin(0);
    new UIKnob(audioAnalyzerKnobs.rms)
      .setY(TOP_PADDING).setX(X_SPACING*0).addToContainer(this);

    new UIKnob(audioAnalyzerKnobs.power)
      .setY(TOP_PADDING).setX(X_SPACING*1).addToContainer(this);

    new UIKnob(audioAnalyzerKnobs.pitchConfidence)
      .setY(TOP_PADDING).setX(X_SPACING*2).addToContainer(this);

    new UIKnob(audioAnalyzerKnobs.pitchSalience)
      .setY(TOP_PADDING).setX(X_SPACING*3).addToContainer(this);

    new UIKnob(audioAnalyzerKnobs.inharmonicity)
      .setY(TOP_PADDING).setX(X_SPACING*4).addToContainer(this);

    new UIKnob(audioAnalyzerKnobs.hfc)
      .setY(TOP_PADDING).setX(X_SPACING*5).addToContainer(this);

    //new UIKnob(audioAnalyzerKnobs.dissonance)
      //.setY(TOP_PADDING).addToContainer(this);

    new UIKnob(audioAnalyzerKnobs.centroid)
      .setY(TOP_PADDING+(Y_SPACING)).setX(X_SPACING*0).addToContainer(this);

    new UIKnob(audioAnalyzerKnobs.strongDecay)
      .setY(TOP_PADDING+(Y_SPACING)).setX(X_SPACING*1).addToContainer(this);

    new UIKnob(audioAnalyzerKnobs.strongPeak)
      .setY(TOP_PADDING+(Y_SPACING)).setX(X_SPACING*2).addToContainer(this);

    new UIKnob(audioAnalyzerKnobs.rollOff)
      .setY(TOP_PADDING+(Y_SPACING)).setX(X_SPACING*3).addToContainer(this);

    new UIKnob(audioAnalyzerKnobs.spectralComplexity)
      .setY(TOP_PADDING+(Y_SPACING)).setX(X_SPACING*4).addToContainer(this);

    new UIKnob(audioAnalyzerKnobs.onset)
      .setY(TOP_PADDING+(Y_SPACING)).setX(X_SPACING*5).addToContainer(this);
  }

  @Override
  protected UIModulationSource getModulationSourceUI() {
    return null;
  }
}

public class AudioAnalyzerKnobs extends LXModulator {

  // normalized
  public final BoundedParameter rms = new BoundedParameter("rms")
    .setDescription("rms parameter");

  public final BoundedParameter power = new BoundedParameter("power")
    .setDescription("power parameter");

  public final BoundedParameter pitchConfidence = new BoundedParameter("ptch confidence")
    .setDescription("pitch confidence parameter");

  public final BoundedParameter pitchSalience = new BoundedParameter("ptch salience")
    .setDescription("pitch salience parameter");

  public final BoundedParameter inharmonicity = new BoundedParameter("inharmonicity")
    .setDescription("inharmonicity parameter");

  // public final BoundedParameter dissonance = new BoundedParameter("dissonance")
  //   .setDescription("dissonance parameter");

  public final BoundedParameter hfc = new BoundedParameter("hfc")
    .setDescription("hfc norm parameter");

  public final BoundedParameter centroid = new BoundedParameter("centroid")
    .setDescription("centroid parameter");

  public final BoundedParameter strongDecay = new BoundedParameter("str decay")
    .setDescription("strong decay parameter");

  public final BoundedParameter strongPeak = new BoundedParameter("str peak")
    .setDescription("strong peak parameter");

  public final BoundedParameter rollOff = new BoundedParameter("roll off")
    .setDescription("roll off parameter");

  public final BoundedParameter spectralComplexity = new BoundedParameter("spectral complexity")
    .setDescription("spectral complexity parameter");

  public final BoundedParameter onset = new BoundedParameter("onset")
    .setDescription("onset parameter");

  public AudioAnalyzerKnobs(String label) {
    super(label);
    addParameter("rms", this.rms);
    addParameter("power", this.power);
    addParameter("ptch confidence", this.pitchConfidence);
    addParameter("ptch salience", this.pitchSalience);
    addParameter("inharmonicity", this.inharmonicity);
    //addParameter("dissonance", this.dissonance);
    addParameter("hfc", this.hfc);
    addParameter("centroid", this.centroid);
    addParameter("str decay", this.strongDecay);
    addParameter("str peak", this.strongPeak);
    addParameter("roll off", this.rollOff);
    addParameter("spec complexity", this.spectralComplexity);
    addParameter("onset", this.onset);
  }

  @Override
  protected double computeValue(double deltaMs) {
    // Not relevant
    return 0;
  }

}
