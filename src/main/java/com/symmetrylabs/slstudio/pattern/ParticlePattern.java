package com.symmetrylabs.slstudio.pattern;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;

import org.apache.commons.math3.util.FastMath;
import processing.core.PVector;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXVector;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.EnumParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;

import com.symmetrylabs.slstudio.util.BlobTracker;
import com.symmetrylabs.slstudio.util.Marker;
import com.symmetrylabs.slstudio.util.MarkerSource;
import com.symmetrylabs.slstudio.util.OctahedronWithArrow;
import com.symmetrylabs.slstudio.util.ModelIndex;
import com.symmetrylabs.slstudio.util.LinearModelIndex;
import com.symmetrylabs.slstudio.util.OctreeModelIndex;

public abstract class ParticlePattern extends SubmodelPattern implements MarkerSource {
    private final double SQRT_2PI = FastMath.sqrt(2 * FastMath.PI);

    public static enum KernelChoice {
        GAUSSIAN, LAPLACE, SPHERE, FLAT
    }

    public CompoundParameter particleCount;
    public CompoundParameter kernelSize;
    public EnumParameter<KernelChoice> kernelType;
    public BooleanParameter flattenZ;

    public CompoundParameter hue;
    public CompoundParameter saturation;

    public BooleanParameter enableBlobs;
    public CompoundParameter blobMaxDist;
    public CompoundParameter blobMaxAngle;
    public CompoundParameter blobAffinity;

    protected ModelIndex modelIndex;
    protected List<Particle> particles = new ArrayList<>();

    protected BlobTracker blobTracker = BlobTracker.getInstance(lx);
    protected BlobDist closestBlobDist = null;

    protected void initParticle(Particle p) { }
    protected abstract void simulate(double deltaMs);

