package com.symmetrylabs.shows.summerstage;

import com.symmetrylabs.color.Ops16;
import com.symmetrylabs.color.Spaces;
import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;
import com.symmetrylabs.slstudio.model.StripsTopology.Bundle;
import com.symmetrylabs.slstudio.model.StripsTopology.Junction;
import com.symmetrylabs.slstudio.model.StripsTopology.Sign;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;

public class Infect<T extends Strip> extends SLPattern<StripsModel<T>> {
    List<Bundle> bundles;
    List<Junction> junctions;
    List<Infection> infections;
    Map<Integer, Infection> infectionsByKey;

    private DiscreteParameter armsParam = new DiscreteParameter("Arms", 2, 1, 6).setDescription("Initial branch arms from infection origin");
    private CompoundParameter branchParam = new CompoundParameter("Branch", 1, 1, 6).setDescription("Branching factor from subsequent junctions");
    private CompoundParameter speedParam = new CompoundParameter("Speed", 128, 0, 1000).setDescription("Infection growth speed (strip lengths per minute)");
    private BooleanParameter triggerParam = new BooleanParameter("Trigger", false).setDescription("Trigger a new infection").setMode(BooleanParameter.Mode.MOMENTARY);
    private BooleanParameter gPaletteParam = new BooleanParameter("GPalette", false).setDescription("Use the global palette");
    private BooleanParameter alphaParam = new BooleanParameter("Alpha", false).setDescription("Set alpha channel");
    protected Random random = new Random();

    public Infect(LX lx) {
        super(lx);
        bundles = model.getTopology().bundles;
        junctions = model.getTopology().junctions;
        infections = new ArrayList<>();
        infectionsByKey = new HashMap<>();

        addParameter(armsParam);
        addParameter(branchParam);
        addParameter(speedParam);
        addParameter(triggerParam);
        addParameter(gPaletteParam);
        addParameter(alphaParam);
    }

    @Override
    public void onParameterChanged(LXParameter p) {
        if (p instanceof BooleanParameter) {
            BooleanParameter param = (BooleanParameter) p;
            if (param == triggerParam) {
                if (param.getValueb()) startInfection(0);
                else stopInfection(0);
            }
        }
    }

    @Override
    public void run(double deltaMs, PolyBuffer.Space preferredSpace) {
        double deltaSec = deltaMs / 1000.0;
        advanceInfections(deltaSec);
        renderInfections();
    }

    @Override
    public String getCaption() {
        int segmentCount = 0;
        for (Infection inf : infections) {
            segmentCount += inf.growingSegments.size();
        }
        return String.format(Locale.US, "Infections:%3d / Growing segments:%3d", infections.size(), segmentCount);
    }

    protected void advanceInfections(double deltaSec) {
        List<Infection> continuingInfections = new ArrayList<>();
        for (Infection inf : infections) {
            inf.advance(deltaSec);
            if (!inf.isExpired()) {
                continuingInfections.add(inf);
            }
        }
        infections = continuingInfections;

        List<Integer> expiredKeys = new ArrayList<>();
        for (Integer key : infectionsByKey.keySet()) {
            if (infectionsByKey.get(key).isExpired()) {
                expiredKeys.add(key);
            }
        }
        for (Integer key : expiredKeys) {
            infectionsByKey.remove(key);
        }
    }

    protected void renderInfections() {
        long[] colors = (long[]) getArray(PolyBuffer.Space.RGB16);
        Arrays.fill(colors, 0L);
        for (Infection inf : infections) {
            inf.renderPoints(colors);
        }
        markModified(PolyBuffer.Space.RGB16);
    }

    protected void startInfection(int key) {
        Infection inf = new Infection(selectOrigin(), armsParam.getValue(), branchParam.getValue());
        infections.add(inf);
        infectionsByKey.put(key, inf);
    }

