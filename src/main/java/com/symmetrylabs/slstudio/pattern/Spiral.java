package com.symmetrylabs.slstudio.pattern.raven;

import processing.core.PGraphics;
import processing.core.PVector;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.LXUtils;
import heronarts.lx.transform.LXProjection;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.transform.LXVector;

import com.symmetrylabs.slstudio.util.MathUtils;
import com.symmetrylabs.slstudio.pattern.SLPattern;

import java.lang.Math;

public class Spiral extends SLPattern {

    CompoundParameter speed = new CompoundParameter("spd", 0.2, -1, 1);

    CompoundParameter xTrans = new CompoundParameter("xTrn", -0.18, -1, 1);
    CompoundParameter yTrans = new CompoundParameter("yTrn", -0.32, -1, 1);

    CompoundParameter scale = new CompoundParameter("size", 0.55, 0.01, 5);
    CompoundParameter thick = new CompoundParameter("thick", 3, 0.1, 20);
    CompoundParameter thick1 = new CompoundParameter("thik1", 0.5);
    CompoundParameter steps = new CompoundParameter("steps", 2, 1, 10);

    float rotated = 0;

    private final LXProjection spiral;
    public Spiral(LX lx) {
        super(lx);
        addParameter(speed);
        addParameter(xTrans);
        addParameter(yTrans);
        addParameter(scale);
        addParameter(thick);
        addParameter(thick1);
        addParameter(steps);
        this.spiral = new LXProjection(model);
    }

    public void run(double deltaMs) {
        setColors(0);

        rotated += (100 * speed.getValuef()) * (float)Math.PI / 180.f;
        spiral.reset().scale(scale.getValuef(), scale.getValuef(), 0).translate(model.xRange * xTrans.getValuef(), model.yRange * yTrans.getValuef(), 0).rotateZ(rotated);
        

        int drawsteps = (int)steps.getValuef();
        for (float t = 0; t < drawsteps * ((float)Math.PI*2); t += ((float)Math.PI)/35.f) {
            float x = 8 * t * (float)Math.cos(t);
            float y = 8 * t * (float)Math.sin(t);

            float thickV = thick.getValuef();

            
            for (LXVector p : spiral) {
                if (LXUtils.distance(p.x, p.y, x, y) < thickV) {
                    colors[p.index] = lx.hsb(0, 100, 100);
                }
            }
        }


    }
}