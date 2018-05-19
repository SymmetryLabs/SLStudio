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
    public final BoundedParameter redGammaFactor = new BoundedParameter("RGMA", 2, 1, 4);
    public final BoundedParameter greenGammaFactor = new BoundedParameter("GGMA", 2.2, 1, 4);
    public final BoundedParameter blueGammaFactor = new BoundedParameter("BGMA", 2.8, 1, 4);

    private final int redGamma[] = new int[256];
    private final int greenGamma[] = new int[256];
    private final int blueGamma[] = new int[256];
    private final int redGamma16[] = new int[65536];
    private final int greenGamma16[] = new int[65536];
    private final int blueGamma16[] = new int[65536];

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

        initialize();

        lx.engine.registerComponent("GammaExpander", this);
    }

    public int getExpandedColor(int c) {
        if (!enabled.isOn())
            return c;

        int r = Ops8.red(c);
        int g = Ops8.green(c);
        int b = Ops8.blue(c);
        int a = Ops8.alpha(c);
        return Ops8.rgba(redGamma[r], greenGamma[g], blueGamma[b], a);
    }

    public long getExpandedColor16(long c) {
        if (!enabled.isOn())
            return c;

        int r = Ops16.red(c);
        int g = Ops16.green(c);
        int b = Ops16.blue(c);
        int a = Ops16.alpha(c);
        return Ops16.rgba(redGamma16[r], greenGamma16[g], blueGamma16[b], a);
    }

    public int getExpandedRed(int c) {
        int r = Ops8.red(c);
        return enabled.isOn() ? redGamma[r] : r;
    }

    public int getExpandedGreen(int c) {
        int g = Ops8.green(c);
        return enabled.isOn() ? greenGamma[g] : g;
    }

    public int getExpandedBlue(int c) {
        int b = Ops8.blue(c);
        return enabled.isOn() ? blueGamma[b] : b;
    }

    private void initialize() {
        final float redGammaOrig = redGammaFactor.getValuef();
        final float greenGammaOrig = greenGammaFactor.getValuef();
        final float blueGammaOrig = blueGammaFactor.getValuef();

        gammaSetIndex.addListener(param -> {
            if (gammaSetIndex.getValuei() == 0) {
                redGammaFactor.reset(redGammaOrig);
                greenGammaFactor.reset(greenGammaOrig);
                blueGammaFactor.reset(blueGammaOrig);
            } else {
                redGammaFactor.reset(gammaSet[gammaSetIndex.getValuei() - 1][0]);
                greenGammaFactor.reset(gammaSet[gammaSetIndex.getValuei() - 1][1]);
                blueGammaFactor.reset(gammaSet[gammaSetIndex.getValuei() - 1][2]);
            }
        });

        redGammaFactor.addListener(param -> {
            prepareGammaTables(redGamma, redGamma16, param.getValuef());
        });
        prepareGammaTables(redGamma, redGamma16, redGammaFactor.getValuef());

        greenGammaFactor.addListener(param -> {
            prepareGammaTables(greenGamma, greenGamma16, param.getValuef());
        });
        prepareGammaTables(greenGamma, greenGamma16, greenGammaFactor.getValuef());

        blueGammaFactor.addListener(param -> {
            prepareGammaTables(blueGamma, blueGamma16, param.getValuef());
        });
        prepareGammaTables(blueGamma, blueGamma16, blueGammaFactor.getValuef());
    }

    private void prepareGammaTables(int[] gammaTable8, int[] gammaTable16, float gamma) {
        for (int i = 0; i < 256; i++) {
            gammaTable8[i] = (int) (FastMath.pow(i / 255.0, gamma) * 255 + 0.5);
        }
        for (int i = 0; i < 65536; i++) {
            gammaTable16[i] = (int) (FastMath.pow(i / 65535.0, gamma) * 65335 + 0.5);
        }
    }
}
