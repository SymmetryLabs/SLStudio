package com.symmetrylabs.slstudio.pattern;

import com.symmetrylabs.color.Ops16;
import com.symmetrylabs.color.Spaces;
import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;
import com.symmetrylabs.slstudio.model.StripsTopology.Bundle;
import com.symmetrylabs.slstudio.model.StripsTopology.Junction;
import com.symmetrylabs.slstudio.model.StripsTopology.Sign;
import com.symmetrylabs.slstudio.palettes.ColorPalette;
import com.symmetrylabs.slstudio.palettes.PaletteLibrary;
import com.symmetrylabs.slstudio.palettes.ZigzagPalette;
import com.symmetrylabs.slstudio.pattern.base.MidiPolyphonicExpressionPattern;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import heronarts.lx.Tempo;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.transform.LXVector;

public class Infect extends MidiPolyphonicExpressionPattern<StripsModel<? extends Strip>> implements Tempo.Listener {
    List<Bundle> bundles;
    List<Junction> junctions;
    List<Infection> infections;
    Map<Integer, Infection> infectionsByKey;

    private CompoundParameter hueParam = new CompoundParameter("Hue", 0, -1, 1).setDescription("Hue adjustment");
    private CompoundParameter hVarParam = new CompoundParameter("HVar", 1, 0, 4);  // hue variation
    private CompoundParameter speedParam = new CompoundParameter("Speed", 128, 0, 3000).setDescription("Infection growth speed (strip lengths per minute)");
    private BooleanParameter tempoParam = new BooleanParameter("Tempo", false).setDescription("Use the global tempo to trigger infection growth");

    private DiscreteParameter armsParam = new DiscreteParameter("Arms", 3, 1, 6).setDescription("Initial branch arms from infection origin");
    private CompoundParameter branchParam = new CompoundParameter("Branch", 1.2, 1, 6).setDescription("Branching factor from subsequent junctions");
    private DiscreteParameter spreadParam = new DiscreteParameter("Spread", 0, -1, 4);
    private DiscreteParameter maxLenParam = new DiscreteParameter("MaxLen", 24, 2, 500);
    private BooleanParameter triggerParam = new BooleanParameter("Trigger", false).setDescription("Trigger a new infection").setMode(BooleanParameter.Mode.MOMENTARY);

    private final PaletteLibrary paletteLibrary = PaletteLibrary.getInstance();
    DiscreteParameter palette = new DiscreteParameter("palette", paletteLibrary.getNames());
    CompoundParameter palStart = new CompoundParameter("palStart", 0, 0, 1);  // palette start point (fraction 0 - 1)
    CompoundParameter palStop = new CompoundParameter("palStop", 1, 0, 1);  // palette stop point (fraction 0 - 1)
    CompoundParameter palShift = new CompoundParameter("palShift", 0, 0, 1);  // shift in colour palette (fraction 0 - 1)
    CompoundParameter palBias = new CompoundParameter("palBias", 0, -6, 6);  // bias colour palette toward start or stop
    CompoundParameter palCutoff = new CompoundParameter("palCutoff", 0, 0, 1);  // palette value cutoff (fraction 0 - 1)

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
        addParameter(hVarParam);
        addParameter(speedParam);
        addParameter(tempoParam);

        addParameter(armsParam);
        addParameter(branchParam);
        addParameter(spreadParam);
        addParameter(maxLenParam);
        addParameter(triggerParam);

        addParameter(palette);
        addParameter(palStart);
        addParameter(palStop);
        addParameter(palShift);
        addParameter(palBias);
        addParameter(palCutoff);

        addParameter(noteLoParam);
        addParameter(noteHiParam);

