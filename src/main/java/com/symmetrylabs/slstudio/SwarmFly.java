//package com.symmetrylabs.slstudio.pattern;
//
//import java.util.List;
//import java.util.ArrayList;
//import java.util.Set;
//import java.util.concurrent.CopyOnWriteArraySet;
//import java.util.Collection;
//import java.util.Arrays;
//
//import heronarts.lx.LX;
//import heronarts.lx.color.LXColor;
//
//
//import heronarts.lx.LXPattern;
//import heronarts.lx.audio.LXAudioBuffer;
//import heronarts.lx.parameter.CompoundParameter;
//import heronarts.lx.modulator.*;
//import heronarts.lx.modulator.SawLFO;
//import heronarts.lx.modulator.SinLFO;
//
//
//public class SwarmFly extends LXPattern {
//
//    private static final int NUM_GROUPS = 5;
//
//    public final CompoundParameter speed = (CompoundParameter)
//        new CompoundParameter("Speed", 2000, 10000, 500)
//            .setDescription("Speed of swarm motion")
//            .setExponent(.25);
//
//    public final CompoundParameter base =
//        new CompoundParameter("Base", 10, 60, 1)
//            .setDescription("Base size of swarm");
//
//    public final CompoundParameter floor =
//        new CompoundParameter("Floor", 20, 0, 100)
//            .setDescription("Base level of swarm brightness");
//
//    public final LXModulator[] pos = new LXModulator[NUM_GROUPS];
//
//    public final LXModulator swarmX = startModulator(new SinLFO(
//        startModulator(new SinLFO(0, .2, startModulator(new SinLFO(3000, 9000, 17000).randomBasis()))),
//        startModulator(new SinLFO(.8, 1, startModulator(new SinLFO(4000, 7000, 15000).randomBasis()))),
//        startModulator(new SinLFO(9000, 17000, 33000).randomBasis())
//    ).randomBasis());
//
//    public final LXModulator swarmY = startModulator(new SinLFO(
//        startModulator(new SinLFO(0, .2, startModulator(new SinLFO(3000, 9000, 19000).randomBasis()))),
//        startModulator(new SinLFO(.8, 1, startModulator(new SinLFO(4000, 7000, 13000).randomBasis()))),
//        startModulator(new SinLFO(9000, 17000, 33000).randomBasis())
//    ).randomBasis());
//
//    public final LXModulator swarmZ = startModulator(new SinLFO(
//        startModulator(new SinLFO(0, .2, startModulator(new SinLFO(3000, 9000, 19000).randomBasis()))),
//        startModulator(new SinLFO(.8, 1, startModulator(new SinLFO(4000, 7000, 13000).randomBasis()))),
//        startModulator(new SinLFO(9000, 17000, 33000).randomBasis())
//    ).randomBasis());
//
//    public PatternSwarm(LX lx) {
//        super(lx);
//        addParameter("speed", this.speed);
//        addParameter("base", this.base);
//        addParameter("floor", this.floor);
//        for (int i = 0; i < pos.length; ++i) {
//            final int ii = i;
//            float start = (i % 2 == 0) ? 0 : LeafAssemblage.NUM_LEAVES;
//            pos[i] = new SawLFO(start, LeafAssemblage.NUM_LEAVES - start, new FunctionalParameter() {
//                public double getValue() {
//                    return speed.getValue() + ii*500;
//                }
//            }).randomBasis();
//            startModulator(pos[i]);
//        }
//    }
//
//    public void run(double deltaMs) {
//        float base = this.base.getValuef();
//        float swarmX = this.swarmX.getValuef();
//        float swarmY = this.swarmY.getValuef();
//        float swarmZ = this.swarmZ.getValuef();
//        float floor = this.floor.getValuef();
//
//        int i = 0;
//        for (LeafAssemblage assemblage : tree.assemblages) {
//            float pos = this.pos[i++ % NUM_GROUPS].getValuef();
//            for (Leaf leaf : assemblage.leaves) {
//                float falloff = min(100, base + 40 * dist(leaf.point.xn, leaf.point.yn, leaf.point.zn, swarmX, swarmY, swarmZ));
//                float b = max(floor, 100 - falloff * LXUtils.wrapdistf(leaf.orientation.index, pos, LeafAssemblage.LEAVES.length));
//                setColor(leaf, LXColor.gray(b));
//            }
//        }
//    }
//}
