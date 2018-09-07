package com.symmetrylabs.shows.pilots;

import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;
import com.symmetrylabs.slstudio.model.StripsTopology;
import com.symmetrylabs.slstudio.model.StripsTopology.Dir;
import com.symmetrylabs.slstudio.model.StripsTopology.Sign;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import com.symmetrylabs.util.EdgeAStar;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.midi.MidiNote;
import heronarts.lx.midi.MidiNoteOn;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.ADSREnvelope;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.FixedParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.transform.LXVector;

import java.util.*;

public class PilotsRoots<T extends Strip> extends SLPattern<StripsModel<T>> {
    public static final String GROUP_NAME = PilotsShow.SHOW_NAME;

    private DiscreteParameter countParam = new DiscreteParameter("count", 6, 0, 12);
    /* LEDs per second */
    private DiscreteParameter gapSpeedParam = new DiscreteParameter("speed", 90, 1, 500);
    private CompoundParameter topRadiusParam = new CompoundParameter("toprad", 60, 0, 150);

    private CompoundParameter attackParam = new CompoundParameter("attack", 60, 0, 500);
    private CompoundParameter decayParam = new CompoundParameter("decay", 40, 0, 1000);
    private CompoundParameter sustainParam = new CompoundParameter("sustain", 1, 0, 1);
    private CompoundParameter releaseParam = new CompoundParameter("release", 300, 0, 2000);

    private CompoundParameter minBrightParam = new CompoundParameter("bright", 70, 0, 100);
    private CompoundParameter maxBrightParam = new CompoundParameter("hit", 100, 0, 100);

    private DiscreteParameter rootModeParam = new DiscreteParameter("shape", 0, 0, 2);

    private BooleanParameter trigOneParam = new BooleanParameter("trig1", false).setMode(BooleanParameter.Mode.MOMENTARY);
    private BooleanParameter trigAllParam = new BooleanParameter("trig", false).setMode(BooleanParameter.Mode.MOMENTARY);
    private BooleanParameter addGapParam = new BooleanParameter("gap", false).setMode(BooleanParameter.Mode.MOMENTARY);
    private BooleanParameter regenParam = new BooleanParameter("regen", false).setMode(BooleanParameter.Mode.MOMENTARY);

    private static class PathElement {
        Strip[] strips;
        boolean[] forwards;
    }

    private static class Root {
        StripsTopology.Bundle top;
        StripsTopology.Bundle bottom;
        List<PathElement> path;
        ADSREnvelope adsr;
    }

    private class Gap {
        int start = -1;
        int end = -1;
    }

    private EdgeAStar aStar;
    private List<Root> roots;
    private boolean started = false;
    private LinkedList<Gap> gaps;
    private int maxGapAge = 0;
    private double gapDelay = 0;
    private int nextRootToAttack = 0;

    private ADSREnvelope globalADSR;

    public PilotsRoots(LX lx) {
        super(lx);

        aStar = new EdgeAStar(model.getTopology());
        gaps = new LinkedList<>();

        addParameter(attackParam);
        addParameter(decayParam);
        addParameter(sustainParam);
        addParameter(releaseParam);

        addParameter(minBrightParam);
        addParameter(maxBrightParam);

        addParameter(countParam);
        addParameter(topRadiusParam);
        addParameter(gapSpeedParam);

        addParameter(rootModeParam);

        addParameter(trigAllParam);
        addParameter(trigOneParam);
        addParameter(addGapParam);
        addParameter(regenParam);

        globalADSR = makeADSR();
        addModulator(globalADSR);

        /* Each addParameter will call onParameterChanged, and we don't want
         * to build that many times, because each build can take ~100ms. */
        started = true;
        build();
    }

    @Override
    public void onParameterChanged(LXParameter p) {
        if (p == countParam || p == topRadiusParam || p == rootModeParam) {
            build();
        } else if (p == attackParam || p == decayParam || p == sustainParam || p == releaseParam || p == minBrightParam || p == maxBrightParam) {
            globalADSR.attack();
            globalADSR.release();
            for (Root r : roots) {
                r.adsr.attack();
                r.adsr.release();
            }
        } else if (p == trigAllParam) {
            if (trigAllParam.getValueb()) {
                globalADSR.attack();
            } else {
                globalADSR.release();
            }
        } else if (p == addGapParam) {
            if (addGapParam.getValueb()) {
                startGap();
            } else {
                endGap();
            }
        } else if (p == regenParam) {
            if (regenParam.getValueb()) {
                build();
            }
        } else if (p == trigOneParam) {
            if (trigOneParam.getValueb()) {
                attackOne();
            } else {
                releaseOne();
            }
        }
    }

