package com.symmetrylabs.shows.firefly;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.color.LXColor;
import heronarts.lx.color.ColorParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.EnumParameter;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import art.lookingup.KaledoscopeModel;
import art.lookingup.LUButterfly;
import art.lookingup.LUFlower;
import art.lookingup.AnchorTree;

public class PollinatePattern extends SLPattern<KaledoscopeModel> {
    public static final String GROUP_NAME = FireflyShow.SHOW_NAME;

    public final ColorParameter colorParam;
    public final BooleanParameter usePaletteParam;
    public final CompoundParameter bloomSpeedParam, bloomDelayParam,
           bloomFadeParam, transferSpeedParam, transferDelayParam,
           tailLengthParam, tailOpacityParam, tailDecayParam;
    public final BooleanParameter resetParam;

    private double t;
    private List<BaseAnim> animations;
    private Map<AnchorTree, BloomAnim> bloomAnimByTree = new HashMap<>();
    private Map<AnchorTree, List<TransferAnim>> transferAnimsByTree = new HashMap<>();

    public PollinatePattern(LX lx) {
        super(lx);

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

        addParameter(bloomSpeedParam = new CompoundParameter("BloomSpeed", 1, 0.001, 10));
        addParameter(bloomDelayParam = new CompoundParameter("BloomDelay", 0, 0, 4));
        addParameter(bloomFadeParam = new CompoundParameter("BloomFade", 0.5, 0, 4));
        addParameter(transferSpeedParam = new CompoundParameter("TransferSpeed", 20, 0.001, 100));
        addParameter(transferDelayParam = new CompoundParameter("TransferDelay", 0.5, 0, 4));
        addParameter(tailLengthParam = new CompoundParameter("TailLength", 5, 0, 20));
        addParameter(tailOpacityParam = new CompoundParameter("TailOpacity", 0.25, 0, 1));
        addParameter(tailDecayParam = new CompoundParameter("TailDecay", 0.3, 0, 1));
        addParameter(resetParam = new BooleanParameter("Reset").setMode(BooleanParameter.Mode.MOMENTARY));
        resetParam.addListener((p) -> reset());
    }

    private synchronized void reset() {
        t = 0;
        animations.clear();
        animations.add(new ResetAnim());
    }

    private interface BaseAnim {
        void run(double deltaMs);
        boolean isFinished();

        default void afterFinished() { }
    }

    private class ResetAnim implements BaseAnim {
        private final double startMs, endMs;

        private boolean finished = false;

        public ResetAnim() {
            this.startMs = t;
            this.endMs = t + 1000;
        }

        @Override
        public boolean isFinished() {
            return finished;
        }

        @Override
        public void run(double deltaMs) {
            if (isFinished())
                return;

            if (t >= endMs) {
                clear();
                finished = true;
                return;
            }

            double x = (t - startMs) / (endMs - startMs);

            for (int i = 0; i < colors.length; ++i) {
                setColor(i, LXColor.scaleBrightness(colors[i], 0.2f));
            }
        }
    }

    private class BloomAnim implements BaseAnim {
        private final AnchorTree tree;
        private final KaledoscopeModel.Cable cable;
        private final boolean bloomDown;
        private final double startMs;

        private List<LUFlower> flowers;
        private float minY, maxY;
        private double bloomHeight = 0;
        private boolean reblooming = false;
        private boolean finished = false;
        private boolean triggered = false;

        public BloomAnim(AnchorTree tree, KaledoscopeModel.Cable cable, boolean bloomDown) {
            this.tree = tree;
            this.cable = cable;
            this.bloomDown = bloomDown;
            this.startMs = t;

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
        }

        @Override
        public boolean isFinished() {
            return finished;
        }

        public void rebloom() {
            //reblooming = true;
        }

        private double getEndMs() {
            return startMs + flowers.size() * 1000 / bloomSpeedParam.getValue();
        }

        private double getTriggerMs() {
            double start = bloomDown ? startMs : getEndMs();
            double delay = bloomDown ? 1000 * transferDelayParam.getValue() : 0;
            return start + delay;
        }

        @Override
        public void run(double deltaMs) {
            if (isFinished())
                return;

            //System.out.println(minY + "\t" + maxY + "\t" + bloomHeight);
            double taperHeight = 12 * bloomSpeedParam.getValue() * bloomFadeParam.getValue();
            double taperMinY = bloomHeight - taperHeight;
            double taperMaxY = bloomHeight;

            for (LUFlower flower : flowers) {
                float y = bloomDown ? maxY - flower.y : flower.y - minY;

                float s = 0;
                if (y < taperMinY) {
                    s = 1;
                }
                else if (y >= taperMinY && y <= taperMaxY) {
                    s = 1f - (float)((y - taperMinY) / taperHeight);
                }

                for (LXPoint p : flower.petals) {
                    setColor(p.index, LXColor.scaleBrightness(colorParam.getColor(), s));
                }

                setColor(flower.center.index, LXColor.scaleBrightness(LXColor.WHITE, s));
            }

            bloomHeight += 12 * bloomSpeedParam.getValue() * deltaMs / 1000;

            if (taperMinY > maxY - minY) {
                finished = true;
            }

            if (!triggered && (bloomDown ? t > 1000 * transferDelayParam.getValue() : finished)) {
                triggered = true;
                triggerNext();
            }
        }

