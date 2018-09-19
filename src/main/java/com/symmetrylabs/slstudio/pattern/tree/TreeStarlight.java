package com.symmetrylabs.slstudio.pattern.tree;

import heronarts.lx.LX;
import heronarts.lx.LXUtils;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;

import java.util.ArrayList;
import java.util.Collections;

import com.symmetrylabs.shows.tree.TreeModel;
import static com.symmetrylabs.util.MathUtils.*;
import static com.symmetrylabs.util.MathConstants.*;


public class TreeStarlight extends TreePattern {
    public String getAuthor() {
        return "Mark C. Slee";
    }

    final static int MAX_STARS = 5000;
    final static int LEAVES_PER_STAR = 3;

    final LXUtils.LookupTable flicker = new LXUtils.LookupTable(360, new LXUtils.LookupTable.Function() {
        public float compute(int i, int tableSize) {
            return 0.5f - 0.5f * cos(i * TWO_PI / tableSize);
        }
    });

    public final CompoundParameter speed =
        new CompoundParameter("Speed", 3000, 9000, 300)
            .setDescription("Speed of the twinkling");

    public final CompoundParameter variance =
        new CompoundParameter("Variance", 0.5f, 0, 0.9f)
            .setDescription("Variance of the twinkling");

    public final CompoundParameter numStars = (CompoundParameter)
        new CompoundParameter("Num", 5000, 50, MAX_STARS)
            .setExponent(2)
            .setDescription("Number of stars");

    private final Star[] stars = new Star[MAX_STARS];

    private final ArrayList<TreeModel.Leaf> shuffledLeaves;

    public TreeStarlight(LX lx) {
        super(lx);
        addParameter("speed", this.speed);
        addParameter("numStars", this.numStars);
        addParameter("variance", this.variance);
        this.shuffledLeaves = new ArrayList<TreeModel.Leaf>(model.leaves);
        Collections.shuffle(this.shuffledLeaves);
        for (int i = 0; i < MAX_STARS; ++i) {
            this.stars[i] = new Star(i);
        }
    }

    public void run(double deltaMs) {
        setColors(0);
        float numStars = this.numStars.getValuef();
        float speed = this.speed.getValuef();
        float variance = this.variance.getValuef();
        for (Star star : this.stars) {
            if (star.active) {
                star.run(deltaMs);
            } else if (star.num < numStars) {
                star.activate(speed, variance);
            }
        }
    }

    class Star {

        final int num;

        double period;
        float amplitude = 50;
        double accum = 0;
        boolean active = false;

        Star(int num) {
            this.num = num;
        }

        void activate(float speed, float variance) {
            this.period = max(400, speed * (1 + random(-variance, variance)));
            this.accum = 0;
            this.amplitude = random(20, 100);
            this.active = true;
        }

        void run(double deltaMs) {
            int c = LXColor.gray(this.amplitude * flicker.get(this.accum / this.period));
            int maxLeaves = shuffledLeaves.size();
            for (int i = 0; i < LEAVES_PER_STAR; ++i) {
                int leafIndex = num * LEAVES_PER_STAR + i;
                if (leafIndex < maxLeaves) {
                    setColor(shuffledLeaves.get(leafIndex), c);
                }
            }
            this.accum += deltaMs;
            if (this.accum > this.period) {
                this.active = false;
            }
        }
    }

}
