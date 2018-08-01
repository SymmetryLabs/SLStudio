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
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.transform.LXVector;

import java.util.*;

public class PilotsTree extends SLPattern<CubesModel> {
    private DiscreteParameter countParam = new DiscreteParameter("count", 6, 0, 12);
    /* LEDs per second */
    private DiscreteParameter pulseSpeedParam = new DiscreteParameter("pulse", 20, 0, 100);
    private CompoundParameter topRadiusParam = new CompoundParameter("toprad", 60, 0, 150);

    private class PathElement {
        Strip[] strips;
        boolean[] forwards;
    }
    private class Root {
        CubeTopology.Bundle top;
        CubeTopology.Bundle bottom;
        List<PathElement> path;
    }

    private class Pulse {
        int start = -1;
        int end = -1;
    }

    CubeTopology topology;
    EdgeAStar aStar;
    List<Root> roots;
    boolean started = false;
    LinkedList<Pulse> pulses;
    int maxPulseAge = 0;
    double pulseDelay = 0;

    public PilotsTree(LX lx) {
        super(lx);

        topology = new CubeTopology(model);
        aStar = new EdgeAStar(topology);
        pulses = new LinkedList<>();

        addParameter(countParam);
        addParameter(topRadiusParam);
        addParameter(pulseSpeedParam);

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
                        Strip s = model.getStripByIndex(strip);
                        e.strips[strip] = s;

                        LXVector front = new LXVector(s.points[0]);
                        LXVector back = new LXVector(s.points[s.points.length - 1]);

                        float forwardDist = front.dist(match);
                        float reverseDist = back.dist(match);
                        e.forwards[strip] = forwardDist < reverseDist;
                    }

                    r.path.add(e);
                }

                added = true;
                roots.add(r);
            }
        }

        maxPulseAge = 0;
        for (Root r : roots) {
            int elementCount = 0;
            for (PathElement e : r.path) {
                elementCount += e.strips[0].size;
            }
            maxPulseAge = Integer.max(maxPulseAge, elementCount);
        }
    }

    @Override
    public void run(double deltaMs) {
        pulseDelay += deltaMs;
        /* in steps per millisecond */
        float pulseSpeed = pulseSpeedParam.getValuef() / 1000f;
        int steps = (int) Math.floor(pulseDelay * pulseSpeed);
        if (steps > 0) {
            pulseDelay -= steps / pulseSpeed;
            for (Iterator<Pulse> iter = pulses.iterator(); iter.hasNext();) {
                Pulse p = iter.next();
                p.start += steps;
                if (p.end >= 0)
                    p.end += steps;
                if (p.end > maxPulseAge)
                    iter.remove();
            }
        }

        int black = LXColor.gray(0);
        for (int i = 0; i < colors.length; i++)
            colors[i] = black;
        for (Root r : roots) {

            /* We keep track of how many LEDs we've seen along the course of
             * the whole path before a given element, so that we can figure
             * out which LEDs are in a pulse */
            int previousStripLEDs = 0;

            for (PathElement e : r.path) {
                for (int stripIndex = 0; stripIndex < e.strips.length; stripIndex++) {
                    Strip strip = e.strips[stripIndex];
                    boolean forward = e.forwards[stripIndex];
                    LXPoint[] points = strip.points;

                    for (LXPoint p : points)
                        colors[p.index] = LXColor.gray(100f);
                    if (1==1) continue;

                    for (int localIndex = 0; localIndex < points.length; localIndex++) {
                        int localEffectiveIndex = forward ? localIndex : points.length - localIndex - 1;
                        int globalIndex = localEffectiveIndex + previousStripLEDs;

                        boolean inPulse = false;
                        for (Pulse pulse : pulses) {
                            if (pulse.end <= globalIndex && globalIndex <= pulse.start) {
                                inPulse = true;
                                break;
                            }
                        }
                        if (!inPulse || true)
                            colors[points[localIndex].index] = LXColor.gray(100f);
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
                if (!pulses.isEmpty() && pulses.peekFirst().end < 0)
                    pulses.peekFirst().end = 0;
                pulses.addFirst(new Pulse());
                pulses.peekFirst().start = 0;
                pulses.peekFirst().end = -1;
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
                if (!pulses.isEmpty() && pulses.peekFirst().end < 0)
                    pulses.peekFirst().end = 0;

            default:
                System.out.println(String.format("unknown midi pitch %d", note.getPitch()));
        }
    }

}
