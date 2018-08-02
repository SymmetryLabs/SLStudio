package com.symmetrylabs.layouts.cubes.patterns;

import com.symmetrylabs.color.Ops8;
import com.symmetrylabs.layouts.cubes.CubesModel;
import com.symmetrylabs.slstudio.model.StripsModel;
import com.symmetrylabs.slstudio.model.StripsTopology;
import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import com.symmetrylabs.util.MathUtils;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.midi.*;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.transform.LXVector;

import java.util.*;

public class PilotsLines<T extends Strip> extends SLPattern<StripsModel<T>> {
    private static final float MIRROR_ENDPOINT_TOLERANCE = 6; // inches

    private BooleanParameter vertParam = new BooleanParameter("vert");
    private BooleanParameter xMirrorParam = new BooleanParameter("xmirror");
    private BooleanParameter yMirrorParam = new BooleanParameter("ymirror");
    private BooleanParameter zMirrorParam = new BooleanParameter("zmirror");

    private CompoundParameter attackParam = new CompoundParameter("attack", 60, 0, 600);
    private CompoundParameter colorDelayParam = new CompoundParameter("cdelay", 0, 0, 500);
    private CompoundParameter colorSpeedParam = new CompoundParameter("cspeed", 400, 0, 800);
    private CompoundParameter colorTailParam = new CompoundParameter("tail", 30, 0, 120);
    private CompoundParameter colorWidthParam = new CompoundParameter("cwidth", 20, 0, 200);
    private CompoundParameter decayParam = new CompoundParameter("decay", 300, 0, 2000);
    private CompoundParameter speedParam = new CompoundParameter("speed", 200, 0, 800);
    private CompoundParameter widthParam = new CompoundParameter("mask", 0.7, 0, 1);
    private DiscreteParameter hCountParam = new DiscreteParameter("hcount", 40, 1, 100);
    private DiscreteParameter hLengthParam = new DiscreteParameter("hlen", 8, 0, 20);
    private DiscreteParameter vCountParam = new DiscreteParameter("vcount", 30, 1, 100);
    private DiscreteParameter vLengthParam = new DiscreteParameter("vlen", 4, 0, 20);

    /* We can't use a mirror warp on this pattern because we're need a topological
     * mirror, not a geometric mirror. This caches the mirror image of the left side
     * onto the right side, so that we don't have to calculate the mirror of each
     * strip independently */
    private HashMap<StripsTopology.Bundle, StripsTopology.Bundle> xMirrorMap = new HashMap<>();
    private HashMap<StripsTopology.Bundle, StripsTopology.Bundle> yMirrorMap = new HashMap<>();
    private HashMap<StripsTopology.Bundle, StripsTopology.Bundle> zMirrorMap = new HashMap<>();

    public PilotsLines(LX lx) {
        super(lx);

        addParameter(vertParam);
        addParameter(xMirrorParam);
        addParameter(yMirrorParam);
        addParameter(zMirrorParam);

        addParameter(hCountParam);
        addParameter(vCountParam);
        addParameter(hLengthParam);
        addParameter(vLengthParam);

        addParameter(attackParam);
        addParameter(decayParam);

        addParameter(widthParam);
        addParameter(speedParam);

        addParameter(colorDelayParam);
        addParameter(colorSpeedParam);
        addParameter(colorTailParam);
        addParameter(colorWidthParam);

        for (StripsTopology.Bundle b : model.getTopology().edges) {
            LXVector v = new LXVector(b.endpoints().start);
            LXVector end = new LXVector(b.endpoints().end);
            LXVector delta = v.copy().mult(-1).add(end); // delta = end - v

            LXVector xmirror = v.copy();
            xmirror.x += 2 * (model.cx - v.x) - delta.x;

            LXVector ymirror = v.copy();
            ymirror.y += 2 * (model.cy - v.y) - delta.y;

            LXVector zmirror = v.copy();
            ymirror.z += 2 * (model.cz - v.z) - delta.z;

            boolean foundX = false, foundY = false, foundZ = false;
            for (StripsTopology.Bundle maybeMirror : model.getTopology().edges) {
                if (maybeMirror.dir != b.dir)
                    continue;
                LXVector mm = new LXVector(maybeMirror.endpoints().start);
                if (xmirror.dist(mm) < MIRROR_ENDPOINT_TOLERANCE) {
                    xMirrorMap.put(b, maybeMirror);
                    foundX = true;
                }
                if (ymirror.dist(mm) < MIRROR_ENDPOINT_TOLERANCE) {
                    yMirrorMap.put(b, maybeMirror);
                    foundY = true;
                }
                if (zmirror.dist(mm) < MIRROR_ENDPOINT_TOLERANCE) {
                    zMirrorMap.put(b, maybeMirror);
                    foundZ = true;
                }
                if (foundX && foundY && foundZ)
                    break;
            }
        }
        System.out.println(String.format("%d elements in symmetry map", xMirrorMap.size()));
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
        final List<StripsTopology.Bundle> edges;
        public StaticEdgeSet(List<StripsTopology.Bundle> edges, float attack, float decay) {
            super(attack, decay);
            this.edges = edges;
        }

        private void turnOnBundle(StripsTopology.Bundle b, int c) {
            if (b == null)
                return;
            for (int stripIndex : b.strips) {
                Strip s = model.getStripByIndex(stripIndex);
                for (LXVector p : getVectors(s.points)) {
                    colors[p.index] = Ops8.add(colors[p.index], c);
                }
            }
        }

        @Override
        protected boolean applyColors(float alpha) {
            int c = LXColor.hsba(0, 0, 100, alpha);
            for (StripsTopology.Bundle e : edges) {
                turnOnBundle(e, c);

                if (xMirrorParam.getValueb())
                    turnOnBundle(xMirrorMap.getOrDefault(e,null), c);
                if (yMirrorParam.getValueb())
                    turnOnBundle(yMirrorMap.getOrDefault(e,null), c);
                if (zMirrorParam.getValueb())
                    turnOnBundle(zMirrorMap.getOrDefault(e,null), c);
            }
            return true;
        }
    }