        lx.tempo.addListener(this);
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
        if (pitch >= lo && pitch < hi && isWhiteKey(pitch)) {
            float xMin = model.xMin + model.xRange*(pitch - lo)/(hi - lo);
            float xMax = model.xMin + model.xRange*(pitch + 1 - lo)/(hi - lo);
            startInfection(pitch, xMin, xMax);
        }
    }

    @Override
    public void noteOff(int pitch) {
        stopInfection(pitch);
    }

    @Override
    public void run(double deltaMs, PolyBuffer.Space preferredSpace) {
        double deltaSec = deltaMs/1000.0;
        advanceInfections(deltaSec);
        renderInfections();
    }

    public void onBeat(Tempo tempo, int count) {
        if (tempoParam.isOn()) {
            for (Infection inf : infections) {
                inf.nextStep();
            }
        }
    }

    public void onMeasure(Tempo tempo) { }


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
            spreadParam.getValue(), hueParam.getValue(), hVarParam.getValue(), getPalette(), maxLenParam.getValuei());
        infections.add(inf);
        infectionsByKey.put(key, inf);
    }

    protected void stopInfection(int key) {
        Infection inf = infectionsByKey.get(key);
        if (inf != null) inf.stopGrowing();
    }

    protected Junction selectOrigin(float xMin, float xMax) {
        Junction origin = null;
        Junction closest = null;
        float cx = (xMin + xMax)/2;
        int count = 0;
        do {
            origin = junctions.get(random.nextInt(junctions.size()));
            if (origin.loc.x >= xMin && origin.loc.x < xMax) return origin;
            if (closest == null) closest = origin;
            else if (Math.abs(origin.loc.x - cx) < Math.abs(closest.loc.x - cx)) closest = origin;
        } while (++count < 100);
        return closest;
    }

    ColorPalette getPalette() {
        ZigzagPalette pal = new ZigzagPalette();
        pal.setPalette(paletteLibrary.get(palette.getOption()));
        pal.setBottom(palStart.getValue());
        pal.setTop(palStop.getValue());
        pal.setBias(palBias.getValue());
        pal.setShift(palShift.getValue());
        pal.setCutoff(palCutoff.getValue());
        return pal;
    }

    class Infection {
        public LXVector originVector;
        public Map<Integer, Double> pointAges = new HashMap<>();
        public Map<Bundle, Integer> bundleTraversals = new HashMap<>();
        public List<Segment> growingSegments;
        public List<Segment> nextStepSegments;
        public double branchFactor;
        public double spreadFactor;
        public Random random = new Random();
        public double infectionAge = 0;
        public boolean growing = true;
        public boolean expiring = false;

        public double expireStartAge = Double.MAX_VALUE;
        public double growStopAge = Double.MAX_VALUE;
        public double hue = 0;
        public double hueVar = 1;
        public ColorPalette palette;
        public double grownLength = 0;
        public double maxLength = 0;

        public Infection(Junction origin, double initialBranchFactor, double branchFactor, double spreadFactor, double hue, double hueVar, ColorPalette palette, double maxLength) {
            this.originVector = origin.loc;
            growingSegments = startSegments(origin, initialBranchFactor);
            nextStepSegments = new ArrayList<>();
            this.branchFactor = branchFactor;
            this.spreadFactor = spreadFactor;
            this.hue = hue;
            this.hueVar = hueVar;
            this.palette = palette;
            this.maxLength = maxLength;
        }

        public void beginExpiring() {
            if (!expiring) {
                expiring = true;
                expireStartAge = infectionAge;
                System.out.println("start expiring");
            }
        }

        public void stopGrowing() {
            if (growing) {
                growing = false;
                growStopAge = infectionAge;
                System.out.println("stop growing");
                beginExpiring();
            }
        }

        public boolean isExpired() {
            return !growing && infectionAge > growStopAge + expireStartAge;
        }

        public void nextStep() {
            growingSegments.addAll(nextStepSegments);
            nextStepSegments.clear();
        }

        /** The "progress" argument is measured in strip lengths. */
        public void advance(double deltaSec) {
            infectionAge += deltaSec;
            for (Integer i : pointAges.keySet()) {
                pointAges.put(i, pointAges.get(i) + deltaSec);
            }
            if (grownLength > maxLength) beginExpiring();

            if (!growing) return;

            double progress = speedParam.getValue()*deltaSec/60.0;
            if (tempoParam.isOn()) {
                progress = 6 * deltaSec;  // advance one strip length in 166 ms
            }

            List<Segment> continuingSegments = new ArrayList<>();
            for (Segment s : growingSegments) {
                double remain = 1 - s.progress;
                if (progress > remain) {
                    // We hit a node.  Branch out to some new bundles.
                    advanceSegment(s, remain);
                    List<Segment> nextSegments = startSegments(s.getEnd(), branchFactor);
                    for (Segment ns : nextSegments) {
                        advanceSegment(ns, progress - remain);
                        nextStepSegments.add(ns);
                    }
                } else {
                    advanceSegment(s, progress);
                    continuingSegments.add(s);
                }
            }

            grownLength += progress;
            if (!tempoParam.isOn()) {
                continuingSegments.addAll(nextStepSegments);
                nextStepSegments.clear();
            }
            growingSegments = continuingSegments;
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

            // Order the available bundles in decreasing distance away from the infection origin.
            Collections.sort(available, new Comparator<Bundle>() {
                @Override public int compare(Bundle a, Bundle b) {
                    double aDist = originVector.dist(a.getOpposite(origin).loc);
                    double bDist = originVector.dist(b.getOpposite(origin).loc);
                    if (aDist < bDist) return 1;
                    if (aDist > bDist) return -1;
                    return 0;
                }
            });

            // Use the spread factor to bias the selection of bundles toward those further away.
            while (!available.isEmpty() && selected.size() < targetCount) {
                double index = Math.pow(random.nextDouble(), Math.pow(2, spreadFactor));
                Bundle pick = available.get((int) (index*available.size()));
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
                        xMin = xMin + (xMax - xMin)*oldProgress;
                        xMax = xMin + (xMax - xMin)*newProgress;
                    } else {
                        xMax = xMax + (xMin - xMax)*oldProgress;
                        xMin = xMax + (xMin - xMax)*newProgress;
                    }
                    break;
                case Y:
                    if (segment.sign == Sign.POS) {
                        yMin = yMin + (yMax - yMin)*oldProgress;
                        yMax = yMin + (yMax - yMin)*newProgress;
                    } else {
                        yMax = yMax + (yMin - yMax)*oldProgress;
                        yMin = yMax + (yMin - yMax)*newProgress;
                    }
                    break;
                case Z:
                    if (segment.sign == Sign.POS) {
                        zMin = zMin + (zMax - zMin)*oldProgress;
                        zMax = zMin + (zMax - zMin)*newProgress;
                    } else {
                        zMax = zMax + (zMin - zMax)*oldProgress;
                        zMin = zMax + (zMin - zMax)*newProgress;
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
                    value = 1.0 - (age - expireStartAge)/expireStartAge;
                } else {
                    value = 1.0 - age/infectionAge;
                }
                if (value < 0) value = 0;
                long pc = palette.getColor16(value);
                int r = Ops16.red(pc);
                int g = Ops16.green(pc);
                int b = Ops16.blue(pc);
                int a = (int) (value*Ops16.MAX + 0.5);
                if (value == 0) r = g = b = 0;
                array[index] = Ops16.add(array[index], shiftHue(Ops16.rgba(r, g, b, a), hue, hueVar, palette.getHueCenter()));
            }
        }
    }

    public long shiftHue(long c, double shift) {
        if (shift == 0) return c;
        int color = Spaces.rgb16ToRgb8(c);

        float h = LXColor.h(color);
        float s = LXColor.s(color);
        float b = LXColor.b(color);
        int alpha = color & 0xff000000;
        return Spaces.rgb8ToRgb16(alpha | (LXColor.hsb(h + shift * 360f, s, b) & 0x00ffffff));
    }

    public static long shiftHue(long c, double shift, double var, double center) {
        if (shift == 0 && var == 1) return c;
        int color = Spaces.rgb16ToRgb8(c);

        double h = LXColor.h(color) / 360.0;
        double s = LXColor.s(color) / 100.0;
        double b = LXColor.b(color) / 100.0;
        int alpha = color & 0xff000000;

        h = h - center + 0.5;
        float hf = (float) Math.floor(h);
        h = h - hf;
        h = h - 0.5;
        h *= var;
        h = h + center;
        h += shift;

        return Spaces.rgb8ToRgb16(alpha | (LXColor.hsb(h * 360, s * 100, b * 100) & 0x00ffffff));
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