    public ParticlePattern(LX lx, LXModel submodel) {
        super(lx);

        setModel(submodel);

        particleCount.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter particleCount) {
                synchronized (particles) {
                    int numParticles = (int) particleCount.getValue();
                    while (particles.size() > numParticles) {
                        particles.remove(particles.size() - 1);
                    }

                    for (int i = particles.size(); i < numParticles; ++i) {
                        Particle p = new Particle(i, colors.length);
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
    }

    public ParticlePattern(LX lx) {
        this(lx, lx.model);
    }

    @Override
    protected void createParameters() {
        super.createParameters();

        addParameter(particleCount = new CompoundParameter("count", 0, 0, 100));
        addParameter(kernelSize = new CompoundParameter("size", 15, 0, 100));
        addParameter(kernelType = new EnumParameter<KernelChoice>("kernel", KernelChoice.GAUSSIAN));
        addParameter(flattenZ = new BooleanParameter("flattenZ", false));

        addParameter(hue = new CompoundParameter("hue", 0, 0, 360));
        addParameter(saturation = new CompoundParameter("saturation", 30, 0, 100));

        addParameter(enableBlobs = new BooleanParameter("enableBlobs", true));
        addParameter(blobMaxDist = new CompoundParameter("bMaxDist", 500, 0, 1000));
        addParameter(blobMaxAngle = new CompoundParameter("bMaxAngle", 60, 0, 90));
        addParameter(blobAffinity = new CompoundParameter("bPull", 100, 0, 200));
    }

    @Override
    protected void onModelChanged(LXModel model) {
        modelIndex = createModelIndex();
    }

    @Override
    public void onActive() {
        super.onActive();
        lx.ui.addMarkerSource(this);
    }

    @Override
    public void onInactive() {
        super.onInactive();
        lx.ui.removeMarkerSource(this);
    }

    @Override
    public List<Marker> getMarkers() {
        List<Marker> markers = new ArrayList<Marker>();

        if (closestBlobDist == null)
            return markers;

        PVector blobPos = closestBlobDist.blob.pos;
        markers.add(new OctahedronWithArrow(blobPos, 20, LXColor.WHITE,
            new PVector(
                (float)(model.cx - blobPos.x),
                (float)(model.cy - blobPos.y),
                (float)(model.cz - blobPos.z)
            ), LXColor.RED
        ));

        return markers;
    }

    private ModelIndex createModelIndex() {
        return new OctreeModelIndex(model, flattenZ.isOn());
        //return new LinearModelIndex(model, flattenZ.isOn());
    }

    private double kernelPolySqr(double dSqr, double s) {
        if (dSqr > s)
            return 0;

        double c = 1.19 / s;
        double a2 = -1.414213;
        double a4 = 0.5;
        return 1 + a2 * c * dSqr + 0.5 * c * dSqr * dSqr;
    }

    private double kernelGaussianSqr(double dSqr, double s) {
        return FastMath.exp(-dSqr * 8 / (s * s)) * 2.5 / SQRT_2PI;
    }

    private double kernelLaplace(double d, double s) {
        return FastMath.exp(-FastMath.abs(d * 4 / s));
    }

    protected double kernelSphere(double dSqr, double s) {
        return dSqr > s ? 0 : 1;
    }

    protected double kernel(double x, double y, double z, double s) {
        double dSqr = x * x + y * y + z * z;
        switch (kernelType.getEnum()) {
            case GAUSSIAN:
                return kernelGaussianSqr(dSqr, s);
            case LAPLACE:
                return kernelLaplace(FastMath.sqrt(dSqr), s);
            case SPHERE:
                return kernelSphere(dSqr, s);
            case FLAT:
                return 1;
            default:
                return 0;
        }
    }

    protected int getPaletteColor(float val) {
        double h = hue.getValue();
        double s = saturation.getValue();
        return LXColor.hsb(h, s, FastMath.min(val * 100, 100));
    }

    @Override
    public void render(double deltaMs, List<LXPoint> points, int[] layer) {
        if (enableBlobs.getValueb()) {
            double sqrDistThresh = blobMaxDist.getValue() * blobMaxDist.getValue();
            double maxAngleRad = blobMaxAngle.getValue() * FastMath.PI / 180;
            List<BlobTracker.Blob> blobs = blobTracker.getBlobs();

            BlobTracker.Blob closestBlob = null;
            double closestSqrDist = Double.MAX_VALUE;
            for (BlobTracker.Blob b : blobs) {
                double dx = b.pos.x - model.cx;
                double dy = b.pos.y - model.cy;
                double dz = b.pos.z - model.cz;
                double sqrDist = dx * dx + dy * dy + dz * dz;
                double angleRad = FastMath.atan2(FastMath.abs(dx), FastMath.abs(dz));
                if (angleRad < maxAngleRad && sqrDist < sqrDistThresh && sqrDist < closestSqrDist) {
                    closestSqrDist = sqrDist;
                    closestBlob = b;
                }
            }

            if (closestBlob == null) {
                closestBlobDist = null;
            } else {
                closestBlobDist = new BlobDist(closestBlob, FastMath.sqrt(closestSqrDist));
            }
        } else {
            closestBlobDist = null;
        }

        simulate(deltaMs);

        List<Particle> particleList;
        synchronized (particles) {
            particleList = new ArrayList<Particle>(particles);
        }

        particleList.parallelStream().forEach(this::renderParticle);

        points.parallelStream().forEach(point -> {
            float s = 0;
            for (Particle particle : particleList) {
                s += particle.layer[point.index];
            }

            layer[point.index] = getPaletteColor(s);
        });
    }

    protected void renderParticle(Particle particle) {
        Arrays.fill(particle.layer, 0f);

        LXVector pp = particle.toPointInModel(model);
        float withinDist = particle.size * kernelSize.getValuef();
        List<LXPoint> nearbyPoints = modelIndex.pointsWithin(pp, withinDist);

        final boolean flattening = flattenZ.isOn();
        for (LXPoint p : nearbyPoints) {
            float b = (float)kernel(pp.x - p.x, pp.y - p.y, flattening ? 0 : pp.z - p.z, withinDist);
            particle.layer[p.index] = b;
        }
    }

    protected static class Particle {
        public double[] pos = new double[3];
        public double[] vel = new double[3];
        public float size = 1;

        public final float[] layer;
        public final int index;

        private LXVector point = new LXVector(0, 0, 0);

        public Particle(int index, int pointCount) {
            this.index = index;

            layer = new float[pointCount];
        }

        public synchronized LXVector toPointInModel(LXModel model) {
            return point.set(
                (float)(model.cx + pos[0] * model.xRange / 2f),
                (float)(model.cy + pos[1] * model.yRange / 2f),
                (float)(model.cz + pos[2] * model.zRange / 2f)
            );
        }
    }

    protected class BlobDist {
        public final BlobTracker.Blob blob;
        public final double dist;

        public BlobDist(BlobTracker.Blob blob, double dist) {
            this.blob = blob;
            this.dist = dist;
        }
    }
}
