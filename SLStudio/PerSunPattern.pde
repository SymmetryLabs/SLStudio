import com.symmetrylabs.render.Renderer;
import com.symmetrylabs.render.Renderable;
import com.symmetrylabs.render.SequentialRenderer;
import com.symmetrylabs.render.InterpolatingRenderer;

public abstract static class PerSunPattern extends SLPattern {
  protected List<Subpattern> subpatterns;

  private Object rendererLock = new Object();
  private Renderer renderer;

  public static enum RendererChoices {
    SEQUENTIAL, INTERPOLATING
  };

  EnumParameter<RendererChoices> chooseRenderer;

  private Renderable renderable = new Renderable() {
    @Override
    public void render(final double deltaMs, List<LXPoint> ignore, final int[] layer) {
      // System.out.println(1000 / deltaMs);
      subpatterns.parallelStream().forEach(new Consumer<Subpattern>() {
        public void accept(Subpattern subpattern) {
          if (subpattern.enableParam.getValueb()) {
            subpattern.run(deltaMs, subpattern.sun.getPoints(), layer);
          }
          else {
            for (LXPoint point : subpattern.sun.points) {
              layer[point.index] = 0;
            }
          }
        }
      });
    }
  };

  protected void createParameters() { }

  protected PerSunPattern(final LX lx, Class<? extends Subpattern> subpatternClass) {
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

    addParameter(chooseRenderer = new EnumParameter<RendererChoices>("renderer", RendererChoices.SEQUENTIAL));

    renderer = new SequentialRenderer(lx.model, colors, renderable);

    chooseRenderer.addListener(new LXParameterListener() {
      public void onParameterChanged(LXParameter ignore) {
        synchronized (rendererLock) {
          renderer.stop();

          switch (chooseRenderer.getEnum()) {
          case SEQUENTIAL:
            renderer = new SequentialRenderer(lx.model, colors, renderable);
            break;
          case INTERPOLATING:
            renderer = new InterpolatingRenderer(lx.model, colors, renderable);
            break;
          }

          renderer.start();
        }
      }
    });
  }

  @Override
  public void onActive() {
    super.onActive();

    synchronized (rendererLock) {
      renderer.start();
    }
  }

  @Override
  public void onInactive() {
    super.onInactive();

    synchronized (rendererLock) {
      renderer.stop();
    }
  }

  @Override
  public void run(final double deltaMs) {
    renderer.run(deltaMs);
    /*
    subpatterns.parallelStream().forEach(new Consumer<Subpattern>() {
      public void accept(Subpattern subpattern) {
        if (subpattern.enableParam.getValueb()) {
          subpattern.run(deltaMs, subpattern.sun.getPoints(), colors);
        }
        else {
          for (LXPoint point : subpattern.sun.points) {
            colors[point.index] = 0;
          }
        }
      }
    });
    */
  }

  public static abstract class Subpattern {
    protected final Sun sun;
    protected final int sunIndex;
    protected final BooleanParameter enableParam;

    public Subpattern(Sun sun, int sunIndex) {
      this.sun = sun;
      this.sunIndex = sunIndex;

      enableParam = new BooleanParameter("SUN" + (sunIndex+1), true);
    }

    protected abstract void run(double deltaMs, List<LXPoint> points, int[] layer);
  }
}
