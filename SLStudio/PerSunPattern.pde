
public abstract static class PerSunPattern extends SLPattern {
  protected List<Subpattern> subpatterns;

  protected void createParameters() { }

  protected PerSunPattern(LX lx, Class<? extends Subpattern> subpatternClass) {
    super(lx);

    subpatterns = new ArrayList<Subpattern>(model.suns.size());

    createParameters();

    boolean isNonStaticInnerClass = (subpatternClass.isMemberClass() || subpatternClass.isLocalClass())
        && !Modifier.isStatic(subpatternClass.getModifiers());

    int sunIndex = 0;
    for (Sun sun : model.suns) {
      try {
        Subpattern subpattern;
        if (isNonStaticInnerClass) {
          subpattern = subpatternClass.getDeclaredConstructor(getClass(), Sun.class, int.class).newInstance(this, sun, sunIndex);
        }
        else {
          subpattern = subpatternClass.getConstructor(Sun.class, int.class).newInstance(sun, sunIndex);
        }

        addParameter(subpattern.enableParam);

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

  public static abstract class Subpattern {
    protected final Sun sun;
    protected final int sunIndex;
    protected final BooleanParameter enableParam;

    Subpattern(Sun sun, int sunIndex) {
      this.sun = sun;
      this.sunIndex = sunIndex;

      enableParam = new BooleanParameter("SUN" + (sunIndex+1), true);
    }

    protected abstract void run(double deltaMs);
  }
}
