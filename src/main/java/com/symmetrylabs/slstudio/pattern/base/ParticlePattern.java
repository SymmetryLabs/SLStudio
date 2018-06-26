package com.symmetrylabs.slstudio.pattern.base;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CopyOnWriteArrayList;

import com.symmetrylabs.slstudio.model.SLModel;
import org.apache.commons.math3.util.FastMath;
import processing.core.PVector;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXVector;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.EnumParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;

import com.symmetrylabs.util.BlobTracker;
import com.symmetrylabs.util.BlobFollower;
import com.symmetrylabs.util.Marker;
import com.symmetrylabs.util.MarkerSource;
import com.symmetrylabs.util.CubeMarker;
import com.symmetrylabs.util.OctahedronWithArrow;
import com.symmetrylabs.slstudio.model.LXPointNormal;

public abstract class ParticlePattern extends SLPattern<SLModel> implements MarkerSource {
    private static final double SQRT_2PI = FastMath.sqrt(2 * FastMath.PI);

    public static final int DEFAULT_PARTICLE_GROUP_COUNT = 16;

    public static enum KernelChoice {
        GAUSSIAN, LAPLACE, SPHERE //, FLAT
    }

    public static enum BlobTrackingMode {
        CLOSEST, AVERAGE
    }

    public CompoundParameter particleCount;
    public CompoundParameter kernelSize;
    public CompoundParameter edgeCutoff;
    public CompoundParameter peakCutoff;
    public EnumParameter<KernelChoice> kernelType;
    public BooleanParameter flattenZ;
    public BooleanParameter drawParticles;

    public CompoundParameter hue;
    public CompoundParameter saturation;

    public BooleanParameter enableBlobs;
    public CompoundParameter blobMaxDist;
    public CompoundParameter blobMaxAngle;
    public CompoundParameter blobPull;
    public EnumParameter<BlobTrackingMode> blobTrackingMode;

    protected List<Particle> particles = new CopyOnWriteArrayList<>();
    private ParticleGroup[] particleGroups;

    protected BlobFollower blobFollower;
    protected BlobDist closestBlobDist = null;
    protected double[] avgBlobPos = new double[3];
    protected double avgBlobDist = 0;
    protected int visibleBlobCount = 0;

    protected Particle spawnParticle(int index) {
        return new Particle(index);
    }

    protected void initParticle(Particle p) { }
    protected abstract void simulate(double deltaMs);

    public ParticlePattern(LX lx) {
        this(lx, DEFAULT_PARTICLE_GROUP_COUNT);
    }

