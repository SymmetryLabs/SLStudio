
public static class SunScreen extends SLPattern {
  List<BooleanParameter> params = new ArrayList<BooleanParameter>();

  public SunScreen(LX lx) {
    super(lx);

    for (int i = 0; i < model.suns.size(); ++i) {
      BooleanParameter param = new BooleanParameter("SUN"+i, true);
      params.add(param);
      addParameter(param);
    }

    LXParameterListener updateScreenListener = new LXParameterListener() {
      private boolean inProgress = false;

      public void onParameterChanged(LXParameter param) {
        boolean on = ((BooleanParameter)param).isOn();
        int sunIndex = params.indexOf(param);
        for (LXPoint p : model.suns.get(sunIndex).getPoints()) {
          colors[p.index] = on ? LXColor.WHITE : LXColor.BLACK;
        }
      }
    };

    Arrays.fill(colors, LXColor.WHITE);

    for (BooleanParameter param : params) {
      param.addListener(updateScreenListener);
    }
  }

  public void run(double deltaMs) { }
}
