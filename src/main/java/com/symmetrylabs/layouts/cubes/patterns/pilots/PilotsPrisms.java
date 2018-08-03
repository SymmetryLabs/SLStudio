package com.symmetrylabs.layouts.cubes.patterns.pilots;

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
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;

import java.util.*;

public class PilotsPrisms<T extends Strip> extends SLPattern<StripsModel<T>> {
    private static final float PLANE_TOLERANCE = 3; // inches

    private CompoundParameter attackParam = new CompoundParameter("attack", 60, 0, 500);
    private CompoundParameter decayParam = new CompoundParameter("decay", 40, 0, 1000);
    private CompoundParameter sustainParam = new CompoundParameter("sustain", 1, 0, 1);
    private CompoundParameter releaseParam = new CompoundParameter("release", 300, 0, 2000);

    private DiscreteParameter maxSizeParam = new DiscreteParameter("scale", 10, 3, 20);
    private BooleanParameter noIntersectParam = new BooleanParameter("avoid", false);

    private class Prism {
        //                      XYZ
        StripsTopology.Junction nnn;
        StripsTopology.Junction nnp;
        StripsTopology.Junction npn;
        StripsTopology.Junction npp;
        StripsTopology.Junction pnn;
        StripsTopology.Junction pnp;
        StripsTopology.Junction ppn;
        StripsTopology.Junction ppp;

        HashSet<StripsTopology.Bundle> allEdges = new HashSet<>();
        ADSREnvelope adsr;
    }

    private final List<Prism> prisms;
    private final HashMap<Integer, List<Prism>> midiPrisms;
    private final Random random = new Random();

