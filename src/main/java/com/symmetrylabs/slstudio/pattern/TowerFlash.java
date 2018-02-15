package com.symmetrylabs.slstudio.pattern;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Consumer;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.LXPattern;

import com.symmetrylabs.slstudio.model.CubesModel;

import static com.symmetrylabs.slstudio.util.MathUtils.random;
import static processing.core.PApplet.*;


public class TowerFlash extends LXPattern {
    private CompoundParameter rateParameter = new CompoundParameter("RATE", 0.125);
    private CompoundParameter attackParameter = new CompoundParameter("ATTK", 0.5);
    private CompoundParameter decayParameter = new CompoundParameter("DECAY", 0.5);
    private CompoundParameter hueVarianceParameter = new CompoundParameter("H.V.", 0.25);
    private CompoundParameter saturationParameter = new CompoundParameter("SAT", 0.5);

    private List<Flash> checkedFlashes = new ArrayList<Flash>();

    class Flash {
        CubesModel.Tower c;
        float value;
        float hue;
        boolean hasPeaked;
        boolean checked = false;

        Flash(CubesModel.Tower c) {
            this.c = c;
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

    public TowerFlash(LX lx) {
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

            CubesModel.Tower c = ((CubesModel)model).getTowers().get(floor(random(((CubesModel)model).getTowers().size())));

            //System.out.println("iterate");
            for (Flash flash : flashes) {
                if (flash.c.id.equals(c.id)) {
                    //System.out.println("tried to make a duplicate");
                    continue;
                }
            }

            //System.out.println("making new flash!");
            flashes.add(new Flash(c));
        }
        // for (Flash f1 : flashes) {
        //  boolean foundFirstSelf = false;

        //  for (Flash f2 : flashes) {
        //    if (foundFirstSelf && f1.c.id.equals(f2.c.id)) {
        //      flashes.remove(f1);
        //    }

        //    if (f1.c.id.equals(f2.c.id)) {
        //      foundFirstSelf = true;
        //    }
        //  }
        // }

        for (LXPoint p : model.points) {
            colors[p.index] = 0;
        }

        // checkedFlashes.parallelStream().forEach(new Consumer<Flash>() {
        //  @Override
        //  public void accept(final Flash flash) {

        for (Flash flash : flashes) {
                //System.out.println("drawing flash");
                int c = lx.hsb(flash.hue, saturationParameter.getValuef() * 100, (flash.value) * 100);
                for (LXPoint p : flash.c.points) {
                    colors[p.index] = c;
                }
        }
        //  }
        // });

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
