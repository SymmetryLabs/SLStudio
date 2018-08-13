package com.symmetrylabs.slstudio.pattern.tree;

import heronarts.lx.LX;
import heronarts.lx.LXUtils;
import heronarts.lx.color.LXColor;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.FunctionalParameter;

import com.symmetrylabs.shows.kalpa.TreeModel;
import static com.symmetrylabs.util.MathUtils.*;


public class TreeSwarm extends TreePattern {
    public String getAuthor() {
        return "Mark C. Slee";
    }

    private static final int NUM_GROUPS = 5;

    public final CompoundParameter speed = (CompoundParameter)
        new CompoundParameter("Speed", 2000, 10000, 500)
            .setDescription("Speed of swarm motion")
            .setExponent(0.25f);

    public final CompoundParameter base =
        new CompoundParameter("Base", 10, 60, 1)
            .setDescription("Base size of swarm");

    public final CompoundParameter floor =
        new CompoundParameter("Floor", 20, 0, 100)
            .setDescription("Base level of swarm brightness");

    public final LXModulator[] pos = new LXModulator[NUM_GROUPS];

    public final LXModulator swarmX = startModulator(new SinLFO(
        startModulator(new SinLFO(0, 0.2f, startModulator(new SinLFO(3000, 9000, 17000).randomBasis()))),
        startModulator(new SinLFO(0.8f, 1, startModulator(new SinLFO(4000, 7000, 15000).randomBasis()))),
        startModulator(new SinLFO(9000, 17000, 33000).randomBasis())
    ).randomBasis());

    public final LXModulator swarmY = startModulator(new SinLFO(
        startModulator(new SinLFO(0, 0.2f, startModulator(new SinLFO(3000, 9000, 19000).randomBasis()))),
        startModulator(new SinLFO(0.8f, 1, startModulator(new SinLFO(4000, 7000, 13000).randomBasis()))),
        startModulator(new SinLFO(9000, 17000, 33000).randomBasis())
    ).randomBasis());

    public final LXModulator swarmZ = startModulator(new SinLFO(
        startModulator(new SinLFO(0, 0.2f, startModulator(new SinLFO(3000, 9000, 19000).randomBasis()))),
        startModulator(new SinLFO(0.8f, 1, startModulator(new SinLFO(4000, 7000, 13000).randomBasis()))),
        startModulator(new SinLFO(9000, 17000, 33000).randomBasis())
    ).randomBasis());

    public TreeSwarm(LX lx) {
        super(lx);
        addParameter("speed", this.speed);
        addParameter("base", this.base);
        addParameter("floor", this.floor);
        for (int i = 0; i < pos.length; ++i) {
            final int ii = i;
            float start = (i % 2 == 0) ? 0 : TreeModel.Twig.NUM_LEAVES;
            pos[i] = new SawLFO(start, TreeModel.Twig.NUM_LEAVES - start, new FunctionalParameter() {
                public double getValue() {
                    return speed.getValue() + ii*500;
                }
            }).randomBasis();
            startModulator(pos[i]);
        }
    }

    public void run(double deltaMs) {
        float base = this.base.getValuef();
        float swarmX = this.swarmX.getValuef();
        float swarmY = this.swarmY.getValuef();
        float swarmZ = this.swarmZ.getValuef();
        float floor = this.floor.getValuef();

        int i = 0;
        for (TreeModel.Twig twig : tree.getTwigs()) {
            float pos = this.pos[i++ % NUM_GROUPS].getValuef();
            int i1 = 0;
            for (TreeModel.Leaf leaf : twig.getLeaves()) {
                float falloff = min(100, base + 40 * dist(leaf.point.xn, leaf.point.yn, leaf.point.zn, swarmX, swarmY, swarmZ));
                float b = max(floor, 100 - falloff * LXUtils.wrapdistf(i1++, pos, TreeModel.Twig.LEAVES.length));
                setColor(leaf, LXColor.gray(b));
            }
        }
    }
}