    protected void stopInfection(int key) {
        Infection inf = infectionsByKey.get(key);
        if (inf != null) inf.beginExpiring();
    }

    protected Junction selectOrigin() {
        return junctions.get(random.nextInt(junctions.size()));
    }

    class Infection {
        public Map<Integer, Double> pointAges = new HashMap<>();
        public Map<Bundle, Integer> bundleTraversals = new HashMap<>();
        public List<Segment> growingSegments;
        public double branchFactor;
        public Random random = new Random();
        public double segmentAge = 0;
        public boolean expiring = false;;
        public double expireStartAge = Double.MAX_VALUE;
        public double expireElapsed = 0;

        public Infection(Junction origin, double initialBranchFactor, double branchFactor) {
            growingSegments = startSegments(origin, initialBranchFactor);
            this.branchFactor = branchFactor;
        }

        public void beginExpiring() {
            expiring = true;
            expireStartAge = segmentAge;
            expireElapsed = 0;
        }

        public boolean isExpired() {
            return expiring && expireElapsed > expireStartAge;
        }

        /** The "progress" argument is measured in strip lengths. */
        public void advance(double deltaSec) {
            segmentAge += deltaSec;
            for (Integer i : pointAges.keySet()) {
                pointAges.put(i, pointAges.get(i) + deltaSec);
            }
            expireElapsed += deltaSec;

            if (expiring) return;

            double progress = speedParam.getValue() * deltaSec/60.0;
            List<Segment> continuingSegments = new ArrayList<>();
            for (Segment s : growingSegments) {
                double remain = 1 - s.progress;
                if (progress > remain) {
                    advanceSegment(s, remain);
                    List<Segment> nextSegments = startSegments(s.getEnd(), branchFactor);
                    for (Segment ns : nextSegments) {
                        advanceSegment(ns, progress - remain);
                        continuingSegments.add(ns);
                    }
                } else {
                    advanceSegment(s, progress);
                    continuingSegments.add(s);
                }
                growingSegments = continuingSegments;
            }
        }

        /** Selects up to `branchFactor` segments radiating from the given junction. */
        public List<Segment> startSegments(Junction origin, double branchFactor) {
            double branchFloor = Math.floor(branchFactor);
            double branchFrac = branchFactor - branchFloor;
            int targetCount = (int) (branchFloor + (random.nextDouble() < branchFrac ? 1 : 0));

            // Keep the branching segments from getting out of hand.
            int maxTraversals = Integer.MAX_VALUE;
            int segmentCount = growingSegments != null ? growingSegments.size() : 0;
            if (segmentCount > 10) maxTraversals = 6;
            if (segmentCount > 40) maxTraversals = 3;
            if (segmentCount > 100) maxTraversals = 2;

            List<Bundle> selected = new ArrayList<>();
            List<Bundle> available = new ArrayList<>();
            for (Bundle bundle : origin.getBundles()) {
                if (bundleTraversals.getOrDefault(bundle, 0) < maxTraversals) {
                    available.add(bundle);
                }
            }

            while (!available.isEmpty() && selected.size() < targetCount) {
                Bundle pick = available.get(random.nextInt(available.size()));
                selected.add(pick);
                available.remove(pick);
            }

            List<Segment> result = new ArrayList<>();
            for (Bundle bundle : selected) {
                Sign sign = (bundle.get(Sign.NEG) == origin) ? Sign.POS : Sign.NEG;
                result.add(new Segment(bundle, sign));
                bundleTraversals.put(bundle, bundleTraversals.getOrDefault(bundle, 0) + 1);
            }
            return result;
        }

