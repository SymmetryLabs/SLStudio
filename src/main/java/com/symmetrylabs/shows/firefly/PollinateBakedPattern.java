package com.symmetrylabs.shows.firefly;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.color.LXColor;
import heronarts.lx.color.ColorParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.EnumParameter;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import com.symmetrylabs.slstudio.pattern.Sparkle;
import art.lookingup.KaledoscopeModel;
import art.lookingup.LUButterfly;
import art.lookingup.LUFlower;
import art.lookingup.AnchorTree;

public class PollinateBakedPattern extends SLPattern<KaledoscopeModel> {
    public static final String GROUP_NAME = FireflyShow.SHOW_NAME;

    public static enum ColorMode { HUE, BRIGHTNESS };

    public final ColorParameter colorParam;
    public final BooleanParameter usePaletteParam;
    //public final EnumParameter<ColorMode> colorModeParam;
    //public final CompoundParameter sparkleDurationParam, sparkleDelayParam;
    public final CompoundParameter bloomSpeedParam, bloomDelayParam,
           transferSpeedParam, transferDelayParam,
           tailLengthParam, tailOpacityParam, tailDecayParam;
    public final BooleanParameter resetParam;

    private double t;
    private List<BaseAnim> animations;

    public PollinateBakedPattern(LX lx) {
        super(lx);

        // animation state
        t = 0;
        animations = new ArrayList<>();

        addParameter(colorParam = new ColorParameter("Color", LXColor.WHITE));
        addParameter(usePaletteParam = new BooleanParameter("UsePalette", false));
        colorParam.addListener(p -> {
            if (colorParam.getColor() != lx.palette.color.getColor()) {
                usePaletteParam.setValue(false);
            }
        });
        usePaletteParam.addListener(p -> {
            colorParam.setColor(lx.palette.color.getColor());
        });
        lx.palette.color.addListener(p -> {
            if (usePaletteParam.isOn()) {
                colorParam.setColor(lx.palette.color.getColor());
            }
        });

        //addParameter(sparkleDurationParam = new CompoundParameter("SparkleDuration", 1, 0, 30));
        //addParameter(sparkleDelayParam = new CompoundParameter("SparkleDelay", 1, 0, 4));
        addParameter(bloomSpeedParam = new CompoundParameter("BloomSpeed", 20, 0.001, 100));
        addParameter(bloomDelayParam = new CompoundParameter("BloomDelay", 0, 0, 4));
        addParameter(transferSpeedParam = new CompoundParameter("TransferSpeed", 20, 0.001, 100));
        addParameter(transferDelayParam = new CompoundParameter("TransferDelay", 0.5, 0, 4));
        addParameter(tailLengthParam = new CompoundParameter("TailLength", 5, 0, 20));
        addParameter(tailOpacityParam = new CompoundParameter("TailOpacity", 0.25, 0, 1));
        addParameter(tailDecayParam = new CompoundParameter("TailDecay", 0.3, 0, 1));
        addParameter(resetParam = new BooleanParameter("Reset").setMode(BooleanParameter.Mode.MOMENTARY));
        resetParam.addListener((p) -> reset());
    }

    private void reset() {
        t = 0;
        animations.clear();
    }

    private void constructAnimations() {
        double t = this.t;

        Set<AnchorTree> seenTrees = new HashSet<>();
        List<AnchorTree> treeQueue = new LinkedList<>();
        Set<KaledoscopeModel.Cable> seenCables = new HashSet<>();

        AnchorTree firstTree = KaledoscopeModel.anchorTrees.get(0);
        treeQueue.add(firstTree);
        seenTrees.add(firstTree);

        List<KaledoscopeModel.Cable> currentCables = new ArrayList<>();
        while (!treeQueue.isEmpty()) {
            AnchorTree tree = treeQueue.remove(0);
            boolean bloomDown = !seenCables.isEmpty();

            BloomAnim bloomAnim = new BloomAnim(t, tree, bloomDown);
            animations.add(bloomAnim);
            //animations.add(new SparkleAnim(t, tree));

            double lastEndMs = t;
            for (KaledoscopeModel.Cable cable : tree.outCables) {
                if (cable == null)
                    continue;

                if (seenCables.contains(cable))
                    continue;

                double transferStart = bloomDown ? t : bloomAnim.endMs;
                double transferDelay = bloomDown ? 1000 * transferDelayParam.getValue() : 0;
                TransferAnim transferAnim = new TransferAnim(transferStart + transferDelay, cable, false);
                animations.add(transferAnim);
                seenCables.add(cable);

                if (transferAnim.endMs > lastEndMs) {
                    lastEndMs = transferAnim.endMs;
                }

                if (!seenTrees.contains(cable.endTree)) {
                    treeQueue.add(cable.endTree);
                    seenTrees.add(cable.endTree);
                }
            }

            t = lastEndMs;
        }
    }

    private interface BaseAnim {
        boolean run(double deltaMs);
    }