    /* Represents a half-built root; root builders return lists of these,
     * and the build method generates the PathElements and adds the ADSR
     * to the root. */
    private static class RootSpec {
        Root root;
        List<StripsTopology.Bundle> bundles;
        /**
         * The point that determines which direction roots flow in; the end
         * of the first bundle in the list that is closest to gapOrigin is
         * the end that we start the gap at.
         */
        LXVector gapOrigin;
    }

    private List<RootSpec> buildSliceRoots() {
        HashMap<Float, List<StripsTopology.Bundle>> slices = new HashMap<>();
        for (StripsTopology.Bundle b : model.getTopology().bundles) {
            if (b.dir == Dir.X) {
                continue;
            }

            float x = b.endpoints().negative.x;
            boolean found = false;
            for (Float existing : slices.keySet()) {
                if (Math.abs(existing - x) < 4) {
                    slices.get(existing).add(b);
                    found = true;
                    break;
                }
            }
            if (!found) {
                slices.put(x, new ArrayList<>());
                slices.get(x).add(b);
            }
        }
        Random r = new Random();
        List<RootSpec> res = new ArrayList<>(slices.size());
        for (float x : slices.keySet()) {
            List<StripsTopology.Bundle> slice = slices.get(x);
            StripsTopology.Bundle start = null;

            boolean startIsVertical = r.nextBoolean();
            Dir startDir = startIsVertical ? Dir.Y : Dir.Z;
            for (StripsTopology.Bundle b : slice) {
                if (b.dir != startDir) {
                    continue;
                }

                /* Looking for one with nothing below it and nothing in front
                 * (in negative-Z) of it. */
                boolean ok;
                if (startIsVertical) {
                    ok = b.get(Sign.NEG).get(Dir.Y, Sign.NEG) == null;
                    ok = ok && b.get(Sign.NEG).get(Dir.Z, Sign.NEG) == null;
                    ok = ok && b.get(Sign.POS).get(Dir.Z, Sign.NEG) == null;
                } else {
                    ok = b.get(Sign.NEG).get(Dir.Y, Sign.NEG) == null;
                    ok = ok && b.get(Sign.POS).get(Dir.Y, Sign.NEG) == null;
                    ok = ok && b.get(Sign.NEG).get(Dir.Z, Sign.NEG) == null;
                }
                if (ok) {
                    start = b;
                    break;
                }
            }
            if (start == null) {
                throw new IllegalStateException("slice has no admissable start bundles");
            }
            List<StripsTopology.Bundle> path = new ArrayList<>();

            StripsTopology.Bundle t = start;
            while (t != null) {
                if (path.contains(t)) {
                    throw new IllegalStateException("cycle in path");
                }
                path.add(t);
                StripsTopology.Bundle pz = t.get(Sign.POS).get(Dir.Z, Sign.POS);
                StripsTopology.Bundle py = t.get(Sign.POS).get(Dir.Y, Sign.POS);
                if (pz == null) {
                    t = py;
                } else if (py == null) {
                    t = pz;
                } else {
                    t = r.nextBoolean() ? py : pz;
                }
            }

            Root root = new Root();
            root.top = start;
            root.bottom = t;
            RootSpec spec = new RootSpec();
            spec.root = root;
            spec.bundles = path;
            spec.gapOrigin = new LXVector(x, model.yMin, model.zMin);
            res.add(spec);
        }
        res.sort((a, b) -> Float.compare(
            a.bundles.get(0).endpoints().negative.x,
            b.bundles.get(0).endpoints().negative.x));
        return res;
    }

