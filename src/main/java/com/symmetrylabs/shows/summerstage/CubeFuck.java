package com.symmetrylabs.shows.summerstage;

import com.symmetrylabs.color.Ops8;
import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;
import com.symmetrylabs.slstudio.model.StripsTopology.Bundle;
import com.symmetrylabs.slstudio.model.StripsTopology.Dir;
import com.symmetrylabs.slstudio.model.StripsTopology.Junction;
import com.symmetrylabs.slstudio.model.StripsTopology.Sign;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.transform.LXVector;

public class CubeFuck extends SLPattern<StripsModel<? extends Strip>> {
    CompoundParameter hueParam = new CompoundParameter("Hue", 0, -1, 1);
    CompoundParameter satParam = new CompoundParameter("Sat", 0, 0, 1);
    CompoundParameter xParam = new CompoundParameter("X", model.cx, model.xMin, model.xMax);
    CompoundParameter yParam = new CompoundParameter("Y", model.cy, model.yMin, model.yMax);
    CompoundParameter zParam = new CompoundParameter("Z", model.cz, model.zMin, model.zMax);

    BooleanParameter hold = new BooleanParameter("hold", false);
    List<Bundle> fuckBundles = new ArrayList<>();

    public CubeFuck(LX lx) {
        super(lx);

        addParameter(hueParam);
        addParameter(satParam);
        addParameter(xParam);
        addParameter(yParam);
        addParameter(zParam);
        addParameter(hold);

        fuckBundles = getFuckBundles(getClosestJunction(getPos()));
    }

    @Override
    public void onParameterChanged(LXParameter p) {
        if (p == xParam || p == yParam || p == zParam) {
            fuckBundles = getFuckBundles(getClosestJunction(getPos()));
        }
    }

    public LXVector getPos() {
        return new LXVector(xParam.getValuef(), yParam.getValuef(), zParam.getValuef());
    }

    public void run(double deltaMs, PolyBuffer.Space preferredSpace) {
        int[] colors = (int[]) getArray(PolyBuffer.Space.SRGB8);
        int color = LXColor.hsb(hueParam.getValuef() * 360f, satParam.getValuef() * 100f, 100f);

        Arrays.fill(colors, Ops8.BLACK);
        if (hold.isOn()) {
            for (Bundle b : fuckBundles) {
                for (int s : b.strips) {
                    for (LXPoint p : model.getStripByIndex(s).points) {
                        colors[p.index] = color;
                    }
                }
            }
        }

        markModified(PolyBuffer.Space.SRGB8);
    }

    public Junction getClosestJunction(LXVector pos) {
        Junction result = null;
        double minDist = Double.MAX_VALUE;

        for (Junction j : model.getTopology().junctions) {
            if (result == null) result = j;
            else {
                double dist = pos.dist(j.loc);
                if (dist < minDist) {
                    result = j;
                    minDist = dist;
                }
            }
        }
        return result;
    }

    public List<Bundle> getFuckBundles(Junction f0) {
        List<Bundle> bundles = new ArrayList<>();
        Junction f1 = addBundle(bundles, f0, Dir.Y, Sign.POS);
        addBundles(bundles, f1, Dir.X, Sign.POS, 2);
        Junction f2 = addBundles(bundles, f1, Dir.Y, Sign.POS, 2);
        addBundles(bundles, f2, Dir.X, Sign.POS, 2);

        Junction u0 = addBundles(null, f0, Dir.X, Sign.POS, 3);
        addBundles(bundles, u0, Dir.Y, Sign.POS, 3);
        Junction u1 = addBundles(bundles, u0, Dir.X, Sign.POS, 2);
        addBundles(bundles, u1, Dir.Y, Sign.POS, 3);

        Junction c0 = addBundles(null, u0, Dir.X, Sign.POS, 3);
        addBundles(bundles, c0, Dir.X, Sign.POS, 2);
        Junction c1 = addBundles(bundles, c0, Dir.Y, Sign.POS, 3);
        addBundles(bundles, c1, Dir.X, Sign.POS, 2);

        Junction k0 = addBundles(null, c0, Dir.X, Sign.POS, 3);
        Junction k1 = addBundles(bundles, k0, Dir.Y, Sign.POS, 1);
        addBundles(bundles, k1, Dir.Y, Sign.POS, 2);
        Junction k2 = addBundle(bundles, k1, Dir.X, Sign.POS);
        Junction k3 = addBundle(bundles, k2, Dir.X, Sign.POS);
        addBundle(bundles, k3, Dir.Y, Sign.NEG);
        Junction k4 = addBundle(bundles, k2, Dir.Y, Sign.POS);
        k4 = addBundle(bundles, k4, Dir.X, Sign.POS);
        k4 = addBundle(bundles, k4, Dir.Y, Sign.POS);

        return bundles;
    }

    public Junction addBundle(List<Bundle> bundles, Junction j, Dir dir, Sign sign) {
        if (j != null) {
            Bundle b = j.get(dir, sign);
            if (b != null) {
                if (bundles != null) bundles.add(b);
                return b.get(sign);
            }
        }
        return j;
    }

    public Junction addBundles(List<Bundle> bundles, Junction j, Dir dir, Sign sign, int count) {
        for (int i = 0; i < count; i++) {
            j = addBundle(bundles, j, dir, sign);
        }
        return j;
    }
}
