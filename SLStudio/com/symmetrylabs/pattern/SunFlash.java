package com.symmetrylabs.pattern;

import com.symmetrylabs.model.Sun;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class SunFlash extends SLPattern {
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
            c = model.suns.get(floor(random(model.suns.size())));
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
                    value = 1.0;
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
        float msPerFlash = 1000 / ((rateParameter.getValuef() + .01) * 100);
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
                color c = lx.hsb(flash.hue, saturationParameter.getValuef() * 100, (flash.value) * 100);
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
