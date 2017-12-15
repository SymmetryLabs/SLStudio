package com.symmetrylabs.slstudio.pattern;

import com.symmetrylabs.slstudio.util.LayeredRenderer;
import com.symmetrylabs.slstudio.util.ModelIndex;
import com.symmetrylabs.slstudio.util.OctreeModelIndex;
import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import org.apache.commons.math3.util.FastMath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public abstract class ParticlePattern extends LXPattern {
    private final double SQRT_2PI = Math.sqrt(2 * Math.PI);

    public final BoundedParameter particleCount = new BoundedParameter("count", 0, 0, 1000);
    public final CompoundParameter kernelSize = new CompoundParameter("size", 100, 0, 400);
    public final BooleanParameter flattenZ = new BooleanParameter("flattenZ", true);

    public final CompoundParameter hue = new CompoundParameter("hue", 0, 0, 360);
    public final CompoundParameter saturation = new CompoundParameter("saturation", 30, 0, 100);

    protected List<Particle> particles = new Vector<>();
    protected ModelIndex modelIndex;

    private ParticleRenderer renderer;

    public ParticlePattern(LX lx) {
        super(lx);

        renderer = new ParticleRenderer();

        addParameter(particleCount);
        addParameter(kernelSize);
        addParameter(flattenZ);

        addParameter(hue);
        addParameter(saturation);

        modelIndex = createModelIndex();

        particleCount.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter particleCount) {
                int numParticles = (int) particleCount.getValue();
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

        flattenZ.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter param) {
                ParticlePattern.this.modelIndex = createModelIndex();
            }
        });
    }

    private ModelIndex createModelIndex() {
        return new OctreeModelIndex(lx.model, flattenZ.isOn());
    }

    protected float kernel(double d, double s) {
        double stddev = s / 4f;
        double peak = 1.0f / (2.5f * stddev);
        return (float) (FastMath.exp(-(d * d) / (2 * stddev * stddev))
            / (stddev * SQRT_2PI) / peak);
    }

    protected float kernel(double x, double y, double z, double s) {
        return kernel(FastMath.sqrt(x * x + y * y + z * z), s);
    }

    protected void initParticle(Particle p) {
    }

    protected abstract void simulate(double deltaMs);

    @Override
    public void run(double deltaMs) {
        simulate(deltaMs);
        renderer.run(deltaMs);

        /*
        particles.parallelStream().forEach(new Consumer<Particle>() {
            public void accept(Particle particle) {
                renderParticle(particle);
            }
        });

        for (int j = 0; j < colors.length; ++j) {
            float s = 0;
            for (Particle particle : particles) {
                s += particle.layer[j];
            }

            colors[j] = getPaletteColor(s);
        }
        */
    }

    protected void renderParticle(Particle particle) {
        Arrays.fill(particle.layer, 0f);

        LXPoint pp = particle.toPointInModel();
        float withinDist = particle.size * kernelSize.getValuef();
        List<LXPoint> nearbyPoints = modelIndex.pointsWithin(pp, withinDist);

        final boolean flattening = flattenZ.isOn();
        for (LXPoint p : nearbyPoints) {
            float b = kernel(pp.x - p.x, pp.y - p.y, flattening ? 0 : pp.z - p.z, withinDist);
            particle.layer[p.index] = b;
        }
    }

    protected int getPaletteColor(float val) {
        double h = hue.getValue();
        double s = saturation.getValue();
        return LXColor.hsb(h, s, FastMath.min(val * 100, 100));
    }

    protected class Particle {
        public double[] pos = new double[3];
        public double[] vel = new double[3];
        public float size = 1;
        public float[] layer;

        private LXPoint point = new LXPoint(0, 0, 0);

        public Particle() {
            layer = new float[colors.length];
        }

        LXPoint toPointInModel() {
            float x = (float) (lx.model.cx + pos[0] * lx.model.xRange / 2f);
            float y = (float) (lx.model.cy + pos[1] * lx.model.yRange / 2f);
            float z = (float) (lx.model.cz + pos[2] * lx.model.zRange / 2f);
            point.update(x, y, z);
            return point;
        }
    }

    private class ParticleRenderer extends LayeredRenderer {
        public ParticleRenderer() {
            super(lx.model, colors);
        }

        @Override
        protected void render(double deltaMs, List<LXPoint> points, int[] layer, int index, int numThreads) {
            List<Particle> particleList = new ArrayList<>(particles);

            int startInclusive = particleList.size() * index / numThreads;
            int endExclusive = particleList.size() * (index + 1) / numThreads;

            for (int i = startInclusive; i < endExclusive; ++i) {
                Particle particle = particleList.get(i);
                renderParticle(particle);
            }

            for (int j = 0; j < colors.length; ++j) {
                float s = 0;
                for (int i = startInclusive; i < endExclusive; ++i) {
                    Particle particle = particleList.get(i);
                    s += particle.layer[j];
                }

                layer[j] = getPaletteColor(s);
            }
        }
    }
}
