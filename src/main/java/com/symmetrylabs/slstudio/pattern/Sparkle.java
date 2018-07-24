package com.symmetrylabs.slstudio.pattern;

import com.symmetrylabs.color.Ops8;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.LXUtils;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.transform.LXVector;

public class Sparkle extends LXPattern {
    private CompoundParameter densityParameter = new CompoundParameter("Dens", 0.15);
    private CompoundParameter attackParameter = new CompoundParameter("Attack", 0.4);
    private CompoundParameter decayParameter = new CompoundParameter("Decay", 0.3);
    private CompoundParameter hueParameter = new CompoundParameter("Hue", 0.5);
    private CompoundParameter hueVarianceParameter = new CompoundParameter("HueVar", 0.25);
    private CompoundParameter saturationParameter = new CompoundParameter("Sat", 0.5);

    class Spark {
        LXVector vector;
        float value;
        float hue;
        boolean hasPeaked;

        Spark() {
            List<LXVector> vectors = getVectors();
            vector = vectors.get((int) Math.floor(LXUtils.random(0, vectors.size())));
            hue = (float) LXUtils.random(0, 1);
            boolean infiniteAttack = (attackParameter.getValuef() > 0.999);
            hasPeaked = infiniteAttack;
            value = (infiniteAttack ? 1 : 0);
        }

        // returns TRUE if this should die
        boolean age(double ms) {
            if (!hasPeaked) {
                value = value + (float) (ms / 1000.0f * ((attackParameter.getValuef() + 0.01) * 5));
                if (value >= 1.0) {
                    value = (float)1.0;
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
    private List<Spark> sparks;

    public Sparkle(LX lx) {
        super(lx);
        addParameter(densityParameter);
        addParameter(attackParameter);
        addParameter(decayParameter);
        addParameter(hueParameter);
        addParameter(hueVarianceParameter);
        addParameter(saturationParameter);
        sparks = new LinkedList<Spark>();
    }

    public void run(double deltaMs) {
        leftoverMs += deltaMs;
        float msPerSpark = 1000.f / (float)((densityParameter.getValuef() + .01) * (model.xRange*10));
        while (leftoverMs > msPerSpark) {
            leftoverMs -= msPerSpark;
            sparks.add(new Spark());
        }

        Arrays.fill(colors, Ops8.BLACK);

        for (Spark spark : sparks) {
            float hue = ((float)(hueParameter.getValuef() + (hueVarianceParameter.getValuef() * spark.hue))) % 1.0f;
            int c = lx.hsb(hue * 360, saturationParameter.getValuef() * 100, (spark.value) * 100);
            colors[spark.vector.index] = c;
        }

        Iterator<Spark> i = sparks.iterator();
        while (i.hasNext()) {
            Spark spark = i.next();
            boolean dead = spark.age(deltaMs);
            if (dead) {
                i.remove();
            }
        }
    }
}