    private List<RootSpec> buildTreeRoots() {
        /* Generate lists of allowable root top bundles and root bottom bundles */
        List<StripsTopology.Bundle> rootTops = new ArrayList<>();
        for (StripsTopology.Bundle e : model.getTopology().bundles) {
            if (e.dir != Dir.Y) {
                continue;
            }
            /* only get elements with nothing above them */
            if (e.get(Sign.POS).get(Dir.Y, Sign.POS) != null) {
                continue;
            }

            float x = e.endpoints().positive.x;
            if (Math.abs(model.cx - x) < topRadiusParam.getValuef()) {
                rootTops.add(e);
            }
        }

        List<StripsTopology.Bundle> rootBottoms = new ArrayList<>();
        for (StripsTopology.Bundle e : model.getTopology().bundles) {
            if (e.dir == Dir.Y) {
                continue;
            }

            /* only get elements with nothing below them that are on the
             * edge of the structure (meaning at least one of the directions
             * in-bottom-plane has no bundle in it). */
            boolean ok = e.get(Sign.NEG).get(Dir.Y, Sign.NEG) == null &&
                                     e.get(Sign.POS).get(Dir.Y, Sign.NEG) == null;
            if (ok) {
                /* Just make sure we have a single direction in the XZ plane
                 * with no neighbor. */
                ok = false;
                outer:
                for (Sign end : Sign.values()) {
                    for (Dir d : new Dir[]{Dir.X, Dir.Z}) {
                        for (Sign s : Sign.values()) {
                            if (e.get(end).get(d, s) == null) {
                                ok = true;
                                break outer;
                            }
                        }
                    }
                }
                if (ok) {
                    rootBottoms.add(e);
                }
            }
        }

        /* Shuffle those lists (this is where the randomness comes from */
        Collections.shuffle(rootTops);
        Collections.shuffle(rootBottoms);

        /* Now generate N roots, making sure none of the roots overlap */
        int maxN = Integer.min(rootTops.size(), rootBottoms.size());
        int N = Integer.min(countParam.getValuei(), maxN);

        boolean[] bottomsUsed = new boolean[rootBottoms.size()];
        Arrays.fill(bottomsUsed, false);

        HashSet<StripsTopology.Bundle> used = new HashSet<>();
        List<RootSpec> res = new ArrayList<>(N);

        for (int i = 0; i < rootTops.size() && res.size() < N; i++) {
            Root r = new Root();
            r.top = rootTops.get(i);

            boolean added = false;

            /* We try every unused bottom against every top, because the likelihood
             * that a random pair intersects with an existing root gets pretty high
             * when we add a bunch of roots. */
            for (int j = 0; j < rootBottoms.size() && !added; j++) {
                if (bottomsUsed[j]) {
                    continue;
                }
                r.bottom = rootBottoms.get(j);

                List<StripsTopology.Bundle> path;
                try {
                    path = aStar.findPath(r.top, r.bottom);
                } catch (EdgeAStar.NotConnectedException ex) {
                    ex.printStackTrace();
                    continue;
                }

                boolean containsUsed = false;
                for (StripsTopology.Bundle b : path) {
                    if (used.contains(b)) {
                        containsUsed = true;
                        break;
                    }
                }
                if (containsUsed) {
                    continue;
                }
                used.addAll(path);

                RootSpec spec = new RootSpec();
                spec.root = r;
                spec.bundles = path;
                spec.gapOrigin = new LXVector(model.cx, model.yMax, model.cz);
                res.add(spec);
                added = true;
            }
        }
        return res;
    }

    private void build() {
        if (!started) {
            return;
        }

        if (roots != null) {
            for (Root r : roots) {
                removeModulator(r.adsr);
            }
        }
        nextRootToAttack = 0;

        roots = new ArrayList<>();
        List<RootSpec> newRoots;
        if (rootModeParam.getValuei() == 0) {
            newRoots = buildSliceRoots();
        } else {
            newRoots = buildTreeRoots();
        }

        for (RootSpec spec : newRoots) {
            Root r = spec.root;
            List<StripsTopology.Bundle> path = spec.bundles;

            /* Figure out which direction we traverse each strip as we go through the path */
            r.path = new ArrayList<>();
            for (StripsTopology.Bundle b : path) {
                PathElement e = new PathElement();
                e.strips = new Strip[b.strips.length];
                e.forwards = new boolean[b.strips.length];

                LXVector match;
                if (r.path.isEmpty()) {
                    match = spec.gapOrigin;
                } else {
                    PathElement last = r.path.get(r.path.size() - 1);
                    Strip lastStrip = last.strips[0];
                    boolean lastForwards = last.forwards[0];
                    match = new LXVector(lastStrip.points[lastForwards ? lastStrip.points.length - 1 : 0]);
                }

                for (int strip = 0; strip < b.strips.length; strip++) {
                    Strip s = model.getStripByIndex(b.strips[strip]);
                    e.strips[strip] = s;

                    LXVector front = new LXVector(s.points[0]);
                    LXVector back = new LXVector(s.points[s.points.length - 1]);

                    float forwardDist = front.dist(match);
                    float reverseDist = back.dist(match);
                    e.forwards[strip] = forwardDist < reverseDist;
                }

                r.path.add(e);
            }

            r.adsr = makeADSR();
            addModulator(r.adsr);
            roots.add(r);

            globalADSR.attack();
            globalADSR.release();
        }

        maxGapAge = 0;
        for (Root r : roots) {
            int elementCount = 0;
            for (PathElement e : r.path) {
                elementCount += e.strips[0].size;
            }
            maxGapAge = Integer.max(maxGapAge, elementCount);
        }
    }

