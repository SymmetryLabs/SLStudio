package com.symmetrylabs.shows.summerstage;

import com.symmetrylabs.color.Ops16;
import com.symmetrylabs.color.Spaces;
import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;
import com.symmetrylabs.slstudio.model.StripsTopology;
import com.symmetrylabs.slstudio.model.StripsTopology.Bundle;
import com.symmetrylabs.slstudio.model.StripsTopology.Dir;
import com.symmetrylabs.slstudio.model.StripsTopology.Junction;
import com.symmetrylabs.slstudio.model.StripsTopology.Sign;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;

public class WhiteCrawl extends SLPattern<StripsModel<? extends Strip>> {
    public static final String GROUP_NAME = SummerStageShow.SHOW_NAME;

    List<StripsTopology.Bundle> bundles;
    List<Junction> junctions;
    List<Chain> chains = new ArrayList<>();
    boolean running = false;

    private BooleanParameter nextParam = new BooleanParameter("Next", false).setMode(BooleanParameter.Mode.MOMENTARY);
    private BooleanParameter stopParam = new BooleanParameter("Stop", false).setMode(BooleanParameter.Mode.MOMENTARY);
    private CompoundParameter durationParam = new CompoundParameter("Duration", 0.5, 0, 10);
    private CompoundParameter densityParam = new CompoundParameter("Density", 0.5, 0, 1);
    private CompoundParameter yMinParam = new CompoundParameter("YMin", model.yMin, model.yMin, model.yMax);
    private CompoundParameter yMaxParam = new CompoundParameter("YMax", model.yMax, model.yMin, model.yMax);

    private Random random = new Random();

    public WhiteCrawl(LX lx) {
        super(lx);
        bundles = model.getTopology().bundles;
        junctions = model.getTopology().junctions;

        addParameter(nextParam);
        addParameter(stopParam);
        addParameter(durationParam);
        addParameter(densityParam);
        addParameter(yMinParam);
        addParameter(yMaxParam);
    }

    @Override
    public void onParameterChanged(LXParameter p) {
        if (p instanceof BooleanParameter) {
            BooleanParameter param = (BooleanParameter) p;
            if (param.isOn()) {
                if (param == nextParam) {
                    if (!running) {
                        start();
                        running = true;
                    } else {
                        next();
                    }
                }
                if (param == stopParam) {
                    stop();
                    running = false;
                }
            }
        }
    }

    public void start() {
        int validCount = 0;
        chains = new ArrayList<Chain>();
        for (Junction j : getAllLeftmost(yMinParam.getValuef(), yMaxParam.getValuef())) {
            Chain c = new Chain(j);
            if (c.count > 0) {
                validCount++;
                if (random.nextDouble() < densityParam.getValue()) {
                    chains.add(c);
                    c.start();
                }
            }
        }
        if (chains.size() == 0 && validCount > 0) {
            start();
        }
    }

    public void next() {
        for (Chain chain : chains) {
            chain.advance();
        }
    }

    public void stop() {
        for (Chain chain : chains) {
            chain.fadeDownAll();
        }
    }

    public Set<Junction> getAllLeftmost(float yMin, float yMax) {
        Set<Junction> result = new HashSet<>();
        for (Junction j : junctions) {
            if (j.loc.y >= yMin && j.loc.y <= yMax) {
                result.add(leftmost(j));
            }
        }
        return result;
    }

    public Junction leftmost(Junction j) {
        while (true) {
            Bundle bundle = j.get(Dir.X, Sign.NEG);
            if (bundle == null) break;
            Junction nj = bundle.get(Sign.NEG);
            if (nj.loc.x > j.loc.x) break;
            j = nj;
        }
        return j;
    }

    public void run(double deltaMs, PolyBuffer.Space preferredSpace) {
        double deltaSec = deltaMs / 1000;
        for (Chain chain : chains) {
            chain.run(deltaSec);
        }
    }

    class Chain {
        public List<Bundle> bundles = new ArrayList<>();
        public double[] fadeUpStarts;
        public double[] fadeDownStarts;
        public double timeSec = 1;
        public int count = 0;
        public int index = 0;
        public int di = 1;

        public Chain(Junction j) {
            while (j != null) {
                Bundle bundle = j.get(Dir.X, Sign.POS);
                if (bundle == null) break;
                bundles.add(bundle);
                j = bundle.get(Sign.POS);
            }
            count = bundles.size();
            if (count > 0) {
                fadeUpStarts = new double[count];
                fadeDownStarts = new double[count];
                index = random.nextInt(count);
            }
        }

        public void run(double deltaSec) {
            timeSec += deltaSec;
            double duration = durationParam.getValue();

            long[] colors = (long[]) getArray(PolyBuffer.Space.RGB16);

            for (int i = 0; i < bundles.size(); i++) {
                double v = 0;
                if (fadeDownStarts[i] != 0) {
                    double elapsed = timeSec - fadeDownStarts[i];
                    v = 1.0 - elapsed / duration;
                    if (elapsed > duration) {
                        fadeDownStarts[i] = 0;
                    }
                } else if (fadeUpStarts[i] != 0) {
                    double elapsed = timeSec - fadeUpStarts[i];
                    v = elapsed / duration;
                }
                if (v < 0) v = 0;
                if (v > 1) v = 1;
                double lum = Spaces.cie_lightness_to_luminance(v);
                for (int s : bundles.get(i).strips) {
                    for (LXPoint p : model.getStripByIndex(s).points) {
                        colors[p.index] = Ops16.gray(lum);
                    }
                }
            }

            markModified(PolyBuffer.Space.RGB16);
        }

        public void fadeUp(int i) {
            fadeUpStarts[i] = timeSec;
            fadeDownStarts[i] = 0;
        }

        public void fadeDown(int i) {
            fadeDownStarts[i] = timeSec;
            fadeUpStarts[i] = 0;
        }

        public void fadeDownAll() {
            for (int i = 0; i < count; i++) {
                if (fadeUpStarts[i] > 0) fadeDown(i);
            }
        }

        public void start() {
            fadeUp(index);
        }

        public void advance() {
            int newIndex = index + di;
            if (newIndex < 0 || newIndex >= count) {
                di = -di;
                newIndex = index + di;
            }
            if (newIndex >= 0 && newIndex < count) {
                fadeDown(index);
                fadeUp(newIndex);
                index = newIndex;
            }
        }
    }
}
