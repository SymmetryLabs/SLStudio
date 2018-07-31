package com.symmetrylabs.layouts.cubes.patterns;

import com.symmetrylabs.color.Ops8;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.midi.*;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;

import java.util.*;

public class PilotsLines extends TopoPattern {
    private DiscreteParameter hCountParam = new DiscreteParameter("hcount", 40, 1, 100);
    private DiscreteParameter vCountParam = new DiscreteParameter("vcount", 30, 1, 100);
    private CompoundParameter attackParam = new CompoundParameter("attack", 100, 0, 1000);
    private CompoundParameter decayParam = new CompoundParameter("decay", 500, 0, 4000);
    private CompoundParameter speedParam = new CompoundParameter("speed", 200, 0, 800);
    private CompoundParameter widthParam = new CompoundParameter("width", 30, 0, 120);
    private DiscreteParameter lengthParam = new DiscreteParameter("length", 4, 0, 20);

    public PilotsLines(LX lx) {
        super(lx);
        addParameter(hCountParam);
        addParameter(vCountParam);
        addParameter(attackParam);
        addParameter(decayParam);
        addParameter(widthParam);
        addParameter(speedParam);
        addParameter(lengthParam);
    }

    private abstract class LineEffect {
        final float attack;
        final float decay;
        float attackAge;
        float decayAge;
        float age;
        boolean sustaining;

        public LineEffect(float attack, float decay) {
            this.attack = attack;
            this.decay = decay;
            this.attackAge = 0;
            this.decayAge = 0;
            this.age = 0;
            this.sustaining = true;
        }

        /** @returns true if the effect is still running, false otherwise */
        boolean run(double deltaMs) {
            float alpha = 0;
            age += deltaMs;
            if (sustaining) {
                attackAge += deltaMs;
                if (attackAge < attack && attackAge > 0)
                    alpha = (float) Math.pow(attackAge / attack, 2);
                else
                    alpha = 1;
            } else {
                decayAge += deltaMs;
                if (decayAge > decay)
                    return false;
                alpha = 1.f - (float) Math.pow(decayAge / decay, 2);
            }
            return applyColors(alpha);
        }

        protected abstract boolean applyColors(float alpha);
    }

    private class StaticEdgeSet extends LineEffect {
        final List<TopoEdge> edges;
        public StaticEdgeSet(List<TopoEdge> edges, float attack, float decay) {
            super(attack, decay);
            this.edges = edges;
        }

        @Override
        protected boolean applyColors(float alpha) {
            int c = LXColor.hsba(0, 0, 100, alpha);
            for (TopoEdge e : edges) {
                for (LXPoint p : model.getStripByIndex(e.i).points) {
                    colors[p.index] = Ops8.add(colors[p.index], c);
                }
            }
            return true;
        }
    }

    private class ScrollerEdgeSet extends LineEffect {
        final List<TopoEdge> edges;
        float speed;
        float width;
        float min;
        float max;

        public ScrollerEdgeSet(List<TopoEdge> edges, float attack, float decay, float speed, float width) {
            super(attack, decay);
            this.edges = edges;
            this.speed = speed;
            this.width = width;

            min = Float.MAX_VALUE;
            max = Float.MIN_VALUE;
            for (TopoEdge e : edges) {
                min = Float.min(e.minOrder(), min);
                max = Float.max(e.maxOrder(), max);
            }
            System.out.println(String.format("min %f max %f", min, max));
        }

        @Override
        protected boolean applyColors(float alpha) {
            int c = LXColor.hsba(0, 0, 100, alpha);
            float bandHi = age / 1000.f * speed + min;
            float bandLo = bandHi - width;
            for (TopoEdge e : edges) {
                for (LXPoint p : model.getStripByIndex(e.i).points) {
                    float v = Float.MIN_VALUE;
                    switch (e.dir) {
                        case X: v = p.x; break;
                        case Y: v = p.y; break;
                        case Z: v = p.z; break;
                    }
                    if (bandLo < v && v < bandHi)
                        colors[p.index] = Ops8.add(colors[p.index], c);
                }
            }
            return bandLo < max;
        }
    }

