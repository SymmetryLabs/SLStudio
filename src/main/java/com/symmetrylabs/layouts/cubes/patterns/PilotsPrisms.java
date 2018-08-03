package com.symmetrylabs.layouts.cubes.patterns;

import com.symmetrylabs.color.Ops8;
import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;
import com.symmetrylabs.slstudio.model.StripsTopology;
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

import java.util.*;

public class PilotsPrisms<T extends Strip> extends SLPattern<StripsModel<T>> {
    private static final float PLANE_TOLERANCE = 3; // inches

    private CompoundParameter attackParam = new CompoundParameter("attack", 60, 0, 500);
    private CompoundParameter decayParam = new CompoundParameter("decay", 40, 0, 1000);
    private CompoundParameter sustainParam = new CompoundParameter("sustain", 1, 0, 1);
    private CompoundParameter releaseParam = new CompoundParameter("release", 300, 0, 2000);

    private DiscreteParameter maxSizeParam = new DiscreteParameter("maxsize", 4, 1, 20);

    private static class Plane {
        StripsTopology.EdgeDirection normal;
        float v;
        List<StripsTopology.Bundle> bundles = new ArrayList<>();

        public boolean contains(StripsTopology.Bundle b) {
            if (b.dir == normal)
                return false;
            float bv = 0;
            switch (normal) {
                case X:
                    bv = b.planarLocation().a;
                    break;
                case Y:
                    if (b.dir == StripsTopology.EdgeDirection.X)
                        bv = b.planarLocation().a;
                    else
                        bv = b.planarLocation().b;
                    break;
                case Z:
                    bv = b.planarLocation().b;
                    break;
            }
            return Math.abs(bv - v) < PLANE_TOLERANCE;
        }
    }

    class Range {
        int lo, hi;
    }

    private class Prism {
        final Range x, y, z;
        ADSREnvelope adsr;

        Prism(Range x, Range y, Range z, ADSREnvelope adsr) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.adsr = adsr;
        }

