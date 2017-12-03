package com.symmetrylabs.pattern;

import heronarts.lx.LX;
import heronarts.lx.parameter.CompoundParameter;

import com.symmetrylabs.util.BlobTracker;

public class Wasps extends ParticlePattern {
    private final double SQRT_2PI = Math.sqrt(2 * Math.PI);

    public final CompoundParameter accel = new CompoundParameter("accel", 0.15f, 0, 1);
    public final CompoundParameter dampen = new CompoundParameter("dampen", 0.75f, 0, 1);
    public final CompoundParameter focusX = new CompoundParameter("focusX", 0.5f, 0, 1);
    public final CompoundParameter focusY = new CompoundParameter("focusY", 0.5f, 0, 1);
    public final CompoundParameter focusZ = new CompoundParameter("focusZ", 0.5f, 0, 1);
    public final CompoundParameter pullX = new CompoundParameter("pullX", 0.5f, 0, 1);
    public final CompoundParameter pullY = new CompoundParameter("pullY", 0.5f, 0, 1);
    public final CompoundParameter pullZ = new CompoundParameter("pullZ", 0.5f, 0, 1);
    public final CompoundParameter twistX = new CompoundParameter("twistX", 0.5f, 0, 1);
    public final CompoundParameter twistY = new CompoundParameter("twistY", 0.5f, 0, 1);
    public final CompoundParameter twistZ = new CompoundParameter("twistZ", 0.5f, 0, 1);

    //private BlobTracker blobTracker;

    public Wasps(LX lx) {
        this(lx, 10);

        //blobTracker = BlobTracker.getInstance(lx);
    }

    public Wasps(LX lx, int countValue) {
        super(lx, countValue);

        addParameter(accel);
        addParameter(dampen);
        addParameter(focusX);
        addParameter(focusY);
        addParameter(focusZ);
        addParameter(pullX);
        addParameter(pullY);
        addParameter(pullZ);
        addParameter(twistX);
        addParameter(twistY);
        addParameter(twistZ);
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
        float timeStep = timeBoost * (float)deltaMs / 1000f;

        float accelValue = 0.01f * accel.getValuef() * timeStep;
        float dampenValue = 0.05f * dampen.getValuef();

        float pullXValue = 0.0005f * pullX.getValuef();
        float pullYValue = 0.0005f * pullY.getValuef();
        float pullZValue = 0.0005f * pullZ.getValuef();

        float twistXValue = 0.0001f * twistX.getValuef();
        float twistYValue = 0.0001f * twistY.getValuef();
        float twistZValue = 0.0001f * twistZ.getValuef();

        for (int i = 0; i < particles.size(); ++i) {
            Particle p = particles.get(i);

            p.vel[0] -= dampenValue * p.vel[0];
            p.vel[1] -= dampenValue * p.vel[1];
            p.vel[2] -= dampenValue * p.vel[2];

            p.vel[0] += accelValue * (float)(Math.random() - .5);
            p.vel[1] += accelValue * (float)(Math.random() - .5);
            p.vel[2] += accelValue * (float)(Math.random() - .5);

            float pullVecX = (2 * focusX.getValuef() - 1) - p.pos[0];
            float pullVecY = (2 * focusY.getValuef() - 1) - p.pos[1];
            float pullVecZ = (2 * focusZ.getValuef() - 1) - p.pos[2];

            p.vel[0] += pullXValue * pullVecX;
            p.vel[1] += pullYValue * pullVecY;
            p.vel[2] += pullZValue * pullVecZ;

            // NOTE: assuming left-handed Z-axis

            float twistXVecX = 0;
            float twistXVecY = pullVecZ;
            float twistXVecZ = -pullVecY;

            float twistYVecX = -pullVecZ;
            float twistYVecY = 0;
            float twistYVecZ = pullVecX;

            float twistZVecX = -pullVecY;
            float twistZVecY = pullVecX;
            float twistZVecZ = 0;

            p.vel[0] += twistXValue * twistXVecX;
            p.vel[1] += twistXValue * twistXVecY;
            p.vel[2] += twistXValue * twistXVecZ;

            p.vel[0] += twistYValue * twistYVecX;
            p.vel[1] += twistYValue * twistYVecY;
            p.vel[2] += twistYValue * twistYVecZ;

            p.vel[0] += twistZValue * twistZVecX;
            p.vel[1] += twistZValue * twistZVecY;
            p.vel[2] += twistZValue * twistZVecZ;

            /*
            if (i > i / 2 && i / 2 < particles.size()) {
                Particle leader = particles.get(i / 2);
                p.vel[0] -= pullValue * (p.pos[0] - leader.pos[0]);
                p.vel[1] -= pullValue * (p.pos[1] - leader.pos[1]);
                p.vel[2] -= pullValue * (p.pos[2] - leader.pos[2]);
            }

            if (i > i / 2 + 1 && i / 2 + 1 < particles.size()) {
                Particle enemy = particles.get(i / 2 + 1);
                p.vel[0] += pullValue * (p.pos[0] - enemy.pos[0]);
                p.vel[1] += pullValue * (p.pos[1] - enemy.pos[1]);
                p.vel[2] += pullValue * (p.pos[2] - enemy.pos[2]);
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
