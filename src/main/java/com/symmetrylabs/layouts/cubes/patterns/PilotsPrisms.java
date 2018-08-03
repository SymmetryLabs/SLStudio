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

import java.util.*;

public class PilotsPrisms<T extends Strip> extends SLPattern<StripsModel<T>> {
    private static final float PLANE_TOLERANCE = 3; // inches

    private CompoundParameter attackParam = new CompoundParameter("attack", 60, 0, 500);
    private CompoundParameter decayParam = new CompoundParameter("decay", 40, 0, 1000);
    private CompoundParameter sustainParam = new CompoundParameter("sustain", 1, 0, 1);
    private CompoundParameter releaseParam = new CompoundParameter("release", 300, 0, 2000);

    private DiscreteParameter maxSizeParam = new DiscreteParameter("maxsize", 4, 1, 20);

    private class Prism {
        static final int NNN = 0;
        static final int NNP = 1;
        static final int NPN = 2;
        static final int NPP = 3;
        static final int PNN = 4;
        static final int PNP = 5;
        static final int PPN = 6;
        static final int PPP = 7;

        StripsTopology.Bundle corners[];
        HashSet<StripsTopology.Bundle> allOnEdges;
        ADSREnvelope adsr;

        Prism(StripsTopology.Bundle[] corners, ADSREnvelope adsr) {
            this.corners = corners;
            this.adsr = adsr;
            allOnEdges = new HashSet<>();
        }

        boolean isOnEdge(StripsTopology.Bundle b) {
            return allOnEdges.contains(b);
        }
    }

    private final List<StripsTopology.Junction> junctions;
    private final List<Prism> prisms;
    private final HashMap<Integer, List<Prism>> midiPrisms;

    public PilotsPrisms(LX lx) {
        super(lx);

        addParameter(attackParam);
        addParameter(decayParam);
        addParameter(sustainParam);
        addParameter(releaseParam);

        prisms = new ArrayList<>();
        junctions = new ArrayList<>();
        midiPrisms = new HashMap<>();
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

    private Prism randomPrism() {
        Prism p = null;

        for (int tries = 0; tries < 1000; tries++) {
            ADSREnvelope adsr = new ADSREnvelope(
                "PilotsPrismsADSR", 0f, 100f,
                attackParam, decayParam, sustainParam, releaseParam);
            // p = new Prism(x, y, z, adsr);

            int edgeCount = 0;
            for (StripsTopology.Bundle b : model.getTopology().bundles) {
                if (p.isOnEdge(b))
                    edgeCount++;
            }
            // int expectedEdges = 4 * (x.hi - x.lo + y.hi - y.lo + z.hi - z.lo);
            // float visibleProp = (float) edgeCount / (float) expectedEdges;
            // if (visibleProp > 0.8)
                //return p;
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