        boolean isOnEdge(StripsTopology.Bundle b) {
            Plane pxl = xPlanes.get(x.lo);
            Plane pxh = xPlanes.get(x.hi);
            Plane pyl = yPlanes.get(y.lo);
            Plane pyh = yPlanes.get(y.hi);
            Plane pzl = zPlanes.get(z.lo);
            Plane pzh = zPlanes.get(z.hi);

            int planeMemberships = 0;
            planeMemberships += pxl.contains(b) || pxh.contains(b) ? 1 : 0;
            planeMemberships += pyl.contains(b) || pyh.contains(b) ? 1 : 0;
            planeMemberships += pzl.contains(b) || pzh.contains(b) ? 1 : 0;

            boolean projectionInBox = false;
            float proj = b.projection();
            switch (b.dir) {
                case X: projectionInBox = pxl.v < proj && proj < pxh.v; break;
                case Y: projectionInBox = pyl.v < proj && proj < pyh.v; break;
                case Z: projectionInBox = pzl.v < proj && proj < pzh.v; break;
            }

            return projectionInBox && planeMemberships > 1;
        }
    }

    private final List<Prism> prisms;
    private final List<Plane> xPlanes;
    private final List<Plane> yPlanes;
    private final List<Plane> zPlanes;
    private final HashMap<Integer, List<Prism>> midiPrisms;

    public PilotsPrisms(LX lx) {
        super(lx);

        addParameter(attackParam);
        addParameter(decayParam);
        addParameter(sustainParam);
        addParameter(releaseParam);

        prisms = new ArrayList<>();
        xPlanes = new ArrayList<>();
        yPlanes = new ArrayList<>();
        zPlanes = new ArrayList<>();
        midiPrisms = new HashMap<>();

        for (StripsTopology.Bundle b : model.getTopology().bundles) {
            switch (b.dir) {
                case X:
                    addYPlane(b);
                    addZPlane(b);
                    break;

                case Y:
                    addXPlane(b);
                    addZPlane(b);
                    break;

                case Z:
                    addXPlane(b);
                    addYPlane(b);
                    break;

                default: continue;
            }
        }

        xPlanes.sort((a, b) -> Float.compare(a.v, b.v));
        yPlanes.sort((a, b) -> Float.compare(a.v, b.v));
        zPlanes.sort((a, b) -> Float.compare(a.v, b.v));
    }

    private void addXPlane(StripsTopology.Bundle b) {
        for (Plane p : xPlanes) {
            if (p.contains(b)) {
                p.bundles.add(b);
                return;
            }
        }
        Plane p = new Plane();
        p.normal = StripsTopology.EdgeDirection.X;
        p.v = b.endpoints().negative.x;
        p.bundles.add(b);
        xPlanes.add(p);
    }

    private void addYPlane(StripsTopology.Bundle b) {
        for (Plane p : yPlanes) {
            if (p.contains(b)) {
                p.bundles.add(b);
                return;
            }
        }
        Plane p = new Plane();
        p.normal = StripsTopology.EdgeDirection.Y;
        p.v = b.endpoints().negative.y;
        p.bundles.add(b);
        yPlanes.add(p);
    }

    private void addZPlane(StripsTopology.Bundle b) {
        for (Plane p : zPlanes) {
            if (p.contains(b)) {
                p.bundles.add(b);
                return;
            }
        }
        Plane p = new Plane();
        p.normal = StripsTopology.EdgeDirection.Z;
        p.v = b.endpoints().negative.z;
        p.bundles.add(b);
        zPlanes.add(p);
    }

    private boolean done = false;
    private double elapsed = 0;

    @Override
    public void run(double deltaMs) {
        int black = LXColor.gray(0);
        for (int i = 0; i < colors.length; i++)
            colors[i] = black;

        for (Iterator<Prism> iter = prisms.iterator(); iter.hasNext();) {
            Prism p = iter.next();
            if (p.adsr.getValuef() == 0) {
                removeModulator(p.adsr);
                iter.remove();
            }
        }

        for (StripsTopology.Bundle b : model.getTopology().bundles) {
            for (Prism p : prisms) {
                if (p.isOnEdge(b)) {
                    for (int stripIndex : b.strips) {
                        Strip s = model.getStripByIndex(stripIndex);
                        for (LXPoint point : s.points) {
                            colors[point.index] = Ops8.add(colors[point.index], LXColor.gray(p.adsr.getValuef()));
                        }
                    }
                }
            }
        }
    }

    private Random random = new Random();
    private Range randomRange(Collection list, int maxSize) {
        /* This size value is inclusive; lo + size == hi and hi is in the range */
        int size = random.nextInt(Integer.min(list.size() - 1, maxSize)) + 1;
        Range x = new Range();
        if (size >= list.size() - 1) {
            x.lo = 0;
            x.hi = list.size() - 1;
        } else {
            x.lo = random.nextInt(list.size() - size - 1);
            x.hi = x.lo + size;
        }
        return x;
    }

    private Prism randomPrism() {
        Prism p = null;

        for (int tries = 0; tries < 1000; tries++) {
            Range x = randomRange(xPlanes, maxSizeParam.getValuei());
            Range y = randomRange(yPlanes, maxSizeParam.getValuei());
            Range z = randomRange(zPlanes, maxSizeParam.getValuei());

            ADSREnvelope adsr = new ADSREnvelope(
                "PilotsPrismsADSR", 0f, 100f,
                attackParam, decayParam, sustainParam, releaseParam);
            p = new Prism(x, y, z, adsr);

            int edgeCount = 0;
            for (StripsTopology.Bundle b : model.getTopology().bundles) {
                if (p.isOnEdge(b))
                    edgeCount++;
            }
            int expectedEdges = 4 * (x.hi - x.lo + y.hi - y.lo + z.hi - z.lo);
            float visibleProp = (float) edgeCount / (float) expectedEdges;
            if (visibleProp > 0.8)
                return p;
        }

        /* If we didn't find a good one, at least return something. */
        return p;
    }

    private void releasePitch(int pitch) {
        if (!midiPrisms.containsKey(pitch))
            return;
        for (Prism p : midiPrisms.get(pitch)) {
            p.adsr.release();
        }
    }

    @Override
    public void noteOnReceived(MidiNoteOn note) {
        releasePitch(note.getPitch());

        List<Prism> newPrisms = new ArrayList<>();
        int add = note.getPitch() - 59;
        for (int i = 0; i < add; i++) {
            Prism p = randomPrism();
            addModulator(p.adsr);
            p.adsr.attack();
            prisms.add(p);
            newPrisms.add(p);
        }
        midiPrisms.put(note.getPitch(), newPrisms);
    }

    @Override
    public void noteOffReceived(MidiNote note) {
        releasePitch(note.getPitch());
    }
}
