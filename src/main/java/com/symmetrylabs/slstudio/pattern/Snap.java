package com.symmetrylabs.slstudio.pattern;

import java.util.List;
import java.util.ArrayList;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.transform.LXVector;

import com.symmetrylabs.util.MathUtils;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;

public class Snap extends SLPattern {
    List<LXVector> particles = new ArrayList<>();
    List<LXVector> destinations = new ArrayList<>();
    List<Float> spreads = new ArrayList<>();

    CompoundParameter speed = new CompoundParameter("SPEED", 0.01);
    private final SinLFO sinLfo = new SinLFO(5, 50, 2000);

    public Snap(LX lx) {
        super(lx);

        addParameter(speed);
        startModulator(sinLfo);

        for (int i = 0; i < 180; i++) {
            particles.add(new LXVector(randomPoint()));
            destinations.add(new LXVector(randomPoint()));
            spreads.add(nrand(20));
        }
    }

    LXPoint randomPoint() {
        int i = (int)MathUtils.random(model.points.length);
        return model.points[i];
    }

    float nrand(float max) {
        float v = MathUtils.random(0, 2) - 1;
        return v * max;
    }

    public void run(double deltaMs) {

        for (LXPoint point : model.points) {
            float maxB = 0;
            int pi = 0;
            for (LXVector particle: particles) {
                float dist = particle.dist(new LXVector(point));
                float bright = 0;
                // float r = radius.getValuef() * 100;
                float r = sinLfo.getValuef();
                if (dist < r) {
                    bright = MathUtils.map(dist, 0, r, 100, 0);
                }
                if (bright > maxB) {
                    maxB = bright;
                    break;
                }
                pi++;
            }
            float baseHue = palette.getHuef();
            float spreadHue = baseHue;
            if (pi < 180) {
                spreadHue = (baseHue + spreads.get(pi)) % 100;
            }
            if (spreadHue < 0) {
                spreadHue = 100 - spreadHue;
            }
            colors[point.index] = lx.hsb(baseHue, 100, maxB);
        }

        for (int i = 0; i < particles.size(); i++) {
            LXVector particle = particles.get(i).copy();
            LXVector dest = destinations.get(i).copy();
            LXVector diff = dest.add(particle.mult(-1)).mult(speed.getValuef());
            particles.get(i).add(diff);
            // LXVector diff = speed * (dest - particle);

            if (particles.get(i).dist(destinations.get(i)) < 1) {
                destinations.set(i, new LXVector(randomPoint()));
            }
        }
    }
}
