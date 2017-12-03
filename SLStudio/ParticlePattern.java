package com.symmetrylabs.pattern;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.stream.Stream;
import java.util.function.Consumer;
import java.nio.IntBuffer;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
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

public abstract class ParticlePattern extends LXPattern {
    private static final int DEFAULT_PARTICLE_COUNT = 10;
    private final double SQRT_2PI = Math.sqrt(2 * Math.PI);

    public final BoundedParameter particleCount = new BoundedParameter("count", 0, 0, 200);
    public final CompoundParameter kernelSize = new CompoundParameter("size", 100, 0, 400);
    public final BooleanParameter flattenZ = new BooleanParameter("flattenZ", true);

    public final CompoundParameter hue = new CompoundParameter("hue", 0, 0, 360);
    public final CompoundParameter saturation = new CompoundParameter("saturation", 30, 0, 100);

    protected List<Particle> particles = new ArrayList<>();
    protected ModelIndex modelIndex;

    private Map<Particle, float[]> brightnessLayers = new HashMap<Particle, float[]>();
    private float[] brightnessBuffer;

    public ParticlePattern(LX lx) {
        this(lx, DEFAULT_PARTICLE_COUNT);
    }

    public ParticlePattern(LX lx, int numParticles) {
        super(lx);

        brightnessBuffer = new float[colors.length];

        addParameter(particleCount);
        addParameter(kernelSize);
        addParameter(flattenZ);

        addParameter(hue);
        addParameter(saturation);

        modelIndex = createModelIndex();

        particleCount.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter particleCount) {
                synchronized (brightnessLayers) {
                    int numParticles = (int)particleCount.getValue();
                    while (particles.size() > numParticles) {
                        Particle p = particles.remove(particles.size() - 1);
                        brightnessLayers.remove(p);
                    }

                    for (int i = particles.size(); i < numParticles; ++i) {
                        Particle p = new Particle();
                        brightnessLayers.put(p, new float[brightnessBuffer.length]);
                        initParticle(p);
                        particles.add(p);
                    }
                }
            }
        });

        flattenZ.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter param) {
                ParticlePattern.this.modelIndex = createModelIndex();
            }
        });

        particleCount.setValue(numParticles);
    }

    private ModelIndex createModelIndex() {
        return new OctreeModelIndex(lx.model, flattenZ.isOn());
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
    public void run(double deltaMs) {
        //System.out.println(deltaMs);

        simulate(deltaMs);

        final List<Particle> particleList = new ArrayList<>(particles);
        final Map<Particle, float[]> layersMap = new HashMap<>(brightnessLayers);
        final List<float[]> layersList = new ArrayList<>(particles.size());

        synchronized (brightnessLayers) {
            for (Particle particle : particleList) {
                if (brightnessLayers.containsKey(particle)) {
                    layersList.add(brightnessLayers.get(particle));
                }
            }
        }

        // TODO: make per-particle layer sparse
        particleList.parallelStream().forEach(new Consumer<Particle>() {
            public void accept(Particle particle) {
                if (layersMap.containsKey(particle)) {
                    renderParticle(particle, layersMap.get(particle));
                }
            }
        });

        Arrays.asList(model.points).parallelStream().forEach(new Consumer<LXPoint>() {
            public void accept(LXPoint point) {
                float b = 0;
                for (float[] layer : layersList) {
                    b += layer[point.index];
                }

                brightnessBuffer[point.index] = b;
            }
        });

        double h = hue.getValue();
        double s = saturation.getValue();
        for (LXPoint point : model.points) {
            float b = Math.min(brightnessBuffer[point.index] * 100, 100);
            colors[point.index] = LXColor.hsb(h, s, b);
        }
    }

    protected void renderParticle(Particle particle, float[] brightnessLayer) {
        Arrays.fill(brightnessLayer, 0);

        LXPoint pp = particle.toPointInModel(lx.model);
        float withinDist = particle.size * kernelSize.getValuef();
        List<LXPoint> nearbyPoints = modelIndex.pointsWithin(pp, withinDist);

        final boolean flattening = flattenZ.isOn();
        for (LXPoint p : nearbyPoints) {
            brightnessLayer[p.index] = kernel(pp.x - p.x, pp.y - p.y, flattening ? 0 : pp.z - p.z, withinDist);
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
}
