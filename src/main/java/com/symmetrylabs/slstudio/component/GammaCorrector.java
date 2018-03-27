package com.symmetrylabs.slstudio.component;

import java.util.Map;
import java.util.WeakHashMap;
import java.lang.ref.WeakReference;

import org.apache.commons.math3.util.FastMath;

import heronarts.lx.LX;
import heronarts.lx.LXComponent;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.BooleanParameter;

public class GammaCorrector extends LXComponent {

    private final float[][] gammaSet = {
        {2, 2.1f, 2.8f},
        {2, 2.2f, 2.8f},
    };

    public final BooleanParameter enabled = new BooleanParameter("ON", true);
    public final DiscreteParameter gammaSetIndex = new DiscreteParameter("GMA", gammaSet.length + 1);
    public final BoundedParameter redGammaFactor = new BoundedParameter("RGMA", 2, 1, 4);
    public final BoundedParameter greenGammaFactor = new BoundedParameter("GGMA", 2.2, 1, 4);
    public final BoundedParameter blueGammaFactor = new BoundedParameter("BGMA", 2.8, 1, 4);

    private final byte redGamma[] = new byte[256];
    private final byte greenGamma[] = new byte[256];
    private final byte blueGamma[] = new byte[256];

    private static Map<LX, WeakReference<GammaCorrector>> instanceByLX = new WeakHashMap<>();

    public static synchronized GammaCorrector getInstance(LX lx) {
        WeakReference<GammaCorrector> weakRef = instanceByLX.get(lx);
        GammaCorrector ref = weakRef == null ? null : weakRef.get();
        if (ref == null) {
            instanceByLX.put(lx, new WeakReference<>(ref = new GammaCorrector(lx)));
        }
        return ref;
    }

    private GammaCorrector(LX lx) {
        super(lx, "GammaCorrector");

        addParameter(enabled);
        addParameter(gammaSetIndex);
        addParameter(redGammaFactor);
        addParameter(greenGammaFactor);
        addParameter(blueGammaFactor);

        initialize();

        lx.engine.registerComponent("gammaCorrector", this);
    }

    public int getCorrectedColor(int c) {
        if (!enabled.isOn())
            return c;

        int r = c >> 16 & 0xFF;
        int g = c >> 8 & 0xFF;
        int b = c & 0xFF;

        int alpha = LXColor.alpha(c);
        int red = redGamma[r] & 0xFF;
        int green = greenGamma[g] & 0xFF;
        int blue = blueGamma[b] & 0xFF;

        return alpha << LXColor.ALPHA_SHIFT | red << LXColor.RED_SHIFT
                | green << LXColor.GREEN_SHIFT | blue;
    }

    public byte getCorrectedRed(int c) {
        byte r = (byte)(c >> 16 & 0xFF);

        if (!enabled.isOn())
            return r;

        return redGamma[r];
    }

    public byte getCorrectedGreen(int c) {
        byte g = (byte)(c >> 8 & 0xFF);

        if (!enabled.isOn())
            return g;

        return greenGamma[g];
    }

    public byte getCorrectedBlue(int c) {
        byte b = (byte)(c & 0xFF);

        if (!enabled.isOn())
            return b;

        return blueGamma[b];
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
            prepareGammaTable(redGamma, param.getValuef());
        });
        prepareGammaTable(redGamma, redGammaFactor.getValuef());

        greenGammaFactor.addListener(param -> {
            prepareGammaTable(greenGamma, param.getValuef());
        });
        prepareGammaTable(greenGamma, greenGammaFactor.getValuef());

        blueGammaFactor.addListener(param -> {
            prepareGammaTable(blueGamma, param.getValuef());
        });
        prepareGammaTable(blueGamma, blueGammaFactor.getValuef());
    }

    private void prepareGammaTable(byte[] gammaTable, float gamma) {
        for (int i = 0; i < 256; i++) {
            gammaTable[i] = (byte)(FastMath.pow(1.0f * i / 255f, gamma) * 255f + 0.5f);
        }
    }
}