    private class ScrollerEdgeSet extends LineEffect {
        final List<List<StripsTopology.Bundle>> lines;
        final float speed;
        final float[] min;
        final float[] max;
        /* if zero, the band of color isn't displayed */
        final float bandWidth;
        final float bandSpeed;
        /* the length of the fade-out on the end of the band. If zero, has a hard end */
        final float bandTail;
        final float maskPercentage;
        final float bandDelay;

        public ScrollerEdgeSet(
            List<List<StripsTopology.Bundle>> lines, float attack, float decay, float speed, float bandWidth,
            float bandTail, float bandSpeed, float bandDelay, float maskPercentage) {
            super(attack, decay);
            this.lines = lines;
            this.speed = speed;
            this.bandWidth = bandWidth;
            this.bandSpeed = bandSpeed;
            this.bandDelay = bandDelay;
            this.maskPercentage = maskPercentage;
            this.bandTail = bandTail;

            min = new float[lines.size()];
            max = new float[lines.size()];
            Arrays.fill(min, Float.MAX_VALUE);
            Arrays.fill(max, Float.MIN_VALUE);
            for (int i = 0; i < lines.size(); i++) {
                for (StripsTopology.Bundle e : lines.get(i)) {
                    min[i] = Float.min(e.minOrder(), min[i]);
                    max[i] = Float.max(e.maxOrder(), max[i]);
                }
            }
        }

