package com.symmetrylabs.slstudio.pattern.tree;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;

import com.symmetrylabs.layouts.tree.TreeModel;
import static com.symmetrylabs.util.MathUtils.*;


public class ColorRain extends TreePattern {
    public String getAuthor() {
        return "Mark C. Slee";
    }

    public final CompoundParameter speed = new CompoundParameter("Speed", 1, 5)
        .setDescription("Speed of the rainfall");

    public final CompoundParameter range = new CompoundParameter("Range", 30, 5, 50)
        .setDescription("Range of blue depth");

    private static final int BUCKETS = 37;
    private final float[][] buckets = new float[BUCKETS+1][BUCKETS+1];

    public ColorRain(LX lx) {
        super(lx);
        addParameter("speed", this.speed);
        addParameter("range", this.range);
        for (int i = 0; i < BUCKETS+1; ++i) {
            for (int j = 0; j < BUCKETS+1; ++j) {
                this.buckets[i][j] = random(1);
            }
        }
    }

    private double accum = 0;

    public void run(double deltaMs) {
        int range = (int) this.range.getValue();
        accum += this.speed.getValue() * .02 * deltaMs;
        float saturation = palette.getSaturationf();
        for (TreeModel.Leaf leaf : model.leaves) {
            float offset = this.buckets[(int) (BUCKETS * leaf.point.xn)][(int) (BUCKETS * leaf.point.zn)];
            int hMove = ((int) (180 * leaf.point.yn + 120 * offset + accum)) % 80;
            if (hMove > range) {
                hMove = (int)max(0, range - 8*(hMove - range));
            }
            setColor(leaf, LXColor.hsb(
                210 - hMove,
                saturation,
                100
            ));
        }
    }
}
