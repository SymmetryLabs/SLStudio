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

import com.symmetrylabs.shows.summerstage.SummerStageShow;

public class Sparkle extends LXPattern {
    public static final String GROUP_NAME = SummerStageShow.SHOW_NAME;

    private CompoundParameter speedParameter = new CompoundParameter("Speed", 1.0, 0.0, 2.0);
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
        int index;
        boolean hasPeaked;

        Spark(int index) {
            List<LXVector> vectors = getVectorList();
            this.index = index;
            vector = vectors.get(index);
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
    private boolean[] occupied;


    public Sparkle(LX lx) {
        super(lx);
        addParameter(speedParameter);
        addParameter(densityParameter);
        addParameter(attackParameter);
        addParameter(decayParameter);
        addParameter(hueParameter);
        addParameter(hueVarianceParameter);
        addParameter(saturationParameter);
        sparks = new LinkedList<Spark>();
        occupied = new boolean[getVectorList().size()];
        for (int i = 0; i < occupied.length; i++) {
            occupied[i] = false;
        }
    }

    public void run(double deltaMs) {
        deltaMs *= speedParameter.getValuef();

        leftoverMs += deltaMs;
        float msPerSpark = 1000.f / (float)((densityParameter.getValuef() + .01) * (model.xRange*10));
        while (leftoverMs > msPerSpark) {
            leftoverMs -= msPerSpark;
            int index = (int) Math.floor(LXUtils.random(0, getVectorList().size()));
            if (!occupied[index]) {
                sparks.add(new Spark(index));
                occupied[index] = true;
            }
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
                occupied[spark.index] = false;
                i.remove();
            }
        }
    }
}
