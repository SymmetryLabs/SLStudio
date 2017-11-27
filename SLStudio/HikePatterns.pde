import java.util.concurrent.Semaphore;
import java.util.Collections;

public abstract class ThreadedPattern extends LXPattern {
  private static final int DEFAULT_THREAD_COUNT = 4;

  private List<RenderThread> renderThreads = new ArrayList<RenderThread>();

  //public final MutableParameter threadCount = new MutableParameter("thread-count", 0);

  public ThreadedPattern(LX lx) {
    this(lx, DEFAULT_THREAD_COUNT);
  }

  public ThreadedPattern(LX lx, int numThreads) {
    super(lx);

    for (int i = renderThreads.size(); i < numThreads; ++i) {
      RenderThread rt = new RenderThread(i);
      renderThreads.add(rt);
      rt.start();
    }
  }

  protected int render(LXPoint p) {
    return LXColor.BLACK;
  }

  protected int[] render(List<LXPoint> cs) {
    int[] pointColors = new int[cs.size()];
    for (int i = 0; i < cs.size(); ++i) {
      pointColors[i] = render(cs.get(i));
    }
    return pointColors;
  }

  @Override
  public void run(double deltaMs) {
    for (RenderThread rt : renderThreads) {
      rt.startRender();
    }
    for (RenderThread rt : renderThreads) {
      rt.waitFinished();
    }
  }

  private class RenderThread extends Thread {
    private final int index;
    private boolean running = true;

    private final Semaphore triggerRender = new Semaphore(0);
    private final Semaphore waitRender = new Semaphore(0);

    public RenderThread(int index) {
      this.index = index;
    }

    public void startRender() {
      triggerRender.release();
    }

    public void waitFinished() {
      try {
        waitRender.acquire();
      }
      catch (InterruptedException e) { /* pass */ }
    }

    public void shutdown() {
      running = false;
      waitRender.release();
      interrupt();
    }

    @Override
    public void run() {
      while (running) {
        try {
          triggerRender.acquire();
        }
        catch (InterruptedException e) {
          continue;
        }

        int numThreads = renderThreads.size();
        int pointCount = lx.model.points.length;
        List<LXPoint> points = new ArrayList<LXPoint>();
        for (int i = pointCount * index / numThreads; i < pointCount * (index + 1) / numThreads; ++i) {
          LXPoint p = lx.model.points[i];
          points.add(p);
        }

        int[] pointColors = render(points);
        for (int i = 0; i < points.size(); ++i) {
          LXPoint p = points.get(i);
          colors[p.index] = pointColors[i];
        }

        waitRender.release();
      }
    }
  }
}

public abstract class ParticlePattern extends ThreadedPattern {
  private static final int DEFAULT_PARTICLE_COUNT = 10;
  private final double SQRT_2PI = Math.sqrt(2 * Math.PI);

  public final BoundedParameter particleCount = new BoundedParameter("count", 0, 0, 100);
  public final CompoundParameter kernelSize = new CompoundParameter("size", 100, 0, 400);

  public final CompoundParameter hue = new CompoundParameter("hue", 0, 0, 360);
  public final CompoundParameter saturation = new CompoundParameter("saturation", 30, 0, 100);

  protected List<Particle> particles = new ArrayList<Particle>();
  protected ModelIndex modelIndex;

  private SimulationThread simThread;
  private float[] brightnessBuffer;

  public ParticlePattern(LX lx) {
    this(lx, DEFAULT_PARTICLE_COUNT);
  }

  public ParticlePattern(LX lx, int numParticles) {
    super(lx);

    brightnessBuffer = new float[colors.length];

    //modelIndex = new KDTreeIndex(lx.model);
    //modelIndex = new LinearIndex(lx.model);
    modelIndex = new OctreeIndex(lx.model);

    addParameter(particleCount);
    addParameter(kernelSize);

    addParameter(hue);
    addParameter(saturation);

    particleCount.addListener(new LXParameterListener() {
      public void onParameterChanged(LXParameter particleCount) {
        int numParticles = (int)particleCount.getValue();
        while (particles.size() > numParticles) {
          particles.remove(particles.size() - 1);
        }

        for (int i = particles.size(); i < numParticles; ++i) {
          Particle p = new Particle();
          initParticle(p);
          particles.add(p);
        }
      }
    });

    particleCount.setValue(numParticles);

    simThread = new SimulationThread();
  }

  @Override
  public void onActive() {
    simThread.start();
  }

  @Override
  public void onInactive() {
    simThread.shutdown();
    simThread = new SimulationThread();
  }

  protected float kernel(float d, float s) {
    double stddev = s / 4f;
    double peak = 1.0f / (2.5f * stddev);
    return (float)(Math.exp(-(d * d) / (2 * stddev * stddev))
              / (stddev * SQRT_2PI) / peak);
  }

  protected float kernel(float x, float y, float z, float s) {
    return kernel((float)Math.sqrt(x * x + y * y + z * z), s);
  }

  protected void initParticle(Particle p) { }
  protected abstract void simulate(double deltaMs);