    List<LineEffect> effects = new ArrayList<>();
    /* a map from midi pitch to the effect that that note is currently playing, so
     * that we can flip the sustaining bit on the effect when the note is released. */
    HashMap<Integer, LineEffect> currentEffects = new HashMap<>();

    @Override
    public void noteOnReceived(MidiNoteOn note) {
        LineEffect cur = currentEffects.getOrDefault(note.getPitch(), null);
        if (cur != null) {
            cur.sustaining = false;
        }
        LineEffect newEffect = null;
        switch (note.getPitch()) {
            case 60:
                newEffect = createStaticVerticalLines();
                break;
            case 62:
                newEffect = createScrollingVerticalLines();
                break;
            case 64:
                newEffect = createStaticHorizontalLines();
                break;
            case 65:
                newEffect = createScrollingHorizontalLines();
                break;
            default:
                System.out.println(String.format("unknown midi pitch %d", note.getPitch()));
        }
        if (newEffect != null) {
            currentEffects.put(note.getPitch(), newEffect);
            effects.add(newEffect);
        }
    }

    @Override
    public void noteOffReceived(MidiNote note) {
        LineEffect cur = currentEffects.getOrDefault(note.getPitch(), null);
        if (cur != null) {
            cur.sustaining = false;
            currentEffects.remove(note.getPitch());
        }
    }

    @Override
    public void run(double deltaMs) {
        int black = LXColor.gray(0);
        for (int i = 0; i < colors.length; i++)
            colors[i] = black;
        effects.removeIf(e -> !e.run(deltaMs));
    }

    private List<TopoEdge> randomLineSeg(EdgeDirection d, int expectedLength) {
        Random r = new Random();
        TopoEdge e;
        do {
            e = edges.get(r.nextInt(edges.size()));
        } while (e.dir != d);

        /* determine our target line length by drawing from a poisson distribution */
        double L = Math.exp(-expectedLength);
        int len = 0;
        double p = 1;
        while (true) {
            p *= r.nextDouble();
            if (p <= L)
                break;
            len++;
        }

        LinkedList<TopoEdge> line = new LinkedList<>();
        line.add(e);
        while (line.size() < len) {
            boolean added = false;
            TopoEdge t;

            t = line.getLast();
            if (t.na != null) {
                line.addLast(t.na);
                added = true;
            }

            if (line.size() < len) {
                t = line.getFirst();
                if (t.pa != null) {
                    line.addFirst(t.pa);
                    added = true;
                }
            }

            /* There's not enough segments in this line to meet our target length */
            if (!added)
                break;
        }
        return line;
    }

    private Set<TopoEdge> createLineSet(EdgeDirection d, int count, int expectedLength) {
        Set<TopoEdge> all = new HashSet<>();
        for (int i = 0; i < count; i++) {
            all.addAll(randomLineSeg(d, expectedLength));
        }
        return all;
    }

    private ArrayList<TopoEdge> createHorizontalLines() {
        return new ArrayList<>(
            createLineSet(EdgeDirection.X, hCountParam.getValuei(), lengthParam.getValuei()));
    }

    private ArrayList<TopoEdge> createVerticalLines() {
        return new ArrayList<>(
            createLineSet(EdgeDirection.Y, vCountParam.getValuei(), lengthParam.getValuei()));
    }

    private LineEffect createStaticVerticalLines() {
        return new StaticEdgeSet(
            createVerticalLines(), attackParam.getValuef(), decayParam.getValuef());
    }

    private LineEffect createStaticHorizontalLines() {
        return new StaticEdgeSet(
            createHorizontalLines(), attackParam.getValuef(), decayParam.getValuef());
    }

    private LineEffect createScrollingVerticalLines() {
        return new ScrollerEdgeSet(
            createVerticalLines(), attackParam.getValuef(), decayParam.getValuef(),
            speedParam.getValuef(), widthParam.getValuef());
    }

    private LineEffect createScrollingHorizontalLines() {
        return new ScrollerEdgeSet(
            createHorizontalLines(), attackParam.getValuef(), decayParam.getValuef(),
            speedParam.getValuef(), widthParam.getValuef());
    }
}