    public PilotsPrisms(LX lx) {
        super(lx);

        addParameter(attackParam);
        addParameter(decayParam);
        addParameter(sustainParam);
        addParameter(releaseParam);
        addParameter(maxSizeParam);
        addParameter(noIntersectParam);

        prisms = new ArrayList<>();
        midiPrisms = new HashMap<>();
    }

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
                if (p.allEdges.contains(b)) {
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

    private StripsTopology.Junction randomJunction() {
        List<StripsTopology.Junction> js = model.getTopology().junctions;
        return js.get(random.nextInt(js.size()));
    }

    private static final int PX = 0;
    private static final int NX = 1;
    private static final int PY = 2;
    private static final int NY = 3;
    private static final int PZ = 4;
    private static final int NZ = 5;

    private boolean grow(Prism p, int direction) {
        switch (direction) {
            case PX:
                if (p.nnp.px != null && p.npp.px != null && p.pnp.px != null && p.ppp.px != null) {
                    p.nnp = p.nnp.px.p;
                    p.npp = p.npp.px.p;
                    p.pnp = p.pnp.px.p;
                    p.ppp = p.ppp.px.p;
                    return true;
                }
                return false;
            case NX:
                if (p.nnn.nx != null && p.npn.nx != null && p.pnn.nx != null && p.ppn.nx != null) {
                    p.nnn = p.nnn.nx.n;
                    p.npn = p.npn.nx.n;
                    p.pnn = p.pnn.nx.n;
                    p.ppn = p.ppn.nx.n;
                    return true;
                }
                return false;
            case PY:
                if (p.npn.py != null && p.npp.py != null && p.ppn.py != null && p.ppp.py != null) {
                    p.npn = p.npn.py.p;
                    p.npp = p.npp.py.p;
                    p.ppn = p.ppn.py.p;
                    p.ppp = p.ppp.py.p;
                    return true;
                }
                return false;
            case NY:
                if (p.nnn.ny != null && p.nnp.ny != null && p.pnn.ny != null && p.pnp.ny != null) {
                    p.nnn = p.nnn.ny.n;
                    p.nnp = p.nnp.ny.n;
                    p.pnn = p.pnn.ny.n;
                    p.pnp = p.pnp.ny.n;
                    return true;
                }
                return false;
            case PZ:
                if (p.pnn.pz != null && p.pnp.pz != null && p.ppn.pz != null && p.ppp.pz != null) {
                    p.pnn = p.pnn.pz.p;
                    p.pnp = p.pnp.pz.p;
                    p.ppn = p.ppn.pz.p;
                    p.ppp = p.ppp.pz.p;
                    return true;
                }
                return false;
            case NZ:
                if (p.nnn.nz != null && p.nnp.nz != null && p.npn.nz != null && p.npp.nz != null) {
                    p.nnn = p.nnn.nz.n;
                    p.nnp = p.nnp.nz.n;
                    p.npn = p.npn.nz.n;
                    p.npp = p.npp.nz.n;
                    return true;
                }
                return false;
        }
        return false;
    }

    private void addEdges(Prism p, StripsTopology.Junction start, StripsTopology.Junction end, StripsTopology.EdgeDirection dir) {
        StripsTopology.Junction j = start;
        StripsTopology.Junction next;
        StripsTopology.Bundle bundle;

        while (j != end) {
            switch (dir) {
                case X: bundle = j.px; break;
                case Y: bundle = j.py; break;
                case Z: bundle = j.pz; break;
                default: bundle = null;
            }
            if (bundle == null)
                break;
            p.allEdges.add(bundle);
            j = bundle.p;
        }
        /* If we couldn't get to the end from the start, we head from the end back towards
         * the start. If there's only one break in the edge, this will get all of the
         * relevant strips turned on. */
        if (j != end) {
            j = end;
            while (j != start) {
                switch (dir) {
                    case X: bundle = j.nx; break;
                    case Y: bundle = j.ny; break;
                    case Z: bundle = j.nz; break;
                    default: bundle = null;
                }
                if (bundle == null)
                    break;
                p.allEdges.add(bundle);
                j = bundle.n;
            }
        }
    }

    private boolean fillWithRandomUnitPrism(Prism p) {
        StripsTopology.Junction seed = randomJunction();
        p.nnn = seed;
        p.nnp = seed;
        p.npn = seed;
        p.npp = seed;
        p.pnn = seed;
        p.pnp = seed;
        p.ppn = seed;
        p.ppp = seed;

        /* Grow it one unit in X, Y, and Z; if we fail on any, we just
         * say we have a bad seed and give up. */
        if (!grow(p, PX)) {
            if (!grow(p, NX)) {
                return false;
            }
        }
        if (!grow(p, PY)) {
            if (!grow(p, NY)) {
                return false;
            }
        }
        if (!grow(p, PZ)) {
            if (!grow(p, NZ)) {
                return false;
            }
        }
        return true;
    }

    private Prism randomPrism() {
        Prism p = new Prism();

        ADSREnvelope adsr = new ADSREnvelope(
            "PilotsPrismsADSR", 0f, 100f,
            attackParam, decayParam, sustainParam, releaseParam);
        p.adsr = adsr;

        boolean found = false;
        for (int attempts = 0; attempts < 50 && !found; attempts++)
            fillWithRandomUnitPrism(p);

        int targetGrowth = random.nextInt(maxSizeParam.getValuei());
        int added = 0;
        for (int attempts = 0; attempts < targetGrowth * 10 && added < targetGrowth; attempts++) {
            if (grow(p, random.nextInt(6))) {
                added++;
                if (p.nnn == null || p.nnp == null || p.npn == null || p.npp == null ||
                      p.pnn == null || p.pnp == null || p.ppn == null || p.ppp == null) {
                    throw new IllegalStateException("grow failed");
                }
            }
        }

        /* Add the bundles that make up the 12 cube edges */
        StripsTopology.Junction j;

        addEdges(p, p.nnn, p.nnp, StripsTopology.EdgeDirection.X);
        addEdges(p, p.nnn, p.npn, StripsTopology.EdgeDirection.Y);
        addEdges(p, p.nnn, p.pnn, StripsTopology.EdgeDirection.Z);
        addEdges(p, p.nnp, p.npp, StripsTopology.EdgeDirection.Y);
        addEdges(p, p.nnp, p.pnp, StripsTopology.EdgeDirection.Z);
        addEdges(p, p.npn, p.npp, StripsTopology.EdgeDirection.X);
        addEdges(p, p.npn, p.ppn, StripsTopology.EdgeDirection.Z);
        addEdges(p, p.npp, p.ppp, StripsTopology.EdgeDirection.Z);
        addEdges(p, p.pnn, p.pnp, StripsTopology.EdgeDirection.X);
        addEdges(p, p.pnn, p.ppn, StripsTopology.EdgeDirection.Y);
        addEdges(p, p.ppn, p.ppp, StripsTopology.EdgeDirection.X);
        addEdges(p, p.pnp, p.ppp, StripsTopology.EdgeDirection.Y);

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
            Prism p = null;

            boolean ok = false;
            for (int attempts = 0; attempts < 50 && !ok; attempts++) {
                p = randomPrism();
                ok = true;

                if (noIntersectParam.getValueb()) {
                    int startSize = p.allEdges.size();
                    for (Prism other : prisms) {
                        p.allEdges.removeAll(other.allEdges);
                        if (p.allEdges.size() != startSize) {
                            ok = false;
                            break;
                        }
                    }
                }
            }

            if (ok) {
                addModulator(p.adsr);
                p.adsr.attack();
                prisms.add(p);
                newPrisms.add(p);
            }
        }
        midiPrisms.put(note.getPitch(), newPrisms);
    }

    @Override
    public void noteOffReceived(MidiNote note) {
        releasePitch(note.getPitch());
    }
}
