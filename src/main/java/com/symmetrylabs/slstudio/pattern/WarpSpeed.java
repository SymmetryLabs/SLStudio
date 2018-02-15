package com.symmetrylabs.slstudio.pattern;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.model.LXPoint;
import heronarts.p3lx.P3LXGraphicsPattern;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.p3lx.P3LX;

import processing.core.PGraphics;
import static processing.core.PApplet.*;
import static processing.core.PConstants.PI;
import processing.core.PVector;

import com.symmetrylabs.slstudio.util.MathUtils;
import static com.symmetrylabs.util.MathConstants.*;
import com.symmetrylabs.slstudio.pattern.raven.P3CubeMapPattern;

public class WarpSpeed extends P3CubeMapPattern {

    private final Particle[] particles = new Particle[50];

    private final int diagonal = (int)(MathUtils.sqrt(model.xRange*model.xRange + model.yRange * model.yRange)/2.f);

    private float rotation = 0;

    public final CompoundParameter size = new CompoundParameter("size", 10, 0, 100);

    public WarpSpeed(LX lx) {
        super(
            (P3LX) lx,
            new PVector(lx.model.cx, lx.model.cy, lx.model.cz),
            new PVector(lx.model.xRange, lx.model.yRange, lx.model.zRange),
            200
        );

        addParameter(size);

        for (int i = 0; i < particles.length; i++) {
            particles[i] = new Particle();
            particles[i].o = MathUtils.random(1, MathUtils.random(1, model.xRange/particles[i].n));
        }
    }

    public void run(double deltaMs, PGraphics pg) {
        pg.background(0);
        pg.translate(model.xRange/2, model.yRange/2);
        rotation -= 0.002;
        pg.rotate(rotation);

        for (int i = 0; i < particles.length; i++) {
            particles[i].draw(pg);
            if (particles[i].drawDist() > diagonal) {
                particles[i] = new Particle();
            }
        }
    }

    private class Particle {
        float n;
        float r;
        float o;
        int l;
        Particle() {
            l = 1;
            n = MathUtils.random(1, model.xRange/2);
            r = MathUtils.random(0, TWO_PI);
            o = MathUtils.random(1, MathUtils.random(1, model.xRange/n));
        }

        void draw(PGraphics pg) {
            l++;
            pg.pushMatrix();
            pg.rotate(r);
            pg.translate(drawDist(), 0);
            pg.fill(255, MathUtils.min(l, 255));
            pg.ellipse(0, 0, model.xRange/o/8, model.yRange/o/8);
            pg.popMatrix();
            o -= 0.07;
        }
        float drawDist() {
            return atan(n/o)*model.xRange/PI/2.f;
        }
    }
}

