package com.symmetrylabs.slstudio.component;

import com.symmetrylabs.color.Ops16;
import com.symmetrylabs.color.Ops8;
import heronarts.lx.LX;
import heronarts.lx.LXComponent;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.DiscreteParameter;
import org.apache.commons.math3.util.FastMath;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

public class GammaExpander extends LXComponent {

    private final float[][] gammaSet = {
        {2, 2.1f, 2.8f},
        {2, 2.2f, 2.8f},
    };

    public final BooleanParameter enabled = new BooleanParameter("ON", true);
    public final DiscreteParameter gammaSetIndex = new DiscreteParameter("GMA", gammaSet.length + 1);
    public final BoundedParameter redGammaFactor = new BoundedParameter("RGMA", 2, 0.1, 4);
    public final BoundedParameter greenGammaFactor = new BoundedParameter("GGMA", 2.2, 0.1, 4);
    public final BoundedParameter blueGammaFactor = new BoundedParameter("BGMA", 2.8, 0.1, 4);

    private static Map<LX, WeakReference<GammaExpander>> instanceByLX = new WeakHashMap<>();

    public static synchronized GammaExpander getInstance(LX lx) {
        WeakReference<GammaExpander> weakRef = instanceByLX.get(lx);
        GammaExpander ref = weakRef == null ? null : weakRef.get();
        if (ref == null) {
            instanceByLX.put(lx, new WeakReference<>(ref = new GammaExpander(lx)));
        }
        return ref;
    }

    private GammaExpander(LX lx) {
        super(lx, "GammaExpander");

        addParameter(enabled);
        addParameter(gammaSetIndex);
        addParameter(redGammaFactor);
        addParameter(greenGammaFactor);
        addParameter(blueGammaFactor);

        lx.engine.registerComponent("GammaExpander", this);
    }

    public int getExpandedColor(int c) {
        return c;
    }

    public long getExpandedColor16(long c) {
        return c;
    }

    public int getExpandedRed(int c) {
        int r = Ops8.red(c);
        return r;
    }

    public int getExpandedGreen(int c) {
        int g = Ops8.green(c);
        return g;
    }

    public int getExpandedBlue(int c) {
        int b = Ops8.blue(c);
        return b;
    }
}
