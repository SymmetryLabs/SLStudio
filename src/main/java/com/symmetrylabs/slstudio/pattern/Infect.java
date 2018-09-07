package com.symmetrylabs.slstudio.pattern;

import com.symmetrylabs.color.Ops16;
import com.symmetrylabs.color.Spaces;
import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;
import com.symmetrylabs.slstudio.model.StripsTopology.Bundle;
import com.symmetrylabs.slstudio.model.StripsTopology.Junction;
import com.symmetrylabs.slstudio.model.StripsTopology.Sign;
import com.symmetrylabs.slstudio.pattern.base.MidiPolyphonicExpressionPattern;
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
import heronarts.lx.color.LXColor;
import heronarts.lx.midi.MidiNote;
import heronarts.lx.midi.MidiNoteOff;
import heronarts.lx.midi.MidiNoteOn;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.transform.LXVector;

public class Infect extends MidiPolyphonicExpressionPattern<StripsModel<? extends Strip>> {
    List<Bundle> bundles;
    List<Junction> junctions;
    List<Infection> infections;
    Map<Integer, Infection> infectionsByKey;

    private CompoundParameter hueParam = new CompoundParameter("Hue", 0, -1, 1).setDescription("Hue adjustment");
    private CompoundParameter speedParam = new CompoundParameter("Speed", 128, 0, 1000).setDescription("Infection growth speed (strip lengths per minute)");
    private BooleanParameter grayParam = new BooleanParameter("Gray", false).setDescription("Grayscale output");

    private DiscreteParameter armsParam = new DiscreteParameter("Arms", 2, 1, 6).setDescription("Initial branch arms from infection origin");
    private CompoundParameter branchParam = new CompoundParameter("Branch", 1.2, 1, 6).setDescription("Branching factor from subsequent junctions");
    private BooleanParameter triggerParam = new BooleanParameter("Trigger", false).setDescription("Trigger a new infection").setMode(BooleanParameter.Mode.MOMENTARY);

    private DiscreteParameter noteLoParam = new DiscreteParameter("NoteLo", 36, 0, 127).setDescription("Lowest MIDI note of keyboard range");
    private DiscreteParameter noteHiParam = new DiscreteParameter("NoteHi", 72, 0, 127).setDescription("Highest MIDI note of keyboard range");

    protected Random random = new Random();

    public Infect(LX lx) {
        super(lx);
        bundles = model.getTopology().bundles;
        junctions = model.getTopology().junctions;
        infections = new ArrayList<>();
        infectionsByKey = new HashMap<>();

        addParameter(hueParam);
        addParameter(speedParam);
        addParameter(grayParam);

        addParameter(armsParam);
        addParameter(branchParam);
        addParameter(triggerParam);

        addParameter(noteLoParam);
        addParameter(noteHiParam);
    }

    @Override
    public void onParameterChanged(LXParameter p) {
        if (p instanceof BooleanParameter) {
            BooleanParameter param = (BooleanParameter) p;
            if (param == triggerParam) {
                if (param.getValueb()) startInfection(0, model.xMin, model.xMax);
                else stopInfection(0);
            }
        }
    }

    @Override
    public void noteOn(int pitch, double velocity) {
        int lo = noteLoParam.getValuei();
        int hi = noteHiParam.getValuei();
        if (pitch >= lo && pitch < hi) {
            float xMin = model.xMin + model.xRange * (pitch - lo) / (hi - lo);
            float xMax = model.xMin + model.xRange * (pitch + 1 - lo) / (hi - lo);
            startInfection(pitch, xMin, xMax);
        }
    }

    @Override
    public void noteOff(int pitch) {
        stopInfection(pitch);
    }

    @Override
    public void run(double deltaMs, PolyBuffer.Space preferredSpace) {
        double deltaSec = deltaMs / 1000.0;
        advanceInfections(deltaSec);
        renderInfections();
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

    protected void startInfection(int key, float xMin, float xMax) {
        Infection inf = new Infection(
              selectOrigin(xMin, xMax), armsParam.getValue(), branchParam.getValue(),
              hueParam.getValue(), grayParam.getValueb());
        infections.add(inf);
        infectionsByKey.put(key, inf);
    }

    protected void stopInfection(int key) {
        Infection inf = infectionsByKey.get(key);
        if (inf != null) inf.beginExpiring();
    }

    protected Junction selectOrigin(float xMin, float xMax) {
        Junction origin = null;
        Junction closest = null;
        float cx = (xMin + xMax) / 2;
        int count = 0;
        do {
            origin = junctions.get(random.nextInt(junctions.size()));
            if (origin.loc.x >= xMin && origin.loc.x < xMax) return origin;
            if (closest == null) closest = origin;
            else if (Math.abs(origin.loc.x - cx) < Math.abs(closest.loc.x - cx)) closest = origin;
        } while (++count < 100);
        return closest;
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
        public double hue = 0;
        public boolean gray = false;

        public Infection(Junction origin, double initialBranchFactor, double branchFactor, double hue, boolean gray) {
            growingSegments = startSegments(origin, initialBranchFactor);
            this.branchFactor = branchFactor;
            this.hue = hue;
            this.gray = gray;
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
                int a = v;
                if (!gray && value > 0) {
                    long c = Spaces.rgb8ToRgb16(LXColor.hsb(hue * 360, 100, 100));
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
