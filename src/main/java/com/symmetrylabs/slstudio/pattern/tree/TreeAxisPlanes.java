package com.symmetrylabs.slstudio.pattern.tree;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.CompoundParameter;

import com.symmetrylabs.shows.tree.TreeModel;
import static com.symmetrylabs.util.MathUtils.*;


public class TreeAxisPlanes extends TreePattern {
    public String getAuthor() {
        return "Mark C. Slee";
    }

    public final CompoundParameter xSpeed = new CompoundParameter("XSpd", 19000, 31000, 5000).setDescription("Speed of motion on X-axis");
    public final CompoundParameter ySpeed = new CompoundParameter("YSpd", 13000, 31000, 5000).setDescription("Speed of motion on Y-axis");
    public final CompoundParameter zSpeed = new CompoundParameter("ZSpd", 17000, 31000, 5000).setDescription("Speed of motion on Z-axis");

    public final CompoundParameter xSize = new CompoundParameter("XSize", 0.1f, 0.05f, 0.3f).setDescription("Size of X scanner");
    public final CompoundParameter ySize = new CompoundParameter("YSize", 0.1f, 0.05f, 0.3f).setDescription("Size of Y scanner");
    public final CompoundParameter zSize = new CompoundParameter("ZSize", 0.1f, 0.05f, 0.3f).setDescription("Size of Z scanner");

    private final LXModulator xPos = startModulator(new SinLFO(0, 1, this.xSpeed).randomBasis());
    private final LXModulator yPos = startModulator(new SinLFO(0, 1, this.ySpeed).randomBasis());
    private final LXModulator zPos = startModulator(new SinLFO(0, 1, this.zSpeed).randomBasis());

    public TreeAxisPlanes(LX lx) {
        super(lx);
        addParameter("xSpeed", this.xSpeed);
        addParameter("ySpeed", this.ySpeed);
        addParameter("zSpeed", this.zSpeed);
        addParameter("xSize", this.xSize);
        addParameter("ySize", this.ySize);
        addParameter("zSize", this.zSize);
    }

    public void run(double deltaMs) {
        float xPos = this.xPos.getValuef();
        float yPos = this.yPos.getValuef();
        float zPos = this.zPos.getValuef();
        float xFalloff = 100 / this.xSize.getValuef();
        float yFalloff = 100 / this.ySize.getValuef();
        float zFalloff = 100 / this.zSize.getValuef();

        for (TreeModel.Leaf leaf : model.getLeaves()) {
            float b = max(max(
                100 - xFalloff * abs(leaf.point.xn - xPos),
                100 - yFalloff * abs(leaf.point.yn - yPos)),
                100 - zFalloff * abs(leaf.point.zn - zPos)
            );
            setColor(leaf, LXColor.gray(max(0, b)));
        }
    }
}
