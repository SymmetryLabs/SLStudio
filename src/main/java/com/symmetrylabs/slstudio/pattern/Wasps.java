package com.symmetrylabs.slstudio.pattern;

import heronarts.lx.LX;
import heronarts.lx.parameter.CompoundParameter;

public class Wasps extends ParticlePattern {
    private final double SQRT_2PI = Math.sqrt(2 * Math.PI);

    public final CompoundParameter speed = new CompoundParameter("speed", 1, 0, 5);
    public final CompoundParameter accel = new CompoundParameter("accel", 0.15f, 0, 1);
    public final CompoundParameter dampen = new CompoundParameter("dampen", 0.75f, 0, 1);
    public final CompoundParameter focusX = new CompoundParameter("focusX", 0f, -1, 1);
    public final CompoundParameter focusY = new CompoundParameter("focusY", 0f, -1, 1);
    public final CompoundParameter focusZ = new CompoundParameter("focusZ", 0f, -1, 1);
    public final CompoundParameter pullX = new CompoundParameter("pullX", 0.5f, 0, 1);
    public final CompoundParameter pullY = new CompoundParameter("pullY", 0.5f, 0, 1);
    public final CompoundParameter pullZ = new CompoundParameter("pullZ", 0.5f, 0, 1);
    public final CompoundParameter twistX = new CompoundParameter("twistX", 0.5f, 0, 1);
    public final CompoundParameter twistY = new CompoundParameter("twistY", 0.5f, 0, 1);
    public final CompoundParameter twistZ = new CompoundParameter("twistZ", 0.5f, 0, 1);

    public Wasps(LX lx) {
        super(lx);

        addParameter(speed);
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
        p.pos[0] = (float) (2 * Math.random() - 1);
        p.pos[1] = (float) (2 * Math.random() - 1);
        p.pos[2] = (float) (2 * Math.random() - 1);
        //System.out.println("[" + p.pos[0] + ", " + p.pos[1] + ", " + p.pos[2] + "]");
    }

    @Override
    protected void simulate(double deltaMs) {
        double timeBoost = 30;
        double timeStep = timeBoost * deltaMs / 1000f;

        double speedValue = speed.getValuef();
        double accelValue = 0.01 * accel.getValue() * timeStep;
        double dampenValue = 0.05 * dampen.getValue();

        double pullXValue = 0.0005 * pullX.getValue();
        double pullYValue = 0.0005 * pullY.getValue();
        double pullZValue = 0.0005 * pullZ.getValue();

        double twistXValue = 0.0001 * twistX.getValue();
        double twistYValue = 0.0001 * twistY.getValue();
        double twistZValue = 0.0001 * twistZ.getValue();

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

            double twistXVecX = 0;
            double twistXVecY = pullVecZ;
            double twistXVecZ = -pullVecY;

            double twistYVecX = -pullVecZ;
            double twistYVecY = 0;
            double twistYVecZ = pullVecX;

            double twistZVecX = -pullVecY;
            double twistZVecY = pullVecX;
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

            p.pos[0] += p.vel[0] * speedValue * timeStep;
            p.pos[1] += p.vel[1] * speedValue * timeStep;
            p.pos[2] += p.vel[2] * speedValue * timeStep;

            //p.size = (float)Math.min(1 + 50000 * Math.abs(p.vel[0] * p.vel[1] * p.vel[2]), 10);
            //p.size = (float)Math.min(1 + 1000 * Math.abs(p.vel[0] * p.vel[2]), 10);

            if (p.pos[0] < -1) p.pos[0] = -1;
            if (p.pos[0] > 1) p.pos[0] = 1;
            if (p.pos[1] < -1) p.pos[1] = -1;
            if (p.pos[1] > 1) p.pos[1] = 1;
            if (p.pos[2] < -1) p.pos[2] = -1;
            if (p.pos[2] > 1) p.pos[2] = 1;
        }
    }
}
