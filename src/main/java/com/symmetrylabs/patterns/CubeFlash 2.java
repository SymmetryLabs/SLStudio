package com.symmetrylabs.patterns;

import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;

import heronarts.lx.LX;
import heronarts.lx.parameter.*;
import heronarts.lx.modulator.*;
import heronarts.lx.model.LXPoint;

import com.symmetrylabs.CubesModel;
import com.symmetrylabs.util.MathUtils;

public class CubeFlash extends CubesPattern {
    private CompoundParameter rateParameter = new CompoundParameter("RATE", 0.125);
    private CompoundParameter attackParameter = new CompoundParameter("ATTK", 0.5);
    private CompoundParameter decayParameter = new CompoundParameter("DECAY", 0.5);
    private CompoundParameter hueVarianceParameter = new CompoundParameter("H.V.", 0.25);
    private CompoundParameter saturationParameter = new CompoundParameter("SAT", 0.5);

    class Flash {
        CubesModel.Cube c;
        float value;
        float hue;
        boolean hasPeaked;

        Flash() {
            c = model.cubes.get((int)MathUtils.random(model.cubes.size()));
            hue = palette.getHuef() + (MathUtils.random(1) * 120 * hueVarianceParameter.getValuef());
            boolean infiniteAttack = (attackParameter.getValuef() > 0.999);
            hasPeaked = infiniteAttack;
            value = (infiniteAttack ? 1 : 0);
        }

        // returns TRUE if this should die
        boolean age(double ms) {
            if (!hasPeaked) {
                value = value + (float) (ms / 1000.0f * ((attackParameter.getValuef() + 0.01) * 5));
                if (value >= 1.0) {
                    value = 1f;
                    hasPeaked = true;
                }
                return false;
            } else {
                value = value - (float) (ms / 1000.0f * ((decayParameter.getValuef() + 0.01) * 10));
                return value <= 0;
            }
        }
    }

    private float leftoverMs = 0;
    private List<Flash> flashes;

    public CubeFlash(LX lx) {
        super(lx);
        addParameter(rateParameter);
        addParameter(attackParameter);
        addParameter(decayParameter);
        addParameter(hueVarianceParameter);
        addParameter(saturationParameter);
        flashes = new LinkedList<Flash>();
    }

    public void run(double deltaMs) {
        leftoverMs += deltaMs;
        float msPerFlash = 1000 / ((rateParameter.getValuef() + .01f) * 100);
        while (leftoverMs > msPerFlash) {
            leftoverMs -= msPerFlash;
            flashes.add(new Flash());
        }

        for (LXPoint p : model.points) {
            colors[p.index] = 0;
        }

        for (Flash flash : flashes) {
            int c = lx.hsb(flash.hue, saturationParameter.getValuef() * 100, (flash.value) * 100);
            for (LXPoint p : flash.c.points) {
                colors[p.index] = c;
            }
        }

        Iterator<Flash> i = flashes.iterator();
        while (i.hasNext()) {
            Flash flash = i.next();
            boolean dead = flash.age(deltaMs);
            if (dead) {
                i.remove();
            }
        }
    }
}
