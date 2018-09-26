package com.symmetrylabs.slstudio.pattern.tree;

import com.symmetrylabs.shows.cubes.CubesShow;
import heronarts.lx.LX;
import heronarts.lx.LXUtils;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;

import com.symmetrylabs.shows.tree.TreeModel;
import static com.symmetrylabs.util.MathUtils.*;


public class PatternScanner extends TreePattern {
    public String getAuthor() {
        return "Mark C. Slee";
    }

    public final CompoundParameter speed = (CompoundParameter)
        new CompoundParameter("Speed", 0.5f, -1, 1)
            .setPolarity(LXParameter.Polarity.BIPOLAR)
            .setDescription("Speed that the plane moves at");

    public final CompoundParameter sharp = (CompoundParameter)
        new CompoundParameter("Sharp", 0, -50, 150)
            .setDescription("Sharpness of the falling plane")
            .setExponent(2);

    public final CompoundParameter xSlope = (CompoundParameter)
        new CompoundParameter("XSlope", 0, -1, 1)
            .setDescription("Slope on the X-axis");

    public final CompoundParameter zSlope = (CompoundParameter)
        new CompoundParameter("ZSlope", 0, -1, 1)
            .setDescription("Slope on the Z-axis");

    private float basis = 0;

    public PatternScanner(LX lx) {
        super(lx);
        addParameter("speed", this.speed);
        addParameter("sharp", this.sharp);
        addParameter("xSlope", this.xSlope);
        addParameter("zSlope", this.zSlope);
    }

    public void run(double deltaMs) {
        float speed = this.speed.getValuef();
        speed = speed * speed * ((speed < 0) ? -1 : 1);
        float sharp = this.sharp.getValuef();
        float xSlope = this.xSlope.getValuef();
        float zSlope = this.zSlope.getValuef();
        this.basis = (float) (this.basis - 0.001f * speed * deltaMs) % 1.0f;
        for (TreeModel.Leaf leaf : model.leaves) {
            setColor(leaf, LXColor.gray(max(0, 50 - sharp + (50 + sharp) * LXUtils.trif(leaf.point.yn + this.basis + (leaf.point.xn-0.5f) * xSlope + (leaf.point.zn-0.5f) * zSlope))));
        }
    }
}