        @Override
        protected boolean applyColors(float alpha) {
            int band, field;
            float h, bandS, fieldS, bandB, fieldB, fieldAlpha;
            boolean showBand = bandWidth != 0f;

            /* store the HSB values directly so we can interpolate without having
             * to reverse-engineer them from band and field */
            h = palette.color.hue.getValuef();
            bandS = palette.color.saturation.getValuef();
            fieldS = 0;
            bandB = palette.color.brightness.getValuef();
            fieldB = 100;
            fieldAlpha = alpha;
            band = LXColor.hsba(h, bandS, bandB, alpha);
            field = LXColor.rgba(255, 255, 255, (int) (255 * alpha));

            for (int i = 0; i < lines.size(); i++) {
                float maskHi, maskLo;
                float maskWidth = maskPercentage * (max[i] - min[i]);
                float maskOff = age / 1000.f * speed;
                if (speed < 0) {
                    maskHi = max[i] + maskOff;
                    maskLo = maskHi - maskWidth;
                } else {
                    maskLo = min[i] + maskOff;
                    maskHi = maskLo + maskWidth;
                }

                float bandOff = Math.max(0, age - bandDelay) / 1000.f * bandSpeed;
                float bandHi, bandLo;
                if (bandSpeed < 0) {
                    bandLo = max[i] + bandOff;
                    bandHi = bandLo + bandWidth;
                } else {
                    bandHi = min[i] + bandOff;
                    bandLo = bandHi - bandWidth;
                }

                for (StripsTopology.Bundle e : lines.get(i)) {
                    // pick a strip that's likely to be in the front of the bundle
                    int stripIndex = e.strips.length >= 2 ? 1 : 0;
                    for (LXVector p : getVectors(model.getStripByIndex(e.strips[stripIndex]).points)) {
                        float v = Float.MIN_VALUE;
                        switch (e.dir) {
                            case X: v = p.x; break;
                            case Y: v = p.y; break;
                            case Z: v = p.z; break;
                        }

                        boolean inMask = maskLo < v && v < maskHi;
                        if (!inMask)
                            continue;
                        if (!showBand) {
                            colors[p.index] = Ops8.add(colors[p.index], field);
                            continue;
                        }

                        boolean inBand = bandLo < v && v < bandHi;
                        if (inBand)
                            colors[p.index] = Ops8.add(colors[p.index], band);
                        else if (bandSpeed < 0 && v < bandLo)
                            colors[p.index] = Ops8.add(colors[p.index], field);
                        else if (bandSpeed > 0 && v > bandHi)
                            colors[p.index] = Ops8.add(colors[p.index], field);
                        else {
                            float dist = bandSpeed > 0 ? bandLo - v : v - bandHi;
                            dist = MathUtils.constrain(dist / bandTail, 0.f, 1.f);
                            int c = LXColor.hsba(
                                h,
                                (1.f - dist) * bandS + dist * fieldS,
                                (1.f - dist) * bandB + dist * fieldB,
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
                if (vertParam.getValueb())
                    newEffect = createStaticVerticalLines();
                else
                    newEffect = createStaticHorizontalLines();
                break;
            case 62:
                if (vertParam.getValueb())
                    newEffect = createScrollingVerticalLines(true, false);
                else
                    newEffect = createScrollingHorizontalLines(true, false);
                break;
            case 64:
                if (vertParam.getValueb())
                    newEffect = createScrollingVerticalLines(false, false);
                else
                    newEffect = createScrollingHorizontalLines(false, false);
                break;
            case 65:
                if (vertParam.getValueb())
                    newEffect = createScrollingVerticalLines(true, true);
                else
                    newEffect = createScrollingHorizontalLines(true, true);
                break;
            case 67:
                if (vertParam.getValueb())
                    newEffect = createScrollingVerticalLines(false, true);
                else
                    newEffect = createScrollingHorizontalLines(false, true);
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

    private List<StripsTopology.Bundle> randomLineSeg(StripsTopology.EdgeDirection d, int expectedLength) {
        int N = model.getTopology().edges.size();
        Random r = new Random();
        StripsTopology.Bundle e;
        do {
            e = model.getTopology().edges.get(r.nextInt(N));
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

        LinkedList<StripsTopology.Bundle> line = new LinkedList<>();
        line.add(e);
        while (line.size() < len) {
            boolean added = false;
            StripsTopology.Bundle t;

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

    private Set<StripsTopology.Bundle> createLineSet(StripsTopology.EdgeDirection d, int count, int expectedLength) {
        Set<StripsTopology.Bundle> all = new HashSet<>();
        for (int i = 0; i < count; i++) {
            all.addAll(randomLineSeg(d, expectedLength));
        }
        return all;
    }

    private ArrayList<StripsTopology.Bundle> createHorizontalLines() {
        return new ArrayList<>(
            createLineSet(StripsTopology.EdgeDirection.X, hCountParam.getValuei(), hLengthParam.getValuei()));
    }

    private ArrayList<StripsTopology.Bundle> createVerticalLines() {
        return new ArrayList<>(
            createLineSet(StripsTopology.EdgeDirection.Y, vCountParam.getValuei(), vLengthParam.getValuei()));
    }

    private LineEffect createStaticVerticalLines() {
        return new StaticEdgeSet(
            createVerticalLines(), attackParam.getValuef(), decayParam.getValuef());
    }

    private LineEffect createStaticHorizontalLines() {
        return new StaticEdgeSet(
            createHorizontalLines(), attackParam.getValuef(), decayParam.getValuef());
    }

    private LineEffect createScroller(List<List<StripsTopology.Bundle>> lines, boolean flipSpeed, boolean showBand) {
        return new ScrollerEdgeSet(
            lines,
            attackParam.getValuef(),
            decayParam.getValuef(),
            (flipSpeed ? -1 : 1) * speedParam.getValuef(),
            showBand ? colorWidthParam.getValuef() : 0,
            colorTailParam.getValuef(),
            (flipSpeed ? -1 : 1) * colorSpeedParam.getValuef(),
            colorDelayParam.getValuef(),
            widthParam.getValuef());
    }

    private LineEffect createScrollingVerticalLines(boolean up, boolean showBand) {
        int N = vCountParam.getValuei();
        List<List<StripsTopology.Bundle>> lines = new ArrayList<>(N);
        for (int i = 0; i < N; i++) {
            lines.add(randomLineSeg(StripsTopology.EdgeDirection.Y, vLengthParam.getValuei()));
        }
        return createScroller(lines, !up, showBand);
    }

    private LineEffect createScrollingHorizontalLines(boolean right, boolean showBand) {
        int N = hCountParam.getValuei();
        List<List<StripsTopology.Bundle>> lines = new ArrayList<>(N);
        for (int i = 0; i < N; i++) {
            lines.add(randomLineSeg(StripsTopology.EdgeDirection.X, hLengthParam.getValuei()));
        }
        return createScroller(lines, !right, showBand);
    }
}