        private void triggerNext() {
            boolean cableIn = cable == null || cable.endTree == tree;
            transferAnimsByTree.putIfAbsent(tree, new ArrayList<TransferAnim>());
            List<TransferAnim> existingTransferAnims = transferAnimsByTree.get(tree);

            boolean hasOutCables = false;
            for (int i = 0; i < tree.outCables.length; ++i) {
                if (tree.outCables[i] != null) {
                    hasOutCables = true;
                    break;
                }
            }
            boolean hasInCables = false;
            for (int i = 0; i < tree.inCables.length; ++i) {
                if (tree.inCables[i] != null) {
                    hasInCables = true;
                    break;
                }
            }
            if (!hasOutCables) {
                cableIn = true;
            }
            KaledoscopeModel.Cable[] newCables = cableIn ? tree.outCables : tree.inCables;

            for (KaledoscopeModel.Cable c : newCables) {
                if (c == null)
                    continue;

                TransferAnim existing = null;
                for (TransferAnim x : existingTransferAnims) {
                    if (c == x.cable) {
                        existing = x;
                        break;
                    }
                }

                if (existing != null && existing.endTree == tree) {
                    existing.reverse();
                }
                else {
                    TransferAnim transferAnim = new TransferAnim(c, !cableIn);
                    existingTransferAnims.add(transferAnim);
                    animations.add(transferAnim);
                }
            }
        }

        @Override
        public void afterFinished() {
            bloomAnimByTree.remove(tree, this);
        }
    }

    private class TransferAnim implements BaseAnim {
        private final KaledoscopeModel.Cable cable;
        private final boolean startAtEnd;
        private final double startMs;

        private AnchorTree startTree, endTree;
        private boolean reversing = false;
        private boolean finished = false;
        private boolean triggered = false;

        public TransferAnim(KaledoscopeModel.Cable cable, boolean startAtEnd) {
            this.cable = cable;
            this.startAtEnd = startAtEnd;
            this.startMs = t;

            startTree = startAtEnd ? cable.endTree : cable.startTree;
            endTree = startAtEnd ? cable.startTree : cable.endTree;
        }

        public double getTriggerMs() {
            return startMs + 1000 * (bloomDelayParam.getValue() + cable.butterflies.size() / transferSpeedParam.getValue());
        }

        @Override
        public boolean isFinished() {
            return finished;
        }

        public void reverse() {
            reversing = true;
        }

        @Override
        public void run(double deltaMs) {
            double endMs = getTriggerMs();

            if (t >= endMs && !triggered) {
                triggered = true;
                triggerNext();
            }

            for (int i = 0; i < cable.butterflies.size(); ++i) {
                LUButterfly butterfly = cable.butterflies.get(startAtEnd ? cable.butterflies.size() - i - 1 : i);
                float s = i / (double)cable.butterflies.size() < (t - startMs) / (endMs - startMs) ? 1 : 0;
                for (LXPoint p : butterfly.allPoints) {
                    setColor(p.index, LXColor.scaleBrightness(colorParam.getColor(), s));
                }
            }
        }

        private void triggerNext() {
            BloomAnim bloomAnim = bloomAnimByTree.get(endTree);
            if (bloomAnim != null && !bloomAnim.isFinished()) {
                bloomAnim.rebloom();
            }
            else {
                bloomAnim = new BloomAnim(endTree, cable, true);
                bloomAnimByTree.put(endTree, bloomAnim);
                animations.add(bloomAnim);
            }
        }

        @Override
        public void afterFinished() {
            List<TransferAnim> oldTransfers = transferAnimsByTree.get(endTree);
            if (oldTransfers != null) {
                oldTransfers.remove(this);
            }
        }
    }

    private void constructAnimations() {
        if (animations.isEmpty()) {
            AnchorTree firstTree = KaledoscopeModel.anchorTrees.get(0);
            BloomAnim bloomAnim = new BloomAnim(firstTree, null, false);
            bloomAnimByTree.put(firstTree, bloomAnim);
            animations.add(bloomAnim);
        }

        // prune finished animations
        for (int i = 0; i < animations.size(); ++i) {
            BaseAnim anim = animations.get(i);
            if (anim.isFinished()) {
                anim.afterFinished();
                animations.remove(i);
                --i;
            }
        }
    }

    @Override
    protected synchronized void run(double deltaMs) {
        t += deltaMs;

        constructAnimations();

        // run animations
        for (int i = 0; i < animations.size(); ++i) {
            animations.get(i).run(deltaMs);
        }
    }
}
