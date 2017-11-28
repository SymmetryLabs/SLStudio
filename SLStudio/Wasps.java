package com.symmetrylabs.pattern;

import heronarts.lx.LX;
import heronarts.lx.parameter.CompoundParameter;

public class Wasps extends ParticlePattern {
    private final double SQRT_2PI = Math.sqrt(2 * Math.PI);

    public final CompoundParameter accel = new CompoundParameter("accel", 0.15f, 0, 1);
    public final CompoundParameter dampen = new CompoundParameter("dampen", 0.75f, 0, 1);
    public final CompoundParameter gravityX = new CompoundParameter("gravityX", 0.5f, 0, 1);
    public final CompoundParameter gravityY = new CompoundParameter("gravityY", 0.5f, 0, 1);
    public final CompoundParameter gravityZ = new CompoundParameter("gravityZ", 0.5f, 0, 1);
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
        addParameter(gravityX);
        addParameter(gravityY);
        addParameter(gravityZ);
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

        float accelValue = 0.005f * accel.getValuef() * timeStep;
        float dampenValue = 0.05f * dampen.getValuef();
        float gravityXValue = 0.0005f * gravityX.getValuef();
        float gravityYValue = 0.0005f * gravityY.getValuef();
        float gravityZValue = 0.0005f * gravityZ.getValuef();

        for (int i = 0; i < particles.size(); ++i) {
            Particle p = particles.get(i);

            float fx = gravityXValue * (p.pos[0] - 2 * focusX.getValuef() + 1);
            float fy = gravityYValue * (p.pos[1] - 2 * focusY.getValuef() + 1);
            float fz = gravityZValue * (p.pos[2] - 2 * focusZ.getValuef() + 1);

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
