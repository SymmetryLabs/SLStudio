package com.symmetrylabs.layouts.cubes.patterns.pilots;

import com.symmetrylabs.color.Ops8;
import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;
import com.symmetrylabs.slstudio.model.StripsTopology;
import com.symmetrylabs.slstudio.model.StripsTopology.Dir;
import com.symmetrylabs.slstudio.model.StripsTopology.Sign;
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

    private static class PrismIndexer {
        private StripsTopology.Sign x, y, z;

        PrismIndexer() {
        }

        PrismIndexer(StripsTopology.Sign x, StripsTopology.Sign y, StripsTopology.Sign z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        PrismIndexer with(StripsTopology.Dir d, StripsTopology.Sign s) {
            PrismIndexer i = new PrismIndexer(x, y, z);
            switch (d) {
                case X:
                    i.x = s;
                    return i;
                case Y:
                    i.y = s;
                    return i;
                case Z:
                    i.z = s;
                    return i;
            }
            return null;
        }

        void set(StripsTopology.Dir d, StripsTopology.Sign s) {
            switch (d) {
                case X:
                    x = s;
                    return;
                case Y:
                    y = s;
                    return;
                case Z:
                    z = s;
                    return;
            }
        }

        int index() {
            int idx = 0;
            if (x == StripsTopology.Sign.POS) {
                idx |= 0x01;
            }
            if (y == StripsTopology.Sign.POS) {
                idx |= 0x02;
            }
            if (z == StripsTopology.Sign.POS) {
                idx |= 0x04;
            }
            return idx;
        }

        PrismIndexer[] adjacent() {
            PrismIndexer[] res = new PrismIndexer[3];
            res[0] = this.with(StripsTopology.Dir.X, x.other());
            res[1] = this.with(StripsTopology.Dir.Y, y.other());
            res[2] = this.with(StripsTopology.Dir.Z, z.other());
            return res;
        }

        static PrismIndexer fromIndex(int idx) {
            return new PrismIndexer(
                (idx & 0x01) != 0 ? StripsTopology.Sign.POS : StripsTopology.Sign.NEG,
                (idx & 0x02) != 0 ? StripsTopology.Sign.POS : StripsTopology.Sign.NEG,
                (idx & 0x04) != 0 ? StripsTopology.Sign.POS : StripsTopology.Sign.NEG);
        }

        static PrismIndexer[] allIndexes() {
            PrismIndexer[] idx = new PrismIndexer[8];
            for (int i = 0; i < 8; i++) {
                idx[i] = fromIndex(i);
            }
            return idx;
        }
    }

    private static class Prism {
        StripsTopology.Junction[] junctions = new StripsTopology.Junction[8];

        HashSet<StripsTopology.Bundle> allEdges = new HashSet<>();
        ADSREnvelope adsr;

        StripsTopology.Junction get(PrismIndexer i) {
            return junctions[i.index()];
        }

        void set(PrismIndexer i, StripsTopology.Junction j) {
            junctions[i.index()] = j;
        }

        void setAll(StripsTopology.Junction j) {
            for (int i = 0; i < junctions.length; i++) {
                junctions[i] = j;
            }
        }
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
        for (int i = 0; i < colors.length; i++) {
            colors[i] = black;
        }

        for (Iterator<Prism> iter = prisms.iterator(); iter.hasNext(); ) {
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

    private boolean grow(Prism p, Dir d, Sign s) {
        StripsTopology.Dir a = d.ortho1();
        StripsTopology.Dir b = d.ortho2();

        /* collect all of the junctions on the side we're moving */
        StripsTopology.Junction[] sideJunctions = new StripsTopology.Junction[4];
        for (int i = 0; i < 4; i++) {
            PrismIndexer idx = new PrismIndexer();
            idx.set(d, s);
            idx.set(a, i < 2 ? StripsTopology.Sign.NEG : StripsTopology.Sign.POS);
            idx.set(b, i % 2 == 0 ? StripsTopology.Sign.NEG : StripsTopology.Sign.POS);
            sideJunctions[i] = p.get(idx);
        }

        /* make sure all of the junctions can move in that direction */
        for (StripsTopology.Junction j : sideJunctions) {
            if (j.get(d, s) == null) {
                return false;
            }
        }

        /* actually move them */
        for (int i = 0; i < 4; i++) {
            PrismIndexer idx = new PrismIndexer();
            idx.set(d, s);
            idx.set(a, i < 2 ? StripsTopology.Sign.NEG : StripsTopology.Sign.POS);
            idx.set(b, i % 2 == 0 ? StripsTopology.Sign.NEG : StripsTopology.Sign.POS);
            p.set(idx, sideJunctions[i].get(d, s).get(s));
        }
        return true;
    }

    private void addEdges(Prism p, StripsTopology.Junction start, StripsTopology.Junction end, StripsTopology.Dir dir) {
        StripsTopology.Junction j = start;
        StripsTopology.Junction next;
        StripsTopology.Bundle bundle;

        while (j != end) {
            bundle = j.get(dir, StripsTopology.Sign.POS);
            if (bundle == null) {
                break;
            }
            p.allEdges.add(bundle);
            j = bundle.get(StripsTopology.Sign.POS);
        }
        /* If we couldn't get to the end from the start, we head from the end back towards
         * the start. If there's only one break in the edge, this will get all of the
         * relevant strips turned on. */
        if (j != end) {
            j = end;
            while (j != start) {
                bundle = j.get(dir, StripsTopology.Sign.NEG);
                if (bundle == null) {
                    break;
                }
                p.allEdges.add(bundle);
                j = bundle.get(StripsTopology.Sign.NEG);
            }
        }
    }

    private boolean fillWithRandomUnitPrism(Prism p) {
        StripsTopology.Junction seed = randomJunction();
        p.setAll(seed);

        /* Grow it one unit in X, Y, and Z; if we fail on any, we just
         * say we have a bad seed and give up. */
        if (!grow(p, Dir.X, Sign.POS)) {
            if (!grow(p, Dir.X, Sign.NEG)) {
                return false;
            }
        }
        if (!grow(p, Dir.Y, Sign.POS)) {
            if (!grow(p, Dir.Y, Sign.NEG)) {
                return false;
            }
        }
        if (!grow(p, Dir.Z, Sign.POS)) {
            if (!grow(p, Dir.Z, Sign.NEG)) {
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
        for (int attempts = 0; attempts < 50 && !found; attempts++) {
            fillWithRandomUnitPrism(p);
        }

        int targetGrowth = random.nextInt(maxSizeParam.getValuei());
        int added = 0;
        for (int attempts = 0; attempts < targetGrowth * 10 && added < targetGrowth; attempts++) {
            Dir d = null;
            Sign s = null;
            switch (random.nextInt(6)) {
                case 0:
                    d = Dir.X;
                    s = Sign.POS;
                    break;
                case 1:
                    d = Dir.X;
                    s = Sign.NEG;
                case 2:
                    d = Dir.Y;
                    s = Sign.POS;
                    break;
                case 3:
                    d = Dir.Y;
                    s = Sign.NEG;
                case 4:
                    d = Dir.Z;
                    s = Sign.POS;
                    break;
                case 5:
                    d = Dir.Z;
                    s = Sign.NEG;
                    break;
            }
            if (grow(p, d, s)) {
                added++;
            }
        }

        /* Add the bundles that make up the 12 cube edges */
        for (PrismIndexer i : PrismIndexer.allIndexes()) {
            for (PrismIndexer j : i.adjacent()) {
                if (i.index() < j.index()) {
                    StripsTopology.Dir shared = null;
                    if (i.x != j.x) {
                        shared = StripsTopology.Dir.X;
                    } else if (i.y != j.y) {
                        shared = StripsTopology.Dir.Y;
                    } else if (i.z != j.z) {
                        shared = StripsTopology.Dir.Z;
                    }
                    addEdges(p, p.get(i), p.get(j), shared);
                }
            }
        }

        return p;
    }

    private void releasePitch(int pitch) {
        if (!midiPrisms.containsKey(pitch)) {
            return;
        }
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