    public ParticlePattern(LX lx, int particleGroupCount) {
        super(lx);

        blobFollower = new BlobFollower(BlobTracker.getInstance(lx));

        particleGroups = new ParticleGroup[particleGroupCount];

        for (int i = 0; i < particleGroups.length; ++i) {
            particleGroups[i] = new ParticleGroup(model.points.length);
        }

        particleCount.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter particleCount) {
                int numParticles = (int) particleCount.getValue();
                while (particles.size() > numParticles) {
                    Particle p = particles.get(particles.size() - 1);
                    particleGroups[p.index % particleGroups.length].particles.remove(p);
                    particles.remove(p);
                }

                for (int i = particles.size(); i < numParticles; ++i) {
                    Particle p = spawnParticle(i);
                    initParticle(p);
                    particleGroups[p.index % particleGroups.length].particles.add(p);
                    particles.add(p);
                }
            }
        });
    }

    @Override
    protected void createParameters() {
        super.createParameters();

        addParameter(particleCount = new CompoundParameter("count", 0, 0, 300));
        addParameter(kernelSize = new CompoundParameter("size", 15, 0, 100));
        addParameter(edgeCutoff = new CompoundParameter("edgeCut", 0.85, 0.25, 1));
        addParameter(peakCutoff = new CompoundParameter("peakCut", 1, 0, 1));
        addParameter(kernelType = new EnumParameter<KernelChoice>("kernel", KernelChoice.GAUSSIAN));
        addParameter(flattenZ = new BooleanParameter("flattenZ", false));
        addParameter(drawParticles = new BooleanParameter("drawParticles", false));

        addParameter(hue = new CompoundParameter("hue", 0, 0, 360));
        addParameter(saturation = new CompoundParameter("saturation", 30, 0, 100));

        addParameter(enableBlobs = new BooleanParameter("enableBlobs", true));
        addParameter(blobMaxDist = new CompoundParameter("bMaxDist", 500, 0, 1000));
        addParameter(blobMaxAngle = new CompoundParameter("bMaxAngle", 60, 0, 90));
        addParameter(blobPull = new CompoundParameter("bPull", 100, 0, 200));
        addParameter(blobTrackingMode = new EnumParameter<BlobTrackingMode>("bTrackingMode", BlobTrackingMode.AVERAGE));
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

        if (drawParticles.isOn()) {
            for (Particle p : particles) {
                markers.add(new OctahedronWithArrow(p.posToPVectorInModel(model), p.size, LXColor.GREEN, p.arrowToPVectorInModel(model), LXColor.BLUE));
            }
        }

        if (closestBlobDist == null)
            return markers;

        PVector blobPos = closestBlobDist.blob.pos;
        markers.add(new OctahedronWithArrow(blobPos, 24, LXColor.WHITE,
            new PVector(
                (float)(model.cx - blobPos.x),
                (float)(model.cy - blobPos.y),
                (float)(model.cz - blobPos.z)
            ), LXColor.RED
        ));

        markers.add(new CubeMarker(new PVector(
                (float)avgBlobPos[0],
                (float)avgBlobPos[1],
                (float)avgBlobPos[2]
            ), 50, LXColor.BLUE));

        return markers;
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

    protected double kernelSphereSqr(double dSqr, double s) {
        return dSqr > s * s ? 0 : 1;
    }

    protected double kernel(double x, double y, double z, double s) {
        double dSqr = x * x + y * y + z * z;
        switch (kernelType.getEnum()) {
            case GAUSSIAN:
                return kernelGaussianSqr(dSqr, s);
            case LAPLACE:
                return kernelLaplace(FastMath.sqrt(dSqr), s);
            case SPHERE:
                return kernelSphereSqr(dSqr, s);
            //case FLAT:
            //    return 1;
            default:
                return 0;
        }
    }

    protected int getPaletteColor(float val) {
        double h = hue.getValue();
        double s = saturation.getValue();
        return LXColor.hsb(h, Math.min(s + val * 50, 100), FastMath.min(val * 100, 100));
    }

    @Override
    public void render(double deltaMs, List<LXVector> points, int[] layer) {
        if (enableBlobs.getValueb()) {
            double sqrDistThresh = blobMaxDist.getValue() * blobMaxDist.getValue();
            double maxAngleRad = blobMaxAngle.getValue() * FastMath.PI / 180;
            List<BlobFollower.Follower> blobs = blobFollower.getFollowers();

            double avgBlobPosX = 0;
            double avgBlobPosY = 0;
            double avgBlobPosZ = 0;

            BlobFollower.Follower closestBlob = null;
            double closestSqrDist = Double.MAX_VALUE;
            for (BlobFollower.Follower blob : blobs) {
                double dx = blob.pos.x - model.cx;
                double dy = blob.pos.y - model.cy;
                double dz = blob.pos.z - model.cz;
                double sqrDist = dx * dx + dy * dy + dz * dz;
                double angleRad = FastMath.atan2(FastMath.abs(dx), FastMath.abs(dz));
                if (angleRad < maxAngleRad && sqrDist < sqrDistThresh && sqrDist < closestSqrDist) {
                    closestSqrDist = sqrDist;
                    closestBlob = blob;
                }

                avgBlobPosX += blob.pos.x;
                avgBlobPosY += blob.pos.y;
                avgBlobPosZ += blob.pos.z;
            }

            visibleBlobCount = blobs.size();

            if (visibleBlobCount > 0) {
                avgBlobPosX /= visibleBlobCount;
                avgBlobPosY /= visibleBlobCount;
                avgBlobPosZ /= visibleBlobCount;
            }

            avgBlobPos[0] = avgBlobPosX;
            avgBlobPos[1] = avgBlobPosY;
            avgBlobPos[2] = avgBlobPosZ;

            double dx = avgBlobPosX - model.cx;
            double dy = avgBlobPosY - model.cy;
            double dz = avgBlobPosZ - model.cz;
            avgBlobDist = FastMath.sqrt(dx * dx + dy * dy + dz * dz);

            if (closestBlob == null) {
                closestBlobDist = null;
            } else {
                closestBlobDist = new BlobDist(closestBlob, FastMath.sqrt(closestSqrDist));
            }

            blobFollower.advance((float)deltaMs * 0.001f);
        } else {
            closestBlobDist = null;
            visibleBlobCount = 0;
        }

        simulate(deltaMs);

        Arrays.asList(particleGroups).parallelStream().forEach(pg -> {
            Arrays.fill(pg.layer, 0);
            pg.particles.stream().forEach(this::renderParticle);
        });

        points.parallelStream().forEach(point -> {
            float s = 0;
            for (ParticleGroup pg : particleGroups) {
                s += pg.layer[point.index];
            }

            layer[point.index] = getPaletteColor(s);
        });
    }

    protected void renderParticle(Particle particle) {
        float[] particleLayer = particleGroups[particle.index % particleGroups.length].layer;

        LXVector pp = particle.toPointInModel(model);
        float withinDist = particle.size * kernelSize.getValuef();
        List<LXPoint> nearbyPoints = model.getModelIndex(flattenZ.isOn())
                .pointsWithin(pp, withinDist * edgeCutoff.getValuef());

        particle.rebound[0] = 0;
        particle.rebound[1] = 0;
        particle.rebound[2] = 0;
        particle.contact = 0;

        float peakCutoffValue = peakCutoff.getValuef();

        final boolean flattening = flattenZ.isOn();
        for (LXPoint p : nearbyPoints) {
            float b = (float)kernel(pp.x - p.x, pp.y - p.y, flattening ? 0 : pp.z - p.z, withinDist);
            b = FastMath.min(b, peakCutoffValue);

            if (p instanceof LXPointNormal) {
                PVector pointNormal = ((LXPointNormal)p).normal;
                particle.rebound[0] -= pointNormal.x * b;
                particle.rebound[1] -= pointNormal.y * b;
                particle.rebound[2] -= pointNormal.z * b;
            }

            particle.contact += b;

            particleLayer[p.index] += b;
        }

        if (!nearbyPoints.isEmpty()) {
            particle.rebound[0] /= nearbyPoints.size();
            particle.rebound[1] /= nearbyPoints.size();
            particle.rebound[2] /= nearbyPoints.size();
            particle.contact /= nearbyPoints.size();
        }

        //particle.size = (float)FastMath.min(0.5 + 1000 * FastMath.abs(particle.vel[0] * particle.vel[1] * particle.vel[2]), 10);
    }

    private static class ParticleGroup {
        public final List<Particle> particles = new CopyOnWriteArrayList<>();
        public final float[] layer;

        public ParticleGroup(int pointCount) {
            layer = new float[pointCount];
        }
    }

    protected static class Particle {
        public double[] pos = new double[3];
        public double[] vel = new double[3];
        public double[] arrow = new double[3];
        public double[] rebound = new double[3];
        public double contact = 0;
        public float size = 1;

        public final int index;

        private LXVector point = new LXVector(0, 0, 0);

        public Particle(int index) {
            this.index = index;
        }

        public synchronized LXVector toPointInModel(LXModel model) {
            return point.set(
                (float)(model.cx + pos[0] * model.xRange / 2f),
                (float)(model.cy + pos[1] * model.yRange / 2f),
                (float)(model.cz + pos[2] * model.zRange / 2f)
            );
        }

        public PVector posToPVectorInModel(LXModel model) {
            return new PVector(
                (float)(model.cx + pos[0] * model.xRange / 2f),
                (float)(model.cy + pos[1] * model.yRange / 2f),
                (float)(model.cz + pos[2] * model.zRange / 2f)
            );
        }

        public PVector arrowToPVectorInModel(LXModel model) {
            return new PVector(
                (float)(arrow[0] * model.xRange / 2f),
                (float)(arrow[1] * model.yRange / 2f),
                (float)(arrow[2] * model.zRange / 2f)
            );
        }
    }

    protected class BlobDist {
        public final BlobFollower.Follower blob;
        public final double dist;

        public BlobDist(BlobFollower.Follower blob, double dist) {
            this.blob = blob;
            this.dist = dist;
        }
    }
}