        /** Advances along a segment by a given amount, measured in strip lengths. */
        public void advanceSegment(Segment segment, double progress) {
            double oldProgress = segment.progress;
            double newProgress = Math.min(1, oldProgress + progress);
            segment.progress = newProgress;

            double xMin = segment.xMin;
            double xMax = segment.xMax;
            double yMin = segment.yMin;
            double yMax = segment.yMax;
            double zMin = segment.zMin;
            double zMax = segment.zMax;

            switch (segment.bundle.dir) {
                case X:
                    if (segment.sign == Sign.POS) {
                        xMin = xMin + (xMax - xMin) * oldProgress;
                        xMax = xMin + (xMax - xMin) * newProgress;
                    } else {
                        xMax = xMax + (xMin - xMax) * oldProgress;
                      xMin = xMax + (xMin - xMax) * newProgress;
                    }
                    break;
                case Y:
                    if (segment.sign == Sign.POS) {
                        yMin = yMin + (yMax - yMin) * oldProgress;
                        yMax = yMin + (yMax - yMin) * newProgress;
                    } else {
                        yMax = yMax + (yMin - yMax) * oldProgress;
                        yMin = yMax + (yMin - yMax) * newProgress;
                    }
                    break;
                case Z:
                    if (segment.sign == Sign.POS) {
                        zMin = zMin + (zMax - zMin) * oldProgress;
                        zMax = zMin + (zMax - zMin) * newProgress;
                    } else {
                        zMax = zMax + (zMin - zMax) * oldProgress;
                        zMin = zMax + (zMin - zMax) * newProgress;
                    }
                    break;
            }

            for (int si : segment.bundle.strips) {
                Strip strip = model.getStripByIndex(si);
                for (LXPoint point : strip.points) {
                    if (point.x >= xMin && point.x <= xMax &&
                          point.y >= yMin && point.y <= yMax &&
                          point.z >= zMin && point.z <= zMax) {
                        pointAges.put(point.index, 0.0);
                    }
                }
            }
        }

        public void renderPoints(long[] array) {
            boolean usePalette = gPaletteParam.getValueb();
            boolean setAlpha = alphaParam.getValueb();
            for (Integer index : pointAges.keySet()) {
                double age = pointAges.get(index);
                double value = 0;
                if (expiring) {
                    value = 1.0 - (age - expireElapsed)/(segmentAge - expireElapsed) - expireElapsed/expireStartAge;
                } else {
                    value = 1.0 - (age/segmentAge);
                }
                if (value < 0) value = 0;
                int v = (int) (value * Ops16.MAX + 0.5);
                int r = v;
                int g = v;
                int b = v;
                int a = setAlpha ? v : Ops16.MAX;
                if (usePalette && value > 0) {
                    long c = Spaces.rgb8ToRgb16(palette.getColor(model.points[index]));
                    r = Ops16.red(c);
                    g = Ops16.green(c);
                    b = Ops16.blue(c);
              }
                array[index] = Ops16.add(array[index], Ops16.rgba(r, g, b, a));
            }
        }

        class Segment {
            public final Bundle bundle;
            public final Sign sign;
            public final double length;
            public double progress = 0;
            public double xMin = Double.MAX_VALUE;
            public double xMax = Double.MIN_VALUE;
            public double yMin = Double.MAX_VALUE;
            public double yMax = Double.MIN_VALUE;
            public double zMin = Double.MAX_VALUE;
            public double zMax = Double.MIN_VALUE;

            public Segment(Bundle bundle, Sign sign) {
                this.bundle = bundle;
                this.sign = sign;
                this.length = getStart().loc.dist(getEnd().loc);

                for (int si : bundle.strips) {
                    Strip strip = model.getStripByIndex(si);
                    xMin = Math.min(xMin, strip.xMin);
                    xMax = Math.max(xMax, strip.xMax);
                    yMin = Math.min(yMin, strip.yMin);
                    yMax = Math.max(yMax, strip.yMax);
                    zMin = Math.min(zMin, strip.zMin);
                    zMax = Math.max(zMax, strip.zMax);
                }
            }

            public Junction getStart() {
                return bundle.get(sign.other());
            }

            public Junction getEnd() {
                return bundle.get(sign);
            }
        }
    }
}
