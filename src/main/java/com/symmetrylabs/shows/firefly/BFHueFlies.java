package com.symmetrylabs.shows.firefly;

import art.lookingup.KaledoscopeModel;
import art.lookingup.LUButterfly;
import art.lookingup.LUFlower;
import com.symmetrylabs.shows.firefly.BFBase;
import com.symmetrylabs.slstudio.palettes.PaletteLibrary;
import com.symmetrylabs.slstudio.palettes.ZigzagPalette;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;

public class BFHueFlies extends ColorPattern {
    public static final String GROUP_NAME = FireflyShow.SHOW_NAME;

    private final PaletteLibrary paletteLibrary = PaletteLibrary.getInstance();
    BooleanParameter doPoints = new BooleanParameter("points", true);
    BooleanParameter useGradientPal = new BooleanParameter("usegpal", false);
    DiscreteParameter gradpal = new DiscreteParameter("gradpal", paletteLibrary.getNames());
    // selected colour palette
    CompoundParameter palStart = new CompoundParameter("palStart", 0, 0, 1);  // palette start point (fraction 0 - 1)
    CompoundParameter palStop = new CompoundParameter("palStop", 1, 0, 1);  // palette stop point (fraction 0 - 1)
    CompoundParameter palBias = new CompoundParameter("palBias", 0, -6, 6);  // bias colour palette toward zero (dB)
    CompoundParameter palShift = new CompoundParameter("palShift", 0, -1, 1);  // shift in colour palette (fraction 0 - 1)
    CompoundParameter palCutoff = new CompoundParameter("palCutoff", 0, 0, 1);  // palette value cutoff (fraction 0 - 1)
    ZigzagPalette pal = new ZigzagPalette();

    public BFHueFlies(LX lx) {
        super(lx);
        addParameter(fpsKnob);
        addParameter(doPoints);
        addParameter(paletteKnob);
        addParameter(randomPaletteKnob);
        addParameter(hue);
        addParameter(saturation);
        addParameter(bright);
        addParameter(useGradientPal);
        addParameter(gradpal);
        addParameter(palStart);
        addParameter(palStop);
        addParameter(palShift);
        addParameter(palBias);
        addParameter(palCutoff);
        gradpal.setValue(3);
        fpsKnob.setValue(5);
    }


    public void renderFrame(double drawDeltaMs) {
        pal.setPalette(paletteLibrary.get(gradpal.getOption()));
        pal.setBottom(palStart.getValue());
        pal.setTop(palStop.getValue());
        pal.setBias(palBias.getValue());
        pal.setShift(palShift.getValue());
        pal.setCutoff(palCutoff.getValue());

        for (LUButterfly butterfly : KaledoscopeModel.allButterflies) {
            renderButterfly(drawDeltaMs, butterfly);
        }
        for (LUFlower flower : KaledoscopeModel.allFlowers) {
            renderFlower(drawDeltaMs, flower);
        }
    }

    protected void renderButterfly(double drawDeltaMs, LUButterfly butterfly) {
        int color = getNewRGB();
        if (useGradientPal.getValueb()) {
            color = pal.getColor(Math.random());
        }
        for (LXPoint p : butterfly.allPoints) {
            if (doPoints.getValueb()) {
                color = getNewRGB();
                if (useGradientPal.getValueb()) {
                    color = pal.getColor(Math.random());
                }
            }
            colors[p.index] = color;
        }
    }

    protected void renderFlower(double drawDeltaMs, LUFlower flower) {
        int color = getNewRGB();
        if (useGradientPal.getValueb()) {
            color = pal.getColor(Math.random());
        }
        for (LXPoint p : flower.allPoints) {
            if (doPoints.getValueb()) {
                color = getNewRGB();
                if (useGradientPal.getValueb()) {
                    color = pal.getColor(Math.random());
                }
            }
            colors[p.index] = color;
        }
    }
}
