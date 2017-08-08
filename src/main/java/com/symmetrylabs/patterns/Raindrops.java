//package com.symmetrylabs.patterns;

// requires it's own vector classes, but should prob refactor to use standard vector class
/*
public class Raindrops extends CubesPattern {

    CompoundParameter numRainDrops = new CompoundParameter("NUM", -40, -500, -20);
    CompoundParameter size = new CompoundParameter("SIZE", 0.35, 0.1, 1.0);
    CompoundParameter speedP = new CompoundParameter("SPD", -1000, -7000, -300);

    Vector3 randomVector3() {
        return new Vector3(
                random(model.xMax - model.xMin) + model.xMin,
                random(model.yMax - model.yMin) + model.yMin,
                random(model.zMax - model.zMin) + model.zMin);
    }

    class Raindrop {
        Vector3 p;
        Vector3 v;
        float radius;
        float hue;
        float speed;

        Raindrop() {
            this.radius = (model.yRange*.4)*size.getValuef();
            this.p = new Vector3(
                            random(model.xMax - model.xMin) + model.xMin,
                            model.yMax + this.radius,
                            random(model.zMax - model.zMin) + model.zMin);
            float velMagnitude = 120;
            this.v = new Vector3(
                    0,
                    -3 * model.yMax,
                    0);
            this.hue = random(15) + palette.getHuef();
            this.speed = Math.abs(speedP.getValuef());
        }

        // returns TRUE when this should die
        boolean age(double ms) {
            p.add(v, (float) (ms / this.speed));
            return this.p.y < (0 - this.radius);
        }
    }

    private float leftoverMs = 0;
    private float msPerRaindrop = 40;
    private List<Raindrop> raindrops;

    public Raindrops(LX lx) {
        super(lx);
        addParameter(numRainDrops);
        addParameter(size);
        addParameter(speedP);
        raindrops = new LinkedList<Raindrop>();
    }

    public void run(double deltaMs) {
        leftoverMs += deltaMs;
        float msPerRaindrop = Math.abs(numRainDrops.getValuef());
        while (leftoverMs > msPerRaindrop) {
            leftoverMs -= msPerRaindrop;
            raindrops.add(new Raindrop());
        }

        for (LXPoint p : model.points) {
            color c =
                PImage.blendColor(
                    lx.hsb(210, 20, (float)Math.max(0, 1 - Math.pow((model.yMax - p.y) / 10, 2)) * 50),
                    lx.hsb(220, 60, (float)Math.max(0, 1 - Math.pow((p.y - model.yMin) / 10, 2)) * 100),
                    ADD);
            for (Raindrop raindrop : raindrops) {
                if (p.x >= (raindrop.p.x - raindrop.radius) && p.x <= (raindrop.p.x + raindrop.radius) &&
                        p.y >= (raindrop.p.y - raindrop.radius) && p.y <= (raindrop.p.y + raindrop.radius)) {
                    float d = raindrop.p.distanceTo(p) / raindrop.radius;
    //      float value = (float)Math.max(0, 1 - Math.pow(Math.min(0, d - raindrop.radius) / 5, 2));
                    if (d < 1) {
                        c = PImage.blendColor(c, lx.hsb(raindrop.hue, 80, (float)Math.pow(1 - d, 0.01) * 100), ADD);
                    }
                }
            }
            colors[p.index] = c;
        }

        Iterator<Raindrop> i = raindrops.iterator();
        while (i.hasNext()) {
            Raindrop raindrop = i.next();
            boolean dead = raindrop.age(deltaMs);
            if (dead) {
                i.remove();
            }
        }
    }
}
*/
