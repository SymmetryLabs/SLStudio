package com.symmetrylabs.slstudio.pattern;

import com.symmetrylabs.color.Ops16;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.EnumParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.transform.LXVector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Crystalline extends SLPattern<SLModel> {
    private static final int CANDIDATE_COUNT = 500;

    public enum SymmetryMode {
        NONE,
        POISSON,
        TETRAHEDRAL,
        OCTAHEDRAL,
        ICOSAHEDRAL,
        FORWARD,
        UP,
    }

    public enum RadialMode {
        NONE,
        SPHERE,
        CYLINDER,
    }

    /** Moduli divide space into a periodic pattern based on the projection
     *  of a point onto the underlying structure of the modulus. For example
     *  a linear modulus would project each point onto a line, making linear
     *  stripes. */
    private abstract class Modulus {
        float d;

        Modulus() {
            d = 0;
        }

        void step(double tms) {
            d += velocity() * tms / 1000f;
        }

        float eval(LXVector v) {
            float w = width();
            float x = projection(v) + w * d;
            return (x % w) / w + (x < 0 ? 1 : 0);
        }

        /** Returns the velocity of the modulus. Velocity is expressed as the
         *  number of widths moved forward each second. */
        abstract float velocity();

        /** Returns the divisor of the modular space we're operating in, which
         *  is also double the width of each stripe. */
        abstract float width();

        /** Returns the projection of the given vector onto the structure. This
         *  method operates on the structure without modularity; the eval method
         *  takes care of making the whole structure periodic over the width of
         *  the space. */
        abstract float projection(LXVector v);
    }

    /** A linear modulus that makes linear stripes */
    private class LinearModulus extends Modulus {
        final LXVector n;
        final float lat;
        final float lon;

        LinearModulus(float lat, float lon) {
            this.lat = lat;
            this.lon = lon;
            this.d = 0;
            this.n = new LXVector(
                (float) (Math.sin(lat) * Math.cos(lon)),
                (float) (Math.sin(lat) * Math.sin(lon)),
                (float) Math.cos(lat));
        }

        LinearModulus(LXVector n) {
            this.n = n.copy().normalize();
            this.lat = -1;
            this.lon = -1;
        }

        float angDist(LinearModulus p) {
            if (lat == -1 && lon == -1) {
                throw new IllegalStateException("can't find angular distance without lat/lon");
            }
            /* this is the formula for great-circle distance */
            return (float) Math.acos(
                Math.sin(lat) * Math.sin(p.lat) + Math.cos(lon) * Math.cos(p.lon) * Math.cos(Math.abs(lon - p.lon)));
        }

        @Override
        float velocity() {
            return linearVelocity.getValuef();
        }

        @Override
        float width() {
            return width.getValuef();
        }

        @Override
        float projection(LXVector v) {
            return v.dot(n);
        }
    }

    /** A spherical modulus that makes spherical shells */
    private class SphericalModulus extends Modulus {
        @Override
        float velocity() {
            return radialVelocity.getValuef();
        }

        @Override
        float width() {
            return radialWidth.getValuef();
        }

        @Override
        float projection(LXVector v) {
            return v.mag();
        }
    }

    /** A cylindrical modulus that makes cylindrical shells */
    private class CylindricalModulus extends Modulus {
        LXVector n = new LXVector(0, 0, 1);
        @Override
        float velocity() {
            return radialVelocity.getValuef();
        }

        @Override
        float width() {
            return radialWidth.getValuef();
        }

        @Override
        float projection(LXVector v) {
            float nd = v.dot(n);
            return n.copy().mult(-nd).add(v).mag();
        }
    }

    private final DiscreteParameter count = new DiscreteParameter("count", 3, 0, 40);
    private final CompoundParameter width = new CompoundParameter("width", 260, 800);
    private final CompoundParameter cx = new CompoundParameter("cx", model.cx, model.xMin, model.xMax);
    private final CompoundParameter cy = new CompoundParameter("cy", model.cy, model.yMin, model.yMax);
    private final CompoundParameter cz = new CompoundParameter("cz", model.cz, model.zMin, model.zMax);
    private final CompoundParameter linearVelocity = new CompoundParameter("lvel", 0.02, -4, 4);
    private final CompoundParameter cutWhite = new CompoundParameter("cutwhite", 0.1, 0, 1);
    private final CompoundParameter radialWidth = new CompoundParameter("rwidth", 60, 1, 800);
    private final CompoundParameter radialVelocity = new CompoundParameter("rvel", 0.1, -4, 4);
    private final BooleanParameter reset = new BooleanParameter("reset", false).setMode(BooleanParameter.Mode.MOMENTARY);
    private final EnumParameter<SymmetryMode> symmetryMode = new EnumParameter<>("symm", SymmetryMode.TETRAHEDRAL);
    private final EnumParameter<RadialMode> radialMode = new EnumParameter<>("radial", RadialMode.NONE);
    private final BooleanParameter alphaBg = new BooleanParameter("alpha", true);
    private final Random random = new Random();

    private final List<Modulus> moduli = new ArrayList<>();

    public Crystalline(LX lx) {
        super(lx);
        addParameter(count);
        addParameter(cx);
        addParameter(cy);
        addParameter(cz);
        addParameter(width);
        addParameter(linearVelocity);
        addParameter(radialWidth);
        addParameter(radialVelocity);
        addParameter(cutWhite);
        addParameter(symmetryMode);
        addParameter(radialMode);
        addParameter(alphaBg);
        addParameter(reset);
        reset.setShouldSerialize(false);
        refillDirs();
    }

    @Override
    public String getCaption() {
        return String.format("%d symmetry directions", moduli.size());
    }

    @Override
    public void onParameterChanged(LXParameter p) {
        if (p == count || p == symmetryMode || p == radialMode) {
            refillDirs();
        } else if (p == reset) {
            moduli.clear();
            refillDirs();
        }
    }

    private boolean antipodalDirectionExists(LXVector v) {
        LXVector nv = v.copy().normalize();
        for (Modulus m : moduli) {
            if (m instanceof LinearModulus) {
                LinearModulus d = (LinearModulus) m;
                if (nv.copy().add(d.n).magSq() < 1e-1) {
                    return true;
                }
            }
        }
        return false;
    }

    private void refillDirs() {
        moduli.clear();

        switch (radialMode.getEnum()) {
            case NONE:
                break;

            case SPHERE:
                moduli.add(new SphericalModulus());
                break;

            case CYLINDER:
                moduli.add(new CylindricalModulus());
                break;
        }

        switch (symmetryMode.getEnum()) {
            case NONE:
                break;

            case POISSON:
                poissonSampleDirs(count.getValuei());
                break;

            case TETRAHEDRAL:
                moduli.add(new LinearModulus(new LXVector(8, 0, -1)));
                moduli.add(new LinearModulus(new LXVector(-2, 4, -1)));
                moduli.add(new LinearModulus(new LXVector(-2, -4, -1)));
                moduli.add(new LinearModulus(new LXVector(0, 0, 1)));
                break;

            case OCTAHEDRAL:
                moduli.add(new LinearModulus(new LXVector(1, 0, 0)));
                moduli.add(new LinearModulus(new LXVector(0, 1, 0)));
                moduli.add(new LinearModulus(new LXVector(0, 0, 1)));
                break;

            case ICOSAHEDRAL:
                double phi = (1 + Math.sqrt(5)) / 2;
                for (double y : new double[]{-1, 1}) {
                    for (double z : new double[]{-phi, phi}) {
                        LXVector x = new LXVector(0f, (float) y, (float) z);
                        if (!antipodalDirectionExists(x)) {
                            moduli.add(new LinearModulus(x));
                        }

                        x = new LXVector((float) z, 0f, (float) y);
                        if (!antipodalDirectionExists(x)) {
                            moduli.add(new LinearModulus(x));
                        }

                        x = new LXVector((float) y, (float) z, 0f);
                        if (!antipodalDirectionExists(x)) {
                            moduli.add(new LinearModulus(x));
                        }
                    }
                }
                break;

            case FORWARD:
                moduli.add(new LinearModulus(new LXVector(0, 0, 1)));
                break;

            case UP:
                moduli.add(new LinearModulus(new LXVector(0, 1, 0)));
                break;
        }
    }

    private void poissonSampleDirs(int c) {
        /* We don't build this directly in the moduli list because that
         * might have other kinds of moduli in it already. */
        List<LinearModulus> chosen = new ArrayList<>();

        while (chosen.size() < c) {
            List<LinearModulus> candidates = new ArrayList<>();
            for (int i = 0; i < CANDIDATE_COUNT; i++) {
                candidates.add(new LinearModulus(
                    180f * random.nextFloat(),
                    360f * random.nextFloat()));
            }
            float maxDist = 0;
            LinearModulus best = candidates.get(0);
            for (LinearModulus candidate : candidates) {
                for (LinearModulus dir : chosen) {
                    float dist = candidate.angDist(dir);
                    if (dist > maxDist) {
                        best = candidate;
                        maxDist = dist;
                    }
                }
            }
            chosen.add(best);
        }

        moduli.addAll(chosen);
    }

    @Override
    public void run(double elapsedMs, PolyBuffer.Space preferredSpace) {
        long[] colors = (long[]) getArray(PolyBuffer.Space.RGB16);

        for (Modulus m : moduli) {
            m.step(elapsedMs);
        }

        final long on = 0xFFFF_FFFF_FFFF_FFFFL;
        final long off = alphaBg.getValueb() ? 0 : Ops16.rgba(0, 0, 0, 0xFFFF);
        Arrays.fill(colors, off);

        final float cw = cutWhite.getValuef();

        LXVector negCenter = new LXVector(-cx.getValuef(), -cy.getValuef(), -cz.getValuef());
        for (LXVector v : getVectors()) {
            v = v.copy().add(negCenter);
            boolean flip = false;
            float min = 1;
            for (Modulus m : moduli) {
                float proj = m.eval(v);
                min = Float.min(min, proj);
                if (proj < 0.5) {
                    flip = !flip;
                }
            }
            if (!flip) {
                colors[v.index] = off;
            } else {
                float x = 2 * min;
                if (x < cw) {
                    colors[v.index] = on;
                } else {
                    float g = 1f - (x - cw) / (1 - cw);
                    int gi = (int) (g * 0xFFFF);
                    colors[v.index] = Ops16.rgba(gi, gi, gi, 0xFFFF);
                }
            }
        }

        markModified(PolyBuffer.Space.RGB16);
    }
}
