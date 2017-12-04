
import com.symmetrylabs.util.LayeredRenderer;
import com.symmetrylabs.util.OctreeModelIndex;
import com.symmetrylabs.util.LinearModelIndex;

public abstract static class PerSunParticlePattern extends PerSunPattern {
  private final double SQRT_2PI = Math.sqrt(2 * Math.PI);

  public BoundedParameter particleCount;
  public CompoundParameter kernelSize;
  public BooleanParameter flattenZ;

  public CompoundParameter hue;
  public CompoundParameter saturation;

  public PerSunParticlePattern(LX lx, Class<? extends Subpattern> subpatternClass) {
    super(lx, subpatternClass);
  }

  @Override
  protected void createParameters() {
    addParameter(particleCount = new BoundedParameter("count", 0, 0, 100));
    addParameter(kernelSize = new CompoundParameter("size", 10, 0, 100));
    addParameter(flattenZ = new BooleanParameter("flattenZ", true));

    addParameter(hue = new CompoundParameter("hue", 0, 0, 360));
    addParameter(saturation = new CompoundParameter("saturation", 30, 0, 100));
  }

  protected float kernel(double d, double s) {
    double stddev = s / 4f;
    double peak = 1.0f / (2.5f * stddev);
    return (float)(FastMath.exp(-(d * d) / (2 * stddev * stddev))
              / (stddev * SQRT_2PI) / peak);
  }

  protected float kernel(double x, double y, double z, double s) {
    return kernel(FastMath.sqrt(x * x + y * y + z * z), s);
  }

  protected int getPaletteColor(float val) {
    double h = hue.getValue();
    double s = saturation.getValue();
    return LXColor.hsb(h, s, FastMath.min(val * 100, 100));
  }

  protected static class Particle {
    public double[] pos = new double[3];
    public double[] vel = new double[3];
    public float size = 1;

    public final float[] layer;
    public final int index;

    private LXPoint point = new LXPoint(0, 0, 0);

    public Particle(int index, int pointCount) {
      this.index = index;

      layer = new float[pointCount];
    }

    public synchronized LXPoint toPointInModel(LXModel model) {
      float x = (float)(model.cx + pos[0] * model.xRange / 2f);
      float y = (float)(model.cy + pos[1] * model.yRange / 2f);
      float z = (float)(model.cz + pos[2] * model.zRange / 2f);
      return point.update(x, y, z);
    }
  }

  protected abstract class Subpattern extends PerSunPattern.Subpattern {
    protected ModelIndex modelIndex;
    protected List<Particle> particles = new ArrayList<Particle>();

    protected void initParticle(Particle p) { }
    protected abstract void simulate(double deltaMs);

    public Subpattern(Sun sun, int sunIndex) {
      super(sun, sunIndex);

      modelIndex = createModelIndex();

      particleCount.addListener(new LXParameterListener() {
        public synchronized void onParameterChanged(LXParameter particleCount) {
          int numParticles = (int)particleCount.getValue();
          while (particles.size() > numParticles) {
            particles.remove(particles.size() - 1);
          }

          for (int i = particles.size(); i < numParticles; ++i) {
            Particle p = new Particle(i, colors.length);
            initParticle(p);
            particles.add(p);
          }
        }
      });

      flattenZ.addListener(new LXParameterListener() {
        public void onParameterChanged(LXParameter param) {
          Subpattern.this.modelIndex = createModelIndex();
        }
      });
    }

    protected ModelIndex createModelIndex() {
      return new LinearModelIndex(sun, flattenZ.isOn());
    }

    protected void renderParticle(Particle particle) {
      Arrays.fill(particle.layer, 0f);

      LXPoint pp = particle.toPointInModel(sun);
      float withinDist = particle.size * kernelSize.getValuef();
      List<LXPoint> nearbyPoints = modelIndex.pointsWithin(pp, withinDist);
      //System.out.println(nearbyPoints.size());
      //System.out.println("pos: {" + particle.pos[0] + ", " + particle.pos[1] + ", " + particle.pos[2] + "}");
      //System.out.println("pp: {" + pp.x + ", " + pp.y + ", " + pp.z + "}");
      //System.out.println("center: {" + sun.cx + ", " + sun.cy + ", " + sun.cz + "}");

      final boolean flattening = flattenZ.isOn();
      for (LXPoint p : nearbyPoints) {
        float b = kernel(pp.x - p.x, pp.y - p.y, flattening ? 0 : pp.z - p.z, withinDist);
        particle.layer[p.index] = b;
      }
    }

    @Override
    void run(double deltaMs) {
      simulate(deltaMs);

      particles.parallelStream().forEach(new Consumer<Particle>() {
        public void accept(Particle particle) {
          renderParticle(particle);
        }
      });

      for (LXPoint point : sun.getPoints()) {
        float s = 0;
        for (Particle particle : particles) {
          s += particle.layer[point.index];
        }

        colors[point.index] = getPaletteColor(s);
      }
    }
  }
}

public static class PerSunWasps extends PerSunParticlePattern {
  private final double SQRT_2PI = Math.sqrt(2 * Math.PI);

