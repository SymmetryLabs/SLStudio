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
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;

public class WhiteCrawl extends SLPattern<StripsModel<? extends Strip>> {
    public static final String GROUP_NAME = SummerStageShow.SHOW_NAME;

    List<StripsTopology.Bundle> bundles;
    List<Junction> junctions;
    List<Chain> chains = new ArrayList<>();
    boolean running = false;

    private BooleanParameter startParam = new BooleanParameter("Start", false).setMode(BooleanParameter.Mode.MOMENTARY);
    private BooleanParameter stopParam = new BooleanParameter("Stop", false).setMode(BooleanParameter.Mode.MOMENTARY);
    private CompoundParameter durationParam = new CompoundParameter("Duration", 3, 0, 5);
    private CompoundParameter radiusParam = new CompoundParameter("Radius", 2.5, 0, 6);

    private CompoundParameter yMinParam = new CompoundParameter("YMin", model.yMin + model.yRange/3f, model.yMin, model.yMax);
    private CompoundParameter yMaxParam = new CompoundParameter("YMax", model.yMin + model.yRange * 2/3f, model.yMin, model.yMax);
    private CompoundParameter densityParam = new CompoundParameter("Density", 0.4, 0, 1);
    private DiscreteParameter minLenParam = new DiscreteParameter("MinLen", 4, 1, 8);

    private Random random = new Random();

    public WhiteCrawl(LX lx) {
        super(lx);
        bundles = model.getTopology().bundles;
        junctions = model.getTopology().junctions;

        addParameter(startParam);
        addParameter(stopParam);
        addParameter(durationParam);
        addParameter(radiusParam);
        addParameter(yMinParam);
        addParameter(yMaxParam);
        addParameter(densityParam);
        addParameter(minLenParam);
    }

    @Override
    public void onParameterChanged(LXParameter p) {
        if (p instanceof BooleanParameter) {
            BooleanParameter param = (BooleanParameter) p;
            if (param.isOn()) {
                if (param == startParam && !running) {
                    start();
                    running = true;
                }
                if (param == stopParam && running) {
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
            if (c.count >= minLenParam.getValuei()) {
                validCount++;
                if (random.nextDouble() < densityParam.getValue()) {
                    chains.add(c);
                }
            }
        }
        if (chains.size() == 0 && validCount > 0) {
            start();
        }
    }

    public void stop() {
        for (Chain chain : chains) {
            chain.fadeDown();
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
        };
    }

    class Chain {
        public List<Bundle> bundles = new ArrayList<>();
        public double[] fadeUpStarts;
        public double[] fadeDownStarts;
        public double timeSec = 1;
        public double xMin = model.xMax;
        public double xMax = model.xMin;
        public double xPos;
        public double stripLength;
        public int dir = 1;
        public int count = 0;
        public double chainValue = 0;
        public boolean fadingDown = false;

        public Chain(Junction j) {
            while (j != null) {
                Bundle bundle = j.get(Dir.X, Sign.POS);
                if (bundle == null) break;
                bundles.add(bundle);
                xMin = Math.min(xMin, bundle.minProjection());
                xMax = Math.max(xMax, bundle.maxProjection());
                j = bundle.get(Sign.POS);
            }
            count = bundles.size();
            if (count > 0) {
                fadeUpStarts = new double[count];
                fadeDownStarts = new double[count];
                xPos = xMin + random.nextDouble() * (xMax - xMin);
                dir = random.nextBoolean() ? 1 : -1;
                stripLength = (xMax - xMin) / count;
            }
            chainValue = 0;
        }

        public void run(double deltaSec) {
            timeSec += deltaSec;
            double duration = durationParam.getValue();
            xPos += dir * stripLength * deltaSec / duration;
            if (xPos > xMax) dir = -1;
            if (xPos < xMin) dir = 1;

            chainValue += (fadingDown ? -1 : 1) * (deltaSec / duration);
            if (chainValue < 0) chainValue = 0;
            if (chainValue > 1) chainValue = 1;

            long[] colors = (long[]) getArray(PolyBuffer.Space.RGB16);

            double radius = radiusParam.getValue() * stripLength;
            for (Bundle bundle : bundles) {
                double v = 1 - Math.abs(xPos - bundle.projection()) / radius;
                v *= chainValue;
                if (v < 0) v = 0;
                if (v > 1) v = 1;
                double lum = Spaces.cie_lightness_to_luminance(v);
                for (int s : bundle.strips) {
                    for (LXPoint p : model.getStripByIndex(s).points) {
                        colors[p.index] = Ops16.gray(lum);
                    }
                }
            }

            markModified(PolyBuffer.Space.RGB16);
        }

        public void fadeDown() {
            fadingDown = true;
        }
    }
}
