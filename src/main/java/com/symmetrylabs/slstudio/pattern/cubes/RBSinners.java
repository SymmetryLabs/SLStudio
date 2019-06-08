package com.symmetrylabs.slstudio.pattern.cubes;

import com.symmetrylabs.shows.cubes.*;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.transform.LXVector;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import static com.symmetrylabs.util.MathUtils.random;
import static heronarts.lx.PolyBuffer.Space.SRGB8;
import static processing.core.PApplet.floor;

public class RBSinners extends SLPattern<CubesModel> {
    public static final String GROUP_NAME = CubesShow.SHOW_NAME;

    private CompoundParameter rateParameter = new CompoundParameter("Speed", 0.125);
    private CompoundParameter attackParameter = new CompoundParameter("Attack", 0.5);
    private CompoundParameter decayParameter = new CompoundParameter("Decay", 0.5);
    private CompoundParameter hue1 = new CompoundParameter("Hue1", 0.25);
    private CompoundParameter hue2 = new CompoundParameter("Hue2", 0.25);
    private CompoundParameter sat1 = new CompoundParameter("Sat1", 0.25);
    private CompoundParameter sat2 = new CompoundParameter("Sat2", 0.25);
    private boolean[] occupied;

    class Flash {
        CubesModel.Cube cube;
        float value;
        float hue;
        boolean hasPeaked;
        boolean isFirst;
        int index;

        Flash(int index, boolean isFirst) {
            this.isFirst = isFirst;
            this.index = index;
            cube = model.getCubes().get(index);
            hue = isFirst ? hue1.getValuef() : hue2.getValuef();
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

    public RBSinners(LX lx) {
        super(lx);

        addParameter(hue1);
        addParameter(hue2);
        addParameter(sat1);
        addParameter(sat2);
        addParameter(rateParameter);
        addParameter(attackParameter);
        addParameter(decayParameter);
        flashes = new LinkedList<Flash>();
        this.occupied = new boolean[(model.getCubes()).size()];

        for (int i = 0; i < occupied.length; i++) {
            occupied[i] = false;
        }
    }

    public void run(double deltaMs, PolyBuffer.Space space) {
        int[] colors = (int[]) getArray(SRGB8);

        leftoverMs += deltaMs;
        float msPerFlash = 1000 / ((rateParameter.getValuef() + .01f) * 500);
        while (leftoverMs > msPerFlash) {
            leftoverMs -= msPerFlash;

            int randomIndex = floor(random(model.getCubes().size()));
            if (!occupied[randomIndex]) {
                flashes.add(new Flash(randomIndex, Math.random() < 0.5));
                occupied[randomIndex] = true;
            }
        }

        setColors(0);

        float sat1f = sat1.getValuef();
        float sat2f = sat2.getValuef();

        flashes.parallelStream().forEach(new Consumer<Flash>() {
            @Override
            public void accept(final Flash flash) {
                float sat = flash.isFirst ? sat1f : sat2f;
                int col = LXColor.hsb(flash.hue * 360, sat * 100, (flash.value) * 100);
                for (LXVector v : getVectors(flash.cube.getPoints())) {
                    colors[v.index] = col;
                }
            }
        });

        Iterator<Flash> i = flashes.iterator();
        while (i.hasNext()) {
            Flash flash = i.next();
            boolean dead = flash.age(deltaMs);
            if (dead) {
                occupied[flash.index] = false;
                i.remove();
            }
        }
        markModified(SRGB8);
    }
}