  @Override
  public synchronized void run(double deltaMs) {
    for (int i = 0; i < brightnessBuffer.length; ++i) {
      brightnessBuffer[i] = 0f;
    }

    particles.parallelStream().forEach(new Consumer<Particle>() {
      @Override
      public void accept(final Particle particle) {
        LXPoint pp = particle.toPointInModel(lx.model);
        float withinDist = particle.size * kernelSize.getValuef();
        List<ModelIndex.PointDist> pointDists = modelIndex.pointsWithin(pp, withinDist);
        for (ModelIndex.PointDist pointDist : pointDists) {
          brightnessBuffer[pointDist.p.index] += kernel(
            pp.x - pointDist.p.x,
            pp.y - pointDist.p.y,
            pp.z - pointDist.p.z,
            withinDist
          );
        }
      }
    });

    super.run(deltaMs);
  }

  @Override
  public int[] render(List<LXPoint> points) {
    double h = hue.getValue();
    double s = saturation.getValue();
    int[] pointColors = new int[points.size()];
    for (int i = 0; i < points.size(); ++i) {
      LXPoint p = points.get(i);
      float b = Math.min(brightnessBuffer[p.index] * 100, 100);
      pointColors[i] = LXColor.hsb(h, s, b);
    }
    return pointColors;
  }

  protected class Particle {
    public float[] pos = new float[3];
    public float[] vel = new float[3];
    public float size = 1;

    LXPoint toPointInModel(LXModel model) {
      float x = model.cx + pos[0] * model.xRange / 2f;
      float y = model.cy + pos[1] * model.yRange / 2f;
      float z = model.cz + pos[2] * model.zRange / 2f;
      return new LXPoint(x, y, z);
    }
  }

  private class SimulationThread extends Thread {
    private final int PERIOD = 8;

    private boolean running = true;

    public void shutdown() {
      running = false;
      interrupt();
    }

    @Override
    public void run() {
      long lastTime = System.currentTimeMillis();
      while (running) {
        long elapsed = System.currentTimeMillis() - lastTime;
        if (elapsed < PERIOD) {
          try {
            sleep(PERIOD - elapsed);
          }
          catch (InterruptedException e) { /* pass */ }
        }

        long t = System.currentTimeMillis();
        double deltaMs = t - lastTime;
        lastTime = t;

        synchronized (ParticlePattern.this) {
          simulate(deltaMs);
        }
      }
    }
  }
}

public class Wasps extends ParticlePattern {
  private final double SQRT_2PI = Math.sqrt(2 * Math.PI);

  public final CompoundParameter accel = new CompoundParameter("accel", 0.15f, 0, 1);
  public final CompoundParameter dampen = new CompoundParameter("dampen", 0.75f, 0, 1);
  public final CompoundParameter gravity = new CompoundParameter("gravity", 0.5f, 0, 1);
  public final CompoundParameter focusX = new CompoundParameter("focusX", 0.5f, 0, 1);
  public final CompoundParameter focusY = new CompoundParameter("focusY", 0.5f, 0, 1);
  public final CompoundParameter focusZ = new CompoundParameter("focusZ", 0.5f, 0, 1);

  public Wasps(LX lx) {
    this(lx, 10);
  }

  public Wasps(LX lx, int countValue) {
    super(lx, countValue);

    addParameter(accel);
    addParameter(dampen);
    addParameter(gravity);
    addParameter(focusX);
    addParameter(focusY);
    addParameter(focusZ);
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
    float timeBoost = 30;
    float timeStep = 1;//timeBoost * (float)deltaMs / 1000f;

    float accelValue = 0.04f * accel.getValuef() * timeStep;
    float dampenValue = 0.05f * dampen.getValuef();
    float gravityValue = 0.0005f * gravity.getValuef();

    for (int i = 0; i < particles.size(); ++i) {
      Particle p = particles.get(i);

      float fx = gravityValue * (p.pos[0] - 2 * focusX.getValuef() + 1);
      float fy = gravityValue * (p.pos[1] - 2 * focusY.getValuef() + 1);
      float fz = gravityValue * (p.pos[2] - 2 * focusZ.getValuef() + 1);

      p.vel[0] += (float)(accelValue * (Math.random() - .5) - fx - dampenValue * p.vel[0]);
      p.vel[1] += (float)(accelValue * (Math.random() - .5) - fy - dampenValue * p.vel[1]);
      p.vel[2] += (float)(accelValue * (Math.random() - .5) - fz - dampenValue * p.vel[2]);

      /*
      if (i > i / 2 && i / 2 < particles.size()) {
        Particle leader = particles.get(i / 2);
        p.vel[0] -= gravityValue * (p.pos[0] - leader.pos[0]);
        p.vel[1] -= gravityValue * (p.pos[1] - leader.pos[1]);
        p.vel[2] -= gravityValue * (p.pos[2] - leader.pos[2]);
      }

      if (i > i / 2 + 1 && i / 2 + 1 < particles.size()) {
        Particle enemy = particles.get(i / 2 + 1);
        p.vel[0] += gravityValue * (p.pos[0] - enemy.pos[0]);
        p.vel[1] += gravityValue * (p.pos[1] - enemy.pos[1]);
        p.vel[2] += gravityValue * (p.pos[2] - enemy.pos[2]);
      }
      */

      p.pos[0] += p.vel[0] * timeStep;
      p.pos[1] += p.vel[1] * timeStep;
      p.pos[2] += p.vel[2] * timeStep;

      //p.size = (float)Math.min(1 + 50000 * Math.abs(p.vel[0] * p.vel[1] * p.vel[2]), 10);
      //p.size = (float)Math.min(1 + 1000 * Math.abs(p.vel[0] * p.vel[2]), 10);
    }
  }
}
