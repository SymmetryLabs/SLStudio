package com.symmetrylabs.slstudio.pattern;

import com.symmetrylabs.color.Ops8;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.LXUtils;
import heronarts.lx.PolyBuffer;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.transform.LXVector;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static com.symmetrylabs.util.MathUtils.random;
import static heronarts.lx.PolyBuffer.Space.SRGB8;
import static processing.core.PApplet.abs;

public class Raindrops extends SLPattern<SLModel> {

    public final CompoundParameter numRainDrops = new CompoundParameter("Number", -40, -500, -20);
    public final CompoundParameter size = new CompoundParameter("Size", 0.35, 0.1, 1.0);
    public final CompoundParameter speedP = new CompoundParameter("Speed", -1000, -7000, -300);
    public final CompoundParameter hueV = new CompoundParameter("HueVar", 0.5);

    private float leftoverMs = 0;
    private float msPerRaindrop = 40;
    private List<Raindrop> raindrops;

    class Raindrop {
        LXVector p;
        LXVector v;
        float radius;
        float hue;
        float speed;

        Raindrop() {
            this.radius = (float)((model.yRange*0.4f)*size.getValuef());

            this.p = new LXVector(
                random(model.xMax - model.xMin) + model.xMin,
                model.yMax + this.radius,
                random(model.zMax - model.zMin) + model.zMin
            );

            float velMagnitude = 120;
            this.v = new LXVector(0, -3 * model.yMax, 0);
            this.hue = (random(0, 50) * hueV.getValuef()) + palette.getHuef();
            this.speed = abs(speedP.getValuef());
        }

        // returns TRUE when this should die
        boolean age(double ms) {
            p.add(new LXVector(v).mult((float) (ms / this.speed)));
            return this.p.y < (0 - this.radius);
        }
    }

    public Raindrops(LX lx) {
        super(lx);
        addParameter(numRainDrops);
        addParameter(size);
        addParameter(speedP);
        addParameter(hueV);
        raindrops = new LinkedList<Raindrop>();
    }

    private LXVector randomVector() {
        return new LXVector(
            random(model.xMax - model.xMin) + model.xMin,
            random(model.yMax - model.yMin) + model.yMin,
            random(model.zMax - model.zMin) + model.zMin
        );
    }

    public void run(double deltaMs, PolyBuffer.Space space) {
        int[] colors = (int[]) getArray(SRGB8);

        leftoverMs += deltaMs;
        float msPerRaindrop = Math.abs(numRainDrops.getValuef());
        while (leftoverMs > msPerRaindrop) {
            leftoverMs -= msPerRaindrop;
            raindrops.add(new Raindrop());
        }

        getVectorList().parallelStream().forEach(p -> {
            int c = 0;
            for (Raindrop raindrop : raindrops) {
                if (p.x >= (raindrop.p.x - raindrop.radius) && p.x <= (raindrop.p.x + raindrop.radius) &&
                        p.y >= (raindrop.p.y - raindrop.radius) && p.y <= (raindrop.p.y + raindrop.radius)) {

                    float d = ((float)LXUtils.distance(raindrop.p.x, raindrop.p.y, p.x, p.y)) / raindrop.radius;
                    if (d < 1) {
                        c = Ops8.add(c, LXColor.hsb(raindrop.hue, 80, (float)Math.pow(1 - d, 0.01) * 100));
                    }
                }
            }
            colors[p.index] = c;
        });

        Iterator<Raindrop> i = raindrops.iterator();
        while (i.hasNext()) {
            Raindrop raindrop = i.next();
            boolean dead = raindrop.age(deltaMs);
            if (dead) {
                i.remove();
            }
        }
        markModified(SRGB8);
    }
}


