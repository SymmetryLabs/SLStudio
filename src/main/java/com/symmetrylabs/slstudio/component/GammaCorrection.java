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
import heronarts.lx.parameter.LXParameterListener;

public class GammaCorrection extends LXComponent {

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

    private static Map<LX, WeakReference<GammaCorrection>> instanceByLX = new WeakHashMap<>();

    public static synchronized GammaCorrection getInstance(LX lx) {
        WeakReference<GammaCorrection> weakRef = instanceByLX.get(lx);
        GammaCorrection ref = weakRef == null ? null : weakRef.get();
        if (ref == null) {
            instanceByLX.put(lx, new WeakReference<>(ref = new GammaCorrection(lx)));
        }
        return ref;
    }

    private GammaCorrection(LX lx) {
        super(lx, "GammaCorrection");

        addParameter(enabled);
        addParameter(gammaSetIndex);
        addParameter(redGammaFactor);
        addParameter(greenGammaFactor);
        addParameter(blueGammaFactor);

        setupGammaCorrection();

        lx.engine.registerComponent("gammaCorrection", this);
    }

    public int getCorrectedColor(int c) {
        if (!enabled.isOn())
            return c;

        return (int)redGamma[((c >> LXColor.RED_SHIFT) & 0xff)] << LXColor.RED_SHIFT +
                (int)greenGamma[((c >> LXColor.GREEN_SHIFT) & 0xff)] << LXColor.GREEN_SHIFT +
                (int)blueGamma[(c & 0xff)];
    }

    private void setupGammaCorrection() {
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
            buildGammaCorrection(redGamma, param.getValuef());
        });
        buildGammaCorrection(redGamma, redGammaFactor.getValuef());

        greenGammaFactor.addListener(param -> {
            buildGammaCorrection(greenGamma, param.getValuef());
        });
        buildGammaCorrection(greenGamma, greenGammaFactor.getValuef());

        blueGammaFactor.addListener(param -> {
            buildGammaCorrection(blueGamma, param.getValuef());
        });
        buildGammaCorrection(blueGamma, blueGammaFactor.getValuef());
    }

    private void buildGammaCorrection(byte[] gammaTable, float gammaCorrection) {
        for (int i = 0; i < 256; i++) {
            gammaTable[i] = (byte)(FastMath.pow(1.0f * i / 255f, gammaCorrection) * 255f + 0.5f);
        }
    }
}
