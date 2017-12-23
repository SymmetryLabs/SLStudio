package com.symmetrylabs.slstudio.pattern;

import java.util.List;
import java.util.ArrayList;

import org.apache.commons.math3.util.FastMath;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;

public class Wasps extends ParticlePattern {
    private final double SQRT_2PI = FastMath.sqrt(2 * FastMath.PI);

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

    public Wasps(LX lx) {
        super(lx);
    }

    @Override
    protected void createParameters() {
        super.createParameters();

        addParameter(speed = new CompoundParameter("speed", 0.2, 0, 2));
        addParameter(accel = new CompoundParameter("accel", 1.5, 0, 2));
        addParameter(dampen = new CompoundParameter("dampen", 0.75, 0, 1));
        addParameter(focusX = new CompoundParameter("focusX", 0, -1, 1));
        addParameter(focusY = new CompoundParameter("focusY", 0, -1, 1));
        addParameter(focusZ = new CompoundParameter("focusZ", 0, -1, 1));
        addParameter(pullX = new CompoundParameter("pullX", 0.5, 0, 1));
        addParameter(pullY = new CompoundParameter("pullY", 0.5, 0, 1));
        addParameter(pullZ = new CompoundParameter("pullZ", 0.5, 0, 1));
        addParameter(twistX = new CompoundParameter("twistX", 0, -1, 1));
        addParameter(twistY = new CompoundParameter("twistY", 0, -1, 1));
        addParameter(twistZ = new CompoundParameter("twistZ", 0, -1, 1));
    }

    @Override
    protected void initParticle(Particle p) {
        p.pos[0] = (float)(2 * FastMath.random() - 1);
        p.pos[1] = (float)(2 * FastMath.random() - 1);
        p.pos[2] = (float)(2 * FastMath.random() - 1);
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

        double twistXValue = 0.005 * twistX.getValue();
        double twistYValue = 0.005 * twistY.getValue();
        double twistZValue = 0.005 * twistZ.getValue();

        double focusPosX = focusX.getValue();
        double focusPosY = focusY.getValue();
        double focusPosZ = focusZ.getValue();

        double blobPosX = 0;
        double blobPosY = 0;
        double blobPosZ = 0;
        double blobScale = 0;

        if (enableBlobs.getValueb()) {
            if (closestBlobDist != null) {
                blobPosX = (closestBlobDist.blob.pos.x - model.cx) * 2f / model.xRange;
                blobPosY = (closestBlobDist.blob.pos.y - model.cy) * 2f / model.yRange;
                blobPosZ = (closestBlobDist.blob.pos.z - model.cz) * 2f / model.zRange;
                blobScale = 0.01 * blobAffinity.getValue() / (closestBlobDist.dist + 1);
            }
        }

        List<Particle> particleList;
        synchronized (particles) {
            particleList = new ArrayList<Particle>(particles);
        }
        for (int i = 0; i < particleList.size(); ++i) {
            Particle p = particleList.get(i);

            p.vel[0] -= dampenValue * p.vel[0];
            p.vel[1] -= dampenValue * p.vel[1];
            p.vel[2] -= dampenValue * p.vel[2];

            p.vel[0] += accelValue * (FastMath.random() - .5);
            p.vel[1] += accelValue * (FastMath.random() - .5);
            p.vel[2] += accelValue * (FastMath.random() - .5);

            double pullVecX = focusPosX - p.pos[0];
            double pullVecY = focusPosY - p.pos[1];
            double pullVecZ = focusPosZ - p.pos[2];

            p.vel[0] += pullXValue * pullVecX;
            p.vel[1] += pullYValue * pullVecY;
            p.vel[2] += pullZValue * pullVecZ;

            double blobVecX = blobPosX - p.pos[0];
            double blobVecY = blobPosY - p.pos[1];
            double blobVecZ = blobPosZ - p.pos[2];

            p.vel[0] += blobScale * blobVecX;
            p.vel[1] += blobScale * blobVecY;
            p.vel[2] += blobScale * blobVecZ;

            // NOTE: assuming left-handed Z-axis
            double pullNorm = FastMath.sqrt(pullVecX * pullVecX + pullVecY * pullVecY + pullVecZ * pullVecZ);

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