    @Override
    public void run(double deltaMs) {
        gapDelay += deltaMs;
        /* in steps per millisecond */
        float gapSpeed = gapSpeedParam.getValuef() / 1000f;
        int steps = (int) Math.floor(gapDelay * gapSpeed);
        if (steps > 0) {
            gapDelay -= steps / gapSpeed;
            for (Iterator<Gap> iter = gaps.iterator(); iter.hasNext(); ) {
                Gap p = iter.next();
                p.start += steps;
                if (p.end >= 0) {
                    p.end += steps;
                }
                if (p.end > maxGapAge) {
                    iter.remove();
                }
            }
        }

        int black = LXColor.gray(0);
        for (int i = 0; i < colors.length; i++) {
            colors[i] = black;
        }

        float globalBright = globalADSR.getValuef();

        for (Root r : roots) {
            /* Each root is the maximum of the global brightness and its
             * own brightness. */
            float rootBright = Float.max(globalBright, r.adsr.getValuef());

            /* We keep track of how many LEDs we've seen along the course of
             * the whole path before a given element, so that we can figure
             * out which LEDs are in gaps */
            int previousStripLEDs = 0;

            for (PathElement e : r.path) {
                for (int stripIndex = 0; stripIndex < e.strips.length; stripIndex++) {
                    Strip strip = e.strips[stripIndex];
                    boolean forward = e.forwards[stripIndex];
                    LXPoint[] points = strip.points;

                    for (int localIndex = 0; localIndex < points.length; localIndex++) {
                        int localEffectiveIndex = forward ? localIndex : points.length - localIndex - 1;
                        int globalIndex = localEffectiveIndex + previousStripLEDs;

                        boolean inGap = false;
                        for (Gap gap : gaps) {
                            if (gap.end <= globalIndex && globalIndex <= gap.start) {
                                inGap = true;
                                break;
                            }
                        }
                        if (!inGap) {
                            colors[points[localIndex].index] = LXColor.gray(rootBright);
                        }
                    }
                }
                /* We assume that all strips in a given element have the same length
                 * (or at least close enough that it doesn't matter */
                previousStripLEDs += e.strips[0].size;
            }
        }
    }

    private void startGap() {
        if (!gaps.isEmpty() && gaps.peekFirst().end < 0) {
            gaps.peekFirst().end = 0;
        }
        gaps.addFirst(new Gap());
        gaps.peekFirst().start = 0;
        gaps.peekFirst().end = -1;
    }

    private void endGap() {
        if (!gaps.isEmpty() && gaps.peekFirst().end < 0) {
            gaps.peekFirst().end = 0;
        }
    }

    private void attackOne() {
        roots.get(nextRootToAttack).adsr.attack();
    }

    private void releaseOne() {
        roots.get(nextRootToAttack).adsr.release();
        nextRootToAttack++;
        if (nextRootToAttack >= roots.size()) {
            nextRootToAttack = 0;
        }
    }

    @Override
    public void noteOnReceived(MidiNoteOn note) {
        switch (note.getPitch()) {
            case 60:
                build();
                break;
            case 62:
                startGap();
                break;
            case 64:
                globalADSR.attack();
                break;
            case 65:
                attackOne();
                break;
            default:
                System.out.println(String.format("unknown midi pitch %d", note.getPitch()));
        }
    }

    @Override
    public void noteOffReceived(MidiNote note) {
        switch (note.getPitch()) {
            case 60:
                break;
            case 62:
                endGap();
                break;
            case 64:
                globalADSR.release();
                break;
            case 65:
                releaseOne();
                break;
            default:
                System.out.println(String.format("unknown midi pitch %d", note.getPitch()));
        }
    }

    private ADSREnvelope makeADSR() {
        return new ADSREnvelope(
            "PilotsRoots ADSR", minBrightParam, maxBrightParam,
            attackParam, decayParam, sustainParam, releaseParam,
            new FixedParameter(1));
    }
}
