package com.symmetrylabs.slstudio.pattern.tree;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.modulator.DampedParameter;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;

import com.symmetrylabs.shows.tree.TreeModel;
import static com.symmetrylabs.util.DistanceConstants.*;
import static com.symmetrylabs.util.MathUtils.*;

public class TreeVortex extends TreePattern {

    public String getAuthor() {
        return "Mark C. Slee";
    }

    public final CompoundParameter speed = (CompoundParameter)
        new CompoundParameter("Speed", 2000, 9000, 300)
            .setExponent(0.5f)
            .setDescription("Speed of vortex motion");

    public final CompoundParameter size =
        new CompoundParameter("Size",  4*FEET, 1*FEET, 10*FEET)
            .setDescription("Size of vortex");

    public final CompoundParameter xPos = (CompoundParameter)
        new CompoundParameter("XPos", model.cx, model.xMin, model.xMax)
            .setPolarity(LXParameter.Polarity.BIPOLAR)
            .setDescription("X-position of vortex center");

    public final CompoundParameter yPos = (CompoundParameter)
        new CompoundParameter("YPos", model.cy, model.yMin, model.yMax)
            .setPolarity(LXParameter.Polarity.BIPOLAR)
            .setDescription("Y-position of vortex center");

    public final CompoundParameter xSlope = (CompoundParameter)
        new CompoundParameter("XSlp", 0.2f, -1, 1)
            .setPolarity(LXParameter.Polarity.BIPOLAR)
            .setDescription("X-slope of vortex center");

    public final CompoundParameter ySlope = (CompoundParameter)
        new CompoundParameter("YSlp", 0.5f, -1, 1)
            .setPolarity(LXParameter.Polarity.BIPOLAR)
            .setDescription("Y-slope of vortex center");

    public final CompoundParameter zSlope = (CompoundParameter)
        new CompoundParameter("ZSlp", 0.3f, -1, 1)
            .setPolarity(LXParameter.Polarity.BIPOLAR)
            .setDescription("Z-slope of vortex center");

    private final LXModulator pos = startModulator(new SawLFO(1, 0, this.speed));

    private final LXModulator sizeDamped = startModulator(new DampedParameter(this.size, 5*FEET, 8*FEET));
    private final LXModulator xPosDamped = startModulator(new DampedParameter(this.xPos, model.xRange, 3*model.xRange));
    private final LXModulator yPosDamped = startModulator(new DampedParameter(this.yPos, model.yRange, 3*model.yRange));
    private final LXModulator xSlopeDamped = startModulator(new DampedParameter(this.xSlope, 3, 6));
    private final LXModulator ySlopeDamped = startModulator(new DampedParameter(this.ySlope, 3, 6));
    private final LXModulator zSlopeDamped = startModulator(new DampedParameter(this.zSlope, 3, 6));

    public TreeVortex(LX lx) {
        super(lx);
        addParameter("speed", this.speed);
        addParameter("size", this.size);
        addParameter("xPos", this.xPos);
        addParameter("yPos", this.yPos);
        addParameter("xSlope", this.xSlope);
        addParameter("ySlope", this.ySlope);
        addParameter("zSlope", this.zSlope);
    }

    public void run(double deltaMs) {
        final float xPos = this.xPosDamped.getValuef();
        final float yPos = this.yPosDamped.getValuef();
        final float size = this.sizeDamped.getValuef();
        final float pos = this.pos.getValuef();

        final float xSlope = this.xSlopeDamped.getValuef();
        final float ySlope = this.ySlopeDamped.getValuef();
        final float zSlope = this.zSlopeDamped.getValuef();

        float dMult = 2 / size;
        for (TreeModel.Leaf leaf : tree.getLeaves()) {
            float radix = abs((xSlope*abs(leaf.x-model.cx) + ySlope*abs(leaf.y-model.cy) + zSlope*abs(leaf.z-model.cz)));
            float dist = dist(leaf.x, leaf.y, xPos, yPos);
            //float falloff = 100 / max(20*INCHES, 2*size - .5*dist);
            //float b = 100 - falloff * LXUtils.wrapdistf(radix, pos * size, size);
            float b = abs(((dist + radix + pos * size) % size) * dMult - 1);
            setColor(leaf, (b > 0) ? LXColor.gray(b*b*100) : 0);
        }
    }
}
