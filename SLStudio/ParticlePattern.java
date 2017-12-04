package com.symmetrylabs.pattern;

import java.util.List;
import java.util.ArrayList;
import java.util.Vector;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.stream.Stream;
import java.util.function.Consumer;

import org.apache.commons.math3.util.FastMath;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.model.LXPoint;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXFixture;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.color.LXColor;

import com.symmetrylabs.util.LayeredRenderer;
import com.symmetrylabs.util.ModelIndex;
import com.symmetrylabs.util.OctreeModelIndex;

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

        flattenZ.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter param) {
                ParticlePattern.this.modelIndex = createModelIndex();
            }
        });
    }

    private ModelIndex createModelIndex() {
        return new OctreeModelIndex(lx.model, flattenZ.isOn());
    }

    protected float kernel(float d, float s) {
        double stddev = s / 4f;
        double peak = 1.0f / (2.5f * stddev);
        return (float)(FastMath.exp(-(d * d) / (2 * stddev * stddev))
                            / (stddev * SQRT_2PI) / peak);
    }

    protected float kernel(float x, float y, float z, float s) {
        return kernel((float)FastMath.sqrt(x * x + y * y + z * z), s);
    }

    protected void initParticle(Particle p) { }
    protected abstract void simulate(double deltaMs);

    @Override
    public void run(double deltaMs) {
        simulate(deltaMs);
        renderer.run(deltaMs);
    }

    protected void renderParticle(Particle particle, int[] layer) {
        Arrays.fill(layer, 0);

        LXPoint pp = particle.toPointInModel();
        float withinDist = particle.size * kernelSize.getValuef();
        List<LXPoint> nearbyPoints = modelIndex.pointsWithin(pp, withinDist);

        double h = hue.getValue();
        double s = saturation.getValue();

        final boolean flattening = flattenZ.isOn();
        for (LXPoint p : nearbyPoints) {
            float b = kernel(pp.x - p.x, pp.y - p.y, flattening ? 0 : pp.z - p.z, withinDist);
            int c = LXColor.hsb(h, s, FastMath.min(b * 100, 100));
            layer[p.index] = LXColor.blend(layer[p.index], c, LXColor.Blend.ADD);
        }
    }

    protected class Particle {
        public float[] pos = new float[3];
        public float[] vel = new float[3];
        public float size = 1;

        private LXPoint point = new LXPoint(0, 0, 0);

        LXPoint toPointInModel() {
            float x = lx.model.xMin + pos[0] * lx.model.xRange;
            float y = lx.model.yMin + pos[1] * lx.model.yRange;
            float z = lx.model.zMin + pos[2] * lx.model.zRange;
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
                renderParticle(particle, layer);
            }
        }
    }
}
