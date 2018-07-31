package com.symmetrylabs.layouts.cubes.patterns;

import com.symmetrylabs.color.Ops8;
import com.symmetrylabs.util.MathUtils;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.midi.*;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.transform.LXVector;

import java.util.*;

public class PilotsLines extends TopoPattern {
    private DiscreteParameter hCountParam = new DiscreteParameter("hcount", 40, 1, 100);
    private DiscreteParameter vCountParam = new DiscreteParameter("vcount", 30, 1, 100);
    private CompoundParameter attackParam = new CompoundParameter("attack", 60, 0, 600);
    private CompoundParameter decayParam = new CompoundParameter("decay", 300, 0, 2000);
    private CompoundParameter speedParam = new CompoundParameter("speed", 200, 0, 800);
    private CompoundParameter widthParam = new CompoundParameter("width", 30, 0, 120);
    private DiscreteParameter hLengthParam = new DiscreteParameter("hlen", 8, 0, 20);
    private DiscreteParameter vLengthParam = new DiscreteParameter("vlen", 4, 0, 20);
    private CompoundParameter tailParam = new CompoundParameter("tail", 30, 0, 120);

    public PilotsLines(LX lx) {
        super(lx);
        addParameter(hCountParam);
        addParameter(vCountParam);
        addParameter(attackParam);
        addParameter(decayParam);
        addParameter(widthParam);
        addParameter(speedParam);
        addParameter(hLengthParam);
        addParameter(vLengthParam);
        addParameter(tailParam);
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
                for (LXVector p : getVectors(model.getStripByIndex(e.i).points)) {
                    colors[p.index] = Ops8.add(colors[p.index], c);
                }
            }
            return true;
        }
    }

    private class ScrollerEdgeSet extends LineEffect {
        final List<List<TopoEdge>> lines;
        final float speed;
        final float width;
        final float[] min;
        final float[] max;
        /* If true, we're scrolling a white bar through a transparent field.
           If false, we're scrolling a bar of the current palette color through a white field */
        final boolean whiteBar;
        /* the length of the fade-out on the end of the line. If zero, has a hard end */
        final float tail;
        final float maskPercentage;
        final float maskSpeedScale;
        final float barDelay;

        public ScrollerEdgeSet(
                    List<List<TopoEdge>> lines, float attack, float decay, float speed, float width,
                    float tail, boolean whiteBar) {
            super(attack, decay);
            this.lines = lines;
            this.speed = speed;
            this.width = width;
            this.whiteBar = whiteBar;
            this.tail = tail;

            min = new float[lines.size()];
            max = new float[lines.size()];
            Arrays.fill(min, Float.MAX_VALUE);
            Arrays.fill(max, Float.MIN_VALUE);
            for (int i = 0; i < lines.size(); i++) {
                for (TopoEdge e : lines.get(i)) {
                    min[i] = Float.min(e.minOrder(), min[i]);
                    max[i] = Float.max(e.maxOrder(), max[i]);
                }
            }

            if (!whiteBar) {
                maskPercentage = 0.9f;
                maskSpeedScale = 0.1f;
                barDelay = 100f;
            } else {
                maskPercentage = 1f;
                maskSpeedScale = 0f;
                barDelay = 0f;
            }
        }

        @Override
        protected boolean applyColors(float alpha) {
            int bar, field;
            float h, barS, fieldS, barB, fieldB, fieldAlpha;

            /* store the HSB values directly so we can interpolate without having
             * to reverse-engineer them from bar and field */
            if (whiteBar) {
                h = 0;
                barS = 0;
                fieldS = 0;
                barB = 100;
                fieldB = 100;
                fieldAlpha = 0.f;
                bar = LXColor.rgba(255, 255, 255, (int) (255 * alpha));
                field = LXColor.rgba(255,255,255,0);
            } else {
                h = palette.color.hue.getValuef();
                barS = palette.color.saturation.getValuef();
                fieldS = 0;
                barB = palette.color.brightness.getValuef();
                fieldB = 100;
                fieldAlpha = alpha;
                bar = LXColor.hsba(h, barS, barB, alpha);
                field = LXColor.rgba(255, 255, 255, (int) (255 * alpha));
            }

            for (int i = 0; i < lines.size(); i++) {
                float barOff = Math.max(0, age - barDelay) / 1000.f * speed;
                float bandHi, bandLo;
                float maskHi, maskLo;
                float maskWidth = maskPercentage * (max[i] - min[i]);
                float maskOff = age / 1000.f * maskSpeedScale * speed;

                if (speed < 0) {
                    bandLo = max[i] + barOff;
                    bandHi = bandLo + width;
                    maskHi = max[i] + maskOff;
                    maskLo = maskHi - maskWidth;
                } else {
                    bandHi = min[i] + barOff;
                    bandLo = bandHi - width;
                    maskLo = min[i] + maskOff;
                    maskHi = maskLo + maskWidth;
                }


                for (TopoEdge e : lines.get(i)) {
                    for (LXVector p : getVectors(model.getStripByIndex(e.i).points)) {
                        float v = Float.MIN_VALUE;
                        switch (e.dir) {
                            case X: v = p.x; break;
                            case Y: v = p.y; break;
                            case Z: v = p.z; break;
                        }

                        boolean inMask = maskLo < v && v < maskHi;
                        if (!inMask)
                            continue;

                        boolean inBand = bandLo < v && v < bandHi;
                        if (inBand)
                            colors[p.index] = Ops8.add(colors[p.index], bar);
                        else if (speed < 0 && v < bandLo)
                            colors[p.index] = Ops8.add(colors[p.index], field);
                        else if (speed > 0 && v > bandHi)
                            colors[p.index] = Ops8.add(colors[p.index], field);
                        else {
                            float dist = speed > 0 ? bandLo - v : v - bandHi;
                            dist = MathUtils.constrain(dist / tail, 0.f,1.f);
                            int c = LXColor.hsba(
                                h,
                                (1.f - dist) * barS + dist * fieldS,
                                (1.f - dist) * barB + dist * fieldB,
                                (1.f - dist) * alpha + dist * fieldAlpha);
                            colors[p.index] = Ops8.add(colors[p.index], c);
                        }
                    }
                }
            }
            return true;
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
                newEffect = createScrollingVerticalLines(true, true);
                break;
            case 64:
                newEffect = createScrollingVerticalLines(false, true);
                break;
            case 65:
                newEffect = createScrollingVerticalLines(true, false);
                break;
            case 67:
                newEffect = createScrollingVerticalLines(false, false);
                break;

            case 72:
                newEffect = createStaticHorizontalLines();
                break;
            case 74:
                newEffect = createScrollingHorizontalLines(true, true);
                break;
            case 76:
                newEffect = createScrollingHorizontalLines(false, true);
                break;
            case 77:
                newEffect = createScrollingHorizontalLines(true, false);
                break;
            case 79:
                newEffect = createScrollingHorizontalLines(false, false);
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
            createLineSet(EdgeDirection.X, hCountParam.getValuei(), hLengthParam.getValuei()));
    }

    private ArrayList<TopoEdge> createVerticalLines() {
        return new ArrayList<>(
            createLineSet(EdgeDirection.Y, vCountParam.getValuei(), vLengthParam.getValuei()));
    }

    private LineEffect createStaticVerticalLines() {
        return new StaticEdgeSet(
            createVerticalLines(), attackParam.getValuef(), decayParam.getValuef());
    }

    private LineEffect createStaticHorizontalLines() {
        return new StaticEdgeSet(
            createHorizontalLines(), attackParam.getValuef(), decayParam.getValuef());
    }

    private LineEffect createScrollingVerticalLines(boolean up, boolean whiteBar) {
        int N = vCountParam.getValuei();
        List<List<TopoEdge>> lines = new ArrayList<>(N);
        for (int i = 0; i < N; i++) {
            lines.add(randomLineSeg(EdgeDirection.Y, vLengthParam.getValuei()));
        }
        return new ScrollerEdgeSet(
            lines,
            attackParam.getValuef(),
            decayParam.getValuef(),
            (up ? 1 : -1) * speedParam.getValuef(),
            widthParam.getValuef(),
            tailParam.getValuef(),
            whiteBar);
    }

    private LineEffect createScrollingHorizontalLines(boolean right, boolean whiteBar) {
        int N = hCountParam.getValuei();
        List<List<TopoEdge>> lines = new ArrayList<>(N);
        for (int i = 0; i < N; i++) {
            lines.add(randomLineSeg(EdgeDirection.X, hLengthParam.getValuei()));
        }
        return new ScrollerEdgeSet(
            lines,
            attackParam.getValuef(),
            decayParam.getValuef(),
            (right ? 1 : -1) * speedParam.getValuef(),
            widthParam.getValuef(),
            tailParam.getValuef(),
            whiteBar);
    }
}
