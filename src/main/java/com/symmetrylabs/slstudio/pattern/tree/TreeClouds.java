package com.symmetrylabs.slstudio.pattern.tree;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;

import com.symmetrylabs.layouts.tree.TreeModel;
import static com.symmetrylabs.util.NoiseUtils.*;
import static com.symmetrylabs.util.MathUtils.*;


public class TreeClouds extends TreePattern {

    public String getAuthor() {
        return "Mark C. Slee";
    }

    public final CompoundParameter thickness =
        new CompoundParameter("Thickness", 50, 100, 0)
            .setDescription("Thickness of the cloud formation");

    public final CompoundParameter xSpeed = (CompoundParameter)
        new CompoundParameter("XSpd", 0, -1, 1)
            .setPolarity(LXParameter.Polarity.BIPOLAR)
            .setDescription("Motion along the X axis");

    public final CompoundParameter ySpeed = (CompoundParameter)
        new CompoundParameter("YSpd", 0, -1, 1)
            .setPolarity(LXParameter.Polarity.BIPOLAR)
            .setDescription("Motion along the Y axis");

    public final CompoundParameter zSpeed = (CompoundParameter)
        new CompoundParameter("ZSpd", 0, -1, 1)
            .setPolarity(LXParameter.Polarity.BIPOLAR)
            .setDescription("Motion along the Z axis");

    public final CompoundParameter scale = (CompoundParameter)
        new CompoundParameter("Scale", 3, 0.25f, 10)
            .setDescription("Scale of the clouds")
            .setExponent(2);

    public final CompoundParameter xScale =
        new CompoundParameter("XScale", 0, 0, 10)
            .setDescription("Scale along the X axis");

    public final CompoundParameter yScale =
        new CompoundParameter("YScale", 0, 0, 10)
            .setDescription("Scale along the Y axis");

    public final CompoundParameter zScale =
        new CompoundParameter("ZScale", 0, 0, 10)
            .setDescription("Scale along the Z axis");

    private float xBasis = 0, yBasis = 0, zBasis = 0;

    public TreeClouds(LX lx) {
        super(lx);
        addParameter("thickness", this.thickness);
        addParameter("xSpeed", this.xSpeed);
        addParameter("ySpeed", this.ySpeed);
        addParameter("zSpeed", this.zSpeed);
        addParameter("scale", this.scale);
        addParameter("xScale", this.xScale);
        addParameter("yScale", this.yScale);
        addParameter("zScale", this.zScale);
    }

    private static final double MOTION = 0.0005f;

    public void run(double deltaMs) {
        this.xBasis -= deltaMs * MOTION * this.xSpeed.getValuef();
        this.yBasis -= deltaMs * MOTION * this.ySpeed.getValuef();
        this.zBasis -= deltaMs * MOTION * this.zSpeed.getValuef();
        float thickness = this.thickness.getValuef();
        float scale = this.scale.getValuef();
        float xScale = this.xScale.getValuef();
        float yScale = this.yScale.getValuef();
        float zScale = this.zScale.getValuef();
        for (TreeModel.Leaf leaf : tree.getLeaves()) {
            float nv = noise(
                (scale + leaf.point.xn * xScale) * leaf.point.xn + this.xBasis,
                (scale + leaf.point.yn * yScale) * leaf.point.yn + this.yBasis,
                (scale + leaf.point.zn * zScale) * leaf.point.zn + this.zBasis
            );
            setColor(leaf, LXColor.gray(constrain(-thickness + (150 + thickness) * nv, 0, 100)));
        }
    }
}