    /*
    private class SparkleAnim implements BaseAnim {
        AnchorTree tree;
        double startMs, endMs;

        private List<LUFlower> flowers;
        private double[] sparkleTimeouts;
        private float sparkleRate = 1/5f;
        private float sparkleSecs = 3;

        public SparkleAnim(double startMs, AnchorTree tree) {
            flowers = new ArrayList<>();
            for (KaledoscopeModel.Run run : tree.flowerRuns) {
                flowers.addAll(run.flowers);
            }

            sparkleTimeouts = new double[flowers.size()];

            this.startMs = startMs + 1000 * sparkleDelayParam.getValue();
            this.endMs = this.startMs + 1000 * sparkleDurationParam.getValue();
            this.tree = tree;
        }

        // [0, 1] -> [0, 1]
        private float sparkleFunc(float x) {
            //return (float)(-Math.pow(2 * x - 1, 4) + 0.9 + 0.1 * Math.random());
            double a = -Math.pow(2 * x - 1, 22) + 1;
            double b = 0.9 + 0.1 * (Math.sin(7 * Math.PI * x) + 1) / 2;
            return (float)(a * b);
        }

        @Override
        public boolean run(double deltaMs) {
            if (t < startMs)
                return true;

            if (t > endMs)
                return false;

            for (int i = 0; i < flowers.size(); ++i) {
                if (sparkleTimeouts[i] <= t) {
                    if (Math.random() < sparkleRate * deltaMs / 1000) {
                        sparkleTimeouts[i] = t + sparkleSecs * 1000;
                    }
                    else {
                        setColor(flowers.get(i).center.index, LXColor.BLACK);
                        continue;
                    }
                }

                float sparkleStart = (float)sparkleTimeouts[i] - sparkleSecs;
                float b = 100 * sparkleFunc(((float)t - sparkleStart) / sparkleSecs);
                setColor(flowers.get(i).center.index, LXColor.hsb(0, 0, b));
            }

            return true;
        }
    }
    */

    private class BloomAnim implements BaseAnim {
        final double startMs, endMs;
        final AnchorTree tree;
        final boolean bloomDown;

        private List<LUFlower> flowers;
        private float minY, maxY;

        public BloomAnim(double startMs, AnchorTree tree, boolean bloomDown) {
            flowers = new ArrayList<>();
            minY = Float.MAX_VALUE;
            maxY = Float.MIN_VALUE;
            for (KaledoscopeModel.Run run : tree.flowerRuns) {
                flowers.addAll(run.flowers);

                for (LUFlower f : run.flowers) {
                    if (f.y < minY) {
                        minY = f.y;
                    }
                    if (f.y > maxY) {
                        maxY = f.y;
                    }
                }
            }
            Collections.reverse(flowers);


            this.startMs = startMs;
            this.endMs = startMs + 1000 * flowers.size() / bloomSpeedParam.getValue();
            this.tree = tree;
            this.bloomDown = bloomDown;
        }

        @Override
        public boolean run(double deltaMs) {
            if (t < startMs)
                return true;

            if (t > endMs)
                return false;

            float a = (float)((t - startMs) / (endMs - startMs));

            for (int i = 0; i < flowers.size(); ++i) {
                LUFlower flower = flowers.get(bloomDown ? flowers.size() - i - 1 : i);
                float r = i / (float)(flowers.size() - 1);
                float h = maxY == minY ? 0 : (flower.y - minY) / (maxY - minY);
                if (bloomDown) {
                    h = 1 - h;
                }
                float s = 0;
                if (r <= a) {
                    float d = a - r;
                    s = d;
                }
                //s += h * a * 0.5;
                if (s > 1) {
                    s = 1;
                }

                for (LXPoint p : flower.petals) {
                    setColor(p.index, LXColor.scaleBrightness(colorParam.getColor(), s));
                }

                setColor(flower.center.index, LXColor.scaleBrightness(LXColor.WHITE, s));
            }

            return true;
        }
    }

    private class TransferAnim implements BaseAnim {
        final double startMs, endMs;
        final KaledoscopeModel.Cable cable;
        final boolean startAtEnd;

        public TransferAnim(double startMs, KaledoscopeModel.Cable cable, boolean startAtEnd) {
            int length = cable.butterflies.size();

            this.startMs = startMs;
            this.endMs = startMs + 1000 * length / transferSpeedParam.getValue();
            this.cable = cable;
            this.startAtEnd = startAtEnd;
        }

        @Override
        public boolean run(double deltaMs) {
            if (t < startMs)
                return true;

            if (t > endMs)
                return false;

            for (int i = 0; i < cable.butterflies.size(); ++i) {
                LUButterfly butterfly = cable.butterflies.get(startAtEnd ? cable.butterflies.size() - i - 1 : i);
                float s = i / (double)cable.butterflies.size() < (t - startMs) / (endMs - startMs) ? 1 : 0;
                for (LXPoint p : butterfly.allPoints) {
                    setColor(p.index, LXColor.scaleBrightness(colorParam.getColor(), s));
                }
            }

            return true;
        }
    }

    @Override
    protected void run(double deltaMs) {
        t += deltaMs;

        if (animations.isEmpty()) {
            clear();
            constructAnimations();
        }

        for (int i = 0; i < animations.size(); ++i) {
            if (!animations.get(i).run(deltaMs)) {
                animations.remove(i);
                --i;
            }
        }
    }
}
