package com.symmetrylabs.slstudio.pattern;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;

import com.symmetrylabs.slstudio.model.Sun;

import static com.symmetrylabs.slstudio.util.MathUtils.random;
import static processing.core.PApplet.*;


public class SunFlash extends SunsPattern {
    private CompoundParameter rateParameter = new CompoundParameter("RATE", 0.125);
    private CompoundParameter attackParameter = new CompoundParameter("ATTK", 0.5);
    private CompoundParameter decayParameter = new CompoundParameter("DECAY", 0.5);
    private CompoundParameter hueVarianceParameter = new CompoundParameter("H.V.", 0.25);
    private CompoundParameter saturationParameter = new CompoundParameter("SAT", 0.5);

    class Flash {
        Sun c;
        float value;
        float hue;
        boolean hasPeaked;

        Flash() {
            c = model.getSuns().get(floor(random(model.getSuns().size())));
            hue = palette.getHuef() + (random(1) * 120 * hueVarianceParameter.getValuef());
            boolean infiniteAttack = (attackParameter.getValuef() > 0.999);
            hasPeaked = infiniteAttack;
            value = (infiniteAttack ? 1 : 0);
        }

        // returns TRUE if this should die
        boolean age(double ms) {
            if (!hasPeaked) {
                value = value + (float) (ms / 1000.0f * ((attackParameter.getValuef() + 0.01) * 5));
                if (value >= 1.0) {
                    value = 1.0f;
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

    public SunFlash(LX lx) {
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

        flashes.parallelStream().forEach(new Consumer<Flash>() {
            @Override
            public void accept(final Flash flash) {
                int c = lx.hsb(flash.hue, saturationParameter.getValuef() * 100, (flash.value) * 100);
                for (LXPoint p : flash.c.points) {
                    colors[p.index] = c;
                }
            }
        });

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