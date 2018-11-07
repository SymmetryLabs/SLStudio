package com.symmetrylabs.slstudio.pattern.tree;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.modulator.DampedParameter;

import com.symmetrylabs.shows.tree.TreeModel;
import static com.symmetrylabs.util.MathUtils.*;


public class ColorGradientTree extends TreePattern {
    public String getAuthor() {
        return "Mark C. Slee";
    }

    public final CompoundParameter slopeX = (CompoundParameter)
        new CompoundParameter("SlpX", 0, -1, 1)
            .setPolarity(LXParameter.Polarity.BIPOLAR)
            .setDescription("Slope of gradient on X-axis");

    public final CompoundParameter slopeZ = (CompoundParameter)
        new CompoundParameter("SlpZ", 0, -1, 1)
            .setPolarity(LXParameter.Polarity.BIPOLAR)
            .setDescription("Slope of gradient on Z-axis");

    private final LXModulator spread = startModulator(new DampedParameter(palette.spread, 720, 720));
    private final LXModulator spreadX = startModulator(new DampedParameter(palette.spreadX, 2, 2));
    private final LXModulator spreadY = startModulator(new DampedParameter(palette.spreadY, 2, 2));
    private final LXModulator spreadZ = startModulator(new DampedParameter(palette.spreadZ, 2, 2));
    private final LXModulator offsetX = startModulator(new DampedParameter(palette.offsetX, 5, 5));
    private final LXModulator offsetY = startModulator(new DampedParameter(palette.offsetY, 5, 5));
    private final LXModulator offsetZ = startModulator(new DampedParameter(palette.offsetZ, 5, 5));

    public ColorGradientTree(LX lx) {
        super(lx);
        addParameter("slopeX", this.slopeX);
        addParameter("slopeZ", this.slopeZ);
    }

    public void run(double deltaMs) {
        float hue = palette.getHuef();
        float sat = palette.getSaturationf();
        float spread = this.spread.getValuef();
        float spreadX = spread * this.spreadX.getValuef();
        float spreadY = spread * this.spreadY.getValuef();
        float spreadZ = spread * this.spreadZ.getValuef();
        float offsetX = this.offsetX.getValuef();
        float offsetY = this.offsetY.getValuef();
        float offsetZ = this.offsetZ.getValuef();
        float slopeX = this.slopeX.getValuef();
        float slopeZ = this.slopeZ.getValuef();
        boolean mirror = palette.mirror.isOn();
        for (TreeModel.Leaf leaf : tree.leaves) {
            float dx = leaf.point.xn - 0.5f - offsetX;
            float dy = leaf.point.yn - 0.5f - offsetY + slopeX * (0.5f - leaf.point.xn) + slopeZ * (0.5f - leaf.point.zn);
            float dz = leaf.point.zn - 0.5f - offsetZ;
            if (mirror) {
                dx = abs(dx);
                dy = abs(dy);
                dz = abs(dz);
            }
            setColor(leaf, LXColor.hsb(
                hue + spreadX*dx + spreadY*dy + spreadZ*dz,
                sat,
                100
            ));
        }
    }
}
