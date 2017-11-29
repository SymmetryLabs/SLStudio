package com.symmetrylabs.pattern;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;
import java.util.function.Consumer;
import java.nio.IntBuffer;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.model.LXModel;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.color.LXColor;

import com.symmetrylabs.util.ModelIndex;
import com.symmetrylabs.util.OctreeModelIndex;

public abstract class ParticlePattern extends ThreadedPattern {
    private static final int DEFAULT_PARTICLE_COUNT = 10;
    private final double SQRT_2PI = Math.sqrt(2 * Math.PI);

    public final BoundedParameter particleCount = new BoundedParameter("count", 0, 0, 100);
    public final CompoundParameter kernelSize = new CompoundParameter("size", 100, 0, 400);
    public final BooleanParameter flattenZ = new BooleanParameter("flattenZ", true);

    public final CompoundParameter hue = new CompoundParameter("hue", 0, 0, 360);
    public final CompoundParameter saturation = new CompoundParameter("saturation", 30, 0, 100);

    protected List<Particle> particles = new ArrayList<Particle>();
    protected ModelIndex modelIndex;

    private SimulationThread simThread;
    private float[][] brightnessLayers;
    private float[] brightnessBuffer;

    public ParticlePattern(LX lx) {
        this(lx, DEFAULT_PARTICLE_COUNT);
    }

    public ParticlePattern(LX lx, int numParticles) {
        super(lx);

        brightnessBuffer = new float[colors.length];
        brightnessLayers = new float[particles.size()][brightnessBuffer.length];

        addParameter(particleCount);
        addParameter(kernelSize);
        addParameter(flattenZ);

        addParameter(hue);
        addParameter(saturation);

        modelIndex = createModelIndex();

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

                brightnessLayers = new float[particles.size()][brightnessBuffer.length];
            }
        });

        flattenZ.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter param) {
                ParticlePattern.this.modelIndex = createModelIndex();
            }
        });

        particleCount.setValue(numParticles);

        simThread = new SimulationThread();
    }

    private ModelIndex createModelIndex() {
        return new OctreeModelIndex(lx.model, flattenZ.isOn());
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

    /** Only needed until we can do IntRange.range in Processing */
    private Stream intRangeStream(final int startInclusive, final int endExclusive) {
        List<Integer> range = new ArrayList<>(endExclusive - startInclusive);
        for (int i = 0; i < (endExclusive - startInclusive); ++i) {
            range.add(i + startInclusive);
        }
        return range.parallelStream();
    }

    @Override
    public synchronized void run(double deltaMs) {
        intRangeStream(0, particles.size()).forEach(new Consumer<Integer>() {
            public void accept(Integer i) {
                Arrays.fill(brightnessLayers[i], 0);
                renderParticle(particles.get(i), brightnessLayers[i]);
            }
        });

        // TODO: only copy part of buffer used by each particle
        intRangeStream(0, brightnessBuffer.length).forEach(new Consumer<Integer>() {
            public void accept(Integer i) {
                brightnessBuffer[i] = 0f;

                for (int j = 0; j < brightnessLayers.length; ++j) {
                    brightnessBuffer[i] += brightnessLayers[j][i];
                }
            }
        });

        super.run(deltaMs);
    }

    protected void renderParticle(Particle particle, float[] brightness) {
        LXPoint pp = particle.toPointInModel(lx.model);
        float withinDist = particle.size * kernelSize.getValuef();
        List<LXPoint> nearbyPoints = modelIndex.pointsWithin(pp, withinDist);

        final boolean flattening = flattenZ.isOn();
        for (LXPoint p : nearbyPoints) {
            brightness[p.index] = kernel(pp.x - p.x, pp.y - p.y, flattening ? 0 : pp.z - p.z, withinDist);
        }
    }

    @Override
    public void render(List<LXPoint> points, IntBuffer pointColors) {
        double h = hue.getValue();
        double s = saturation.getValue();
        for (int i = 0; i < points.size(); ++i) {
            LXPoint p = points.get(i);
            float b = Math.min(brightnessBuffer[p.index] * 100, 100);
            pointColors.put(i, LXColor.hsb(h, s, b));
        }
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
