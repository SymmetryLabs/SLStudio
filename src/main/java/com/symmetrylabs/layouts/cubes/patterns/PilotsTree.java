package com.symmetrylabs.layouts.cubes.patterns;

import com.symmetrylabs.layouts.cubes.CubesModel;
import com.symmetrylabs.layouts.cubes.topology.CubeTopology;
import com.symmetrylabs.layouts.cubes.topology.EdgeAStar;
import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.midi.MidiNote;
import heronarts.lx.midi.MidiNoteOn;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.ADSREnvelope;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.FixedParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.transform.LXVector;

import java.util.*;

public class PilotsTree extends SLPattern<CubesModel> {
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

    private class PathElement {
        Strip[] strips;
        boolean[] forwards;
    }
    private class Root {
        CubeTopology.Bundle top;
        CubeTopology.Bundle bottom;
        List<PathElement> path;
        ADSREnvelope adsr;
    }

    private class Gap {
        int start = -1;
        int end = -1;
    }

    CubeTopology topology;
    EdgeAStar aStar;
    List<Root> roots;
    boolean started = false;
    LinkedList<Gap> gaps;
    int maxGapAge = 0;
    double gapDelay = 0;
    int nextRootToAttack = 0;

    private ADSREnvelope globalADSR;

    public PilotsTree(LX lx) {
        super(lx);

        topology = new CubeTopology(model);
        aStar = new EdgeAStar(topology);
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

        globalADSR = makeADSR();
        addModulator(globalADSR);

        started = true;
        build();
    }

    @Override
    public void onParameterChanged(LXParameter p) {
        if (p == countParam || p == topRadiusParam)
            build();
    }

    private void build() {
        if (!started)
            return;

        if (roots != null) {
            for (Root r : roots) {
                removeModulator(r.adsr);
            }
        }

        /* Generate lists of allowable root top bundles and root bottom bundles */
        List<CubeTopology.Bundle> rootTops = new ArrayList<>();
        for (CubeTopology.Bundle e : topology.edges) {
            if (e.dir != CubeTopology.EdgeDirection.Y)
                continue;
            /* only get elements with nothing above them */
            if (e.pa != null)
                continue;
            float x = e.endpoints().end.x;
            if (Math.abs(model.cx - x) < topRadiusParam.getValuef())
                rootTops.add(e);
        }

        List<CubeTopology.Bundle> rootBottoms = new ArrayList<>();
        for (CubeTopology.Bundle e : topology.edges) {
            if (e.dir == CubeTopology.EdgeDirection.Y)
                continue;

            /* only get elements with nothing below them */
            if (e.dir == CubeTopology.EdgeDirection.X && (e.pbn != null || e.nbn != null))
                continue;
            if (e.dir == CubeTopology.EdgeDirection.Z && (e.pcn != null || e.ncn != null))
                continue;

            rootBottoms.add(e);
        }

        /* Shuffle those lists (this is where the randomness comes from */
        Collections.shuffle(rootTops);
        Collections.shuffle(rootBottoms);

        /* Now generate N roots, making sure none of the roots overlap */
        int maxN = Integer.min(rootTops.size(), rootBottoms.size());
        int N = Integer.min(countParam.getValuei(), maxN);

        boolean[] bottomsUsed = new boolean[rootBottoms.size()];
        Arrays.fill(bottomsUsed, false);

        HashSet<CubeTopology.Bundle> used = new HashSet<>();
        roots = new ArrayList<>();
        for (int i = 0; i < rootTops.size() && roots.size() < N; i++) {
            Root r = new Root();
            r.top = rootTops.get(i);

            boolean added = false;

            /* We try every unused bottom against every top, because the likelihood
             * that a random pair intersects with an existing root gets pretty high
             * when we add a bunch of roots. */
            for (int j = 0; j < rootBottoms.size() && !added; j++) {
                if (bottomsUsed[j])
                    continue;
                r.bottom = rootBottoms.get(j);

                List<CubeTopology.Bundle> path;
                try {
                    path = aStar.findPath(r.top, r.bottom);
                } catch (EdgeAStar.NotConnectedException ex) {
                    ex.printStackTrace();
                    continue;
                }

                boolean containsUsed = false;
                for (CubeTopology.Bundle b : path) {
                    if (used.contains(b)) {
                        containsUsed = true;
                        break;
                    }
                }
                if (containsUsed)
                    continue;
                used.addAll(path);

                /* Now we figure out which direction we traverse each strip as
                 * we go through the path */
                r.path = new ArrayList<>();
                for (CubeTopology.Bundle b : path) {
                    PathElement e = new PathElement();
                    e.strips = new Strip[b.strips.length];
                    e.forwards = new boolean[b.strips.length];

                    LXVector match;
                    if (r.path.isEmpty()) {
                        match = new LXVector(model.cx, model.yMax, model.cz);
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

                added = true;
                roots.add(r);
            }
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
            for (Iterator<Gap> iter = gaps.iterator(); iter.hasNext();) {
                Gap p = iter.next();
                p.start += steps;
                if (p.end >= 0)
                    p.end += steps;
                if (p.end > maxGapAge)
                    iter.remove();
            }
        }

        int black = LXColor.gray(0);
        for (int i = 0; i < colors.length; i++)
            colors[i] = black;

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

    @Override
    public void noteOnReceived(MidiNoteOn note) {
        switch (note.getPitch()) {
            case 60:
                build();
                break;

            case 62:
                if (!gaps.isEmpty() && gaps.peekFirst().end < 0)
                    gaps.peekFirst().end = 0;
                gaps.addFirst(new Gap());
                gaps.peekFirst().start = 0;
                gaps.peekFirst().end = -1;
                break;

            case 64:
                globalADSR.attack();
                break;

            case 65:
                roots.get(nextRootToAttack).adsr.attack();
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
                if (!gaps.isEmpty() && gaps.peekFirst().end < 0)
                    gaps.peekFirst().end = 0;
                break;

            case 64:
                globalADSR.release();
                break;

            case 65:
                roots.get(nextRootToAttack).adsr.release();
                nextRootToAttack++;
                if (nextRootToAttack >= roots.size())
                    nextRootToAttack = 0;
                break;

            default:
                System.out.println(String.format("unknown midi pitch %d", note.getPitch()));
        }
    }

    private ADSREnvelope makeADSR() {
        return new ADSREnvelope(
            "PilotsTree ADSR", minBrightParam, maxBrightParam,
            attackParam, decayParam, sustainParam, releaseParam,
            new FixedParameter(1));
    }
}
