
public abstract static class PerSunPattern extends SLPattern {
  private List<Subpattern> subpatterns;

  protected PerSunPattern(LX lx, Class<? extends Subpattern> subpatternClass, int faceRes) {
    super(lx);

    subpatterns = new ArrayList<Subpattern>(model.suns.size());

    boolean isNonStaticInnerClass = (subpatternClass.isMemberClass() || subpatternClass.isLocalClass())
        && !Modifier.isStatic(subpatternClass.getModifiers());

    int sunIndex = 0;
    for (Sun sun : model.suns) {
      try {
        Subpattern subpattern;
        if (isNonStaticInnerClass) {
          subpattern = subpatternClass.getDeclaredConstructor(getClass()).newInstance(this);
        }
        else {
          subpattern = subpatternClass.getDeclaredConstructor().newInstance();
        }

        subpattern.init(lx, colors, sun, faceRes, sunIndex);

        subpatterns.add(subpattern);
      }
      catch (Exception e) {
        System.err.println("Exception when creating subpattern: " + e.getLocalizedMessage());
        e.printStackTrace();
      }

      ++sunIndex;
    }
  }

  @Override
  public void run(final double deltaMs) {
    subpatterns.parallelStream().forEach(new Consumer<Subpattern>() {
      public void accept(Subpattern subpattern) {
        if (subpattern.enableParam.getValueb()) {
          subpattern.run(deltaMs);
        }
        else {
          for (LXPoint point : subpattern.sun.points) {
            colors[point.index] = 0;
          }
        }
      }
    });
  }

  public abstract static class Subpattern {
    protected LX lx;
    private int[] colors;
    private Sun sun;

    private PGraphics pg;
    protected PGraphics pgF, pgB, pgL, pgR, pgU, pgD;

    protected int sunIndex;
    protected BooleanParameter enableParam;

    private void init(LX lx, int[] colors, Sun sun, int faceRes, int sunIndex) {
      this.lx = lx;
      this.colors = colors;
      this.sun = sun;
      this.sunIndex = sunIndex;

      enableParam = new BooleanParameter("SUN" + (sunIndex+1));
    }

    protected abstract void run(double deltaMs);
  }
}