  public CompoundParameter speed;
  public CompoundParameter accel;
  public CompoundParameter dampen;
  public CompoundParameter focusX;
  public CompoundParameter focusY;
  public CompoundParameter focusZ;
  public CompoundParameter pullX;
  public CompoundParameter pullY;
  public CompoundParameter pullZ;
  public CompoundParameter twistX;
  public CompoundParameter twistY;
  public CompoundParameter twistZ;

  public PerSunWasps(LX lx) {
    super(lx, Subpattern.class);
  }

  @Override
  protected void createParameters() {
    super.createParameters();

    addParameter(speed = new CompoundParameter("speed", 1, 0, 5));
    addParameter(accel = new CompoundParameter("accel", 0.15f, 0, 5));
    addParameter(dampen = new CompoundParameter("dampen", 0.75f, 0, 1));
    addParameter(focusX = new CompoundParameter("focusX", 0f, -1, 1));
    addParameter(focusY = new CompoundParameter("focusY", 0f, -1, 1));
    addParameter(focusZ = new CompoundParameter("focusZ", 0f, -1, 1));
    addParameter(pullX = new CompoundParameter("pullX", 0f, 0, 1));
    addParameter(pullY = new CompoundParameter("pullY", 0f, 0, 1));
    addParameter(pullZ = new CompoundParameter("pullZ", 0f, 0, 1));
    addParameter(twistX = new CompoundParameter("twistX", 0f, 0, 1));
    addParameter(twistY = new CompoundParameter("twistY", 0f, 0, 1));
    addParameter(twistZ = new CompoundParameter("twistZ", 0f, 0, 1));
  }

  protected class Subpattern extends PerSunParticlePattern.Subpattern {

    public Subpattern(Sun sun, int sunIndex) {
      super(sun, sunIndex);
    }

    @Override
    protected void initParticle(Particle p) {
      p.pos[0] = (float)(2 * Math.random() - 1);
      p.pos[1] = (float)(2 * Math.random() - 1);
      p.pos[2] = (float)(2 * Math.random() - 1);
      //System.out.println("[" + p.pos[0] + ", " + p.pos[1] + ", " + p.pos[2] + "]");
    }

    @Override
    protected void simulate(double deltaMs) {
      double timeBoost = 30;
      double timeStep = timeBoost * deltaMs / 1000f;

      double speedValue = speed.getValuef();
      double accelValue = 0.01 * accel.getValue() * timeStep;
      double dampenValue = 0.05 * dampen.getValue();

      double pullXValue = 0.01 * pullX.getValue();
      double pullYValue = 0.01 * pullY.getValue();
      double pullZValue = 0.01 * pullZ.getValue();

      double twistXValue = 0.01 * twistX.getValue();
      double twistYValue = 0.01 * twistY.getValue();
      double twistZValue = 0.01 * twistZ.getValue();

      for (int i = 0; i < particles.size(); ++i) {
        Particle p = particles.get(i);

        p.vel[0] -= dampenValue * p.vel[0];
        p.vel[1] -= dampenValue * p.vel[1];
        p.vel[2] -= dampenValue * p.vel[2];

        p.vel[0] += accelValue * (Math.random() - .5);
        p.vel[1] += accelValue * (Math.random() - .5);
        p.vel[2] += accelValue * (Math.random() - .5);

        double pullVecX = focusX.getValuef() - p.pos[0];
        double pullVecY = focusY.getValuef() - p.pos[1];
        double pullVecZ = focusZ.getValuef() - p.pos[2];

        p.vel[0] += pullXValue * pullVecX;
        p.vel[1] += pullYValue * pullVecY;
        p.vel[2] += pullZValue * pullVecZ;

        // NOTE: assuming left-handed Z-axis

        double pullNorm = Math.sqrt(pullVecX * pullVecX + pullVecY * pullVecY + pullVecZ * pullVecZ);

        double twistXVecX = 0;
        double twistXVecY = pullVecZ / pullNorm;
        double twistXVecZ = -pullVecY / pullNorm;

        double twistYVecX = -pullVecZ / pullNorm;
        double twistYVecY = 0;
        double twistYVecZ = pullVecX / pullNorm;

        double twistZVecX = -pullVecY / pullNorm;
        double twistZVecY = pullVecX / pullNorm;
        double twistZVecZ = 0;

        p.vel[0] += twistXValue * twistXVecX;
        p.vel[1] += twistXValue * twistXVecY;
        p.vel[2] += twistXValue * twistXVecZ;

        p.vel[0] += twistYValue * twistYVecX;
        p.vel[1] += twistYValue * twistYVecY;
        p.vel[2] += twistYValue * twistYVecZ;

        p.vel[0] += twistZValue * twistZVecX;
        p.vel[1] += twistZValue * twistZVecY;
        p.vel[2] += twistZValue * twistZVecZ;

        p.pos[0] += p.vel[0] * speedValue * timeStep;
        p.pos[1] += p.vel[1] * speedValue * timeStep;
        p.pos[2] += p.vel[2] * speedValue * timeStep;

        if (p.pos[0] < -1) p.pos[0] = -1;
        if (p.pos[0] > 1) p.pos[0] = 1;
        if (p.pos[1] < -1) p.pos[1] = -1;
        if (p.pos[1] > 1) p.pos[1] = 1;
        if (p.pos[2] < -1) p.pos[2] = -1;
        if (p.pos[2] > 1) p.pos[2] = 1;
      }
    }
  }
}
