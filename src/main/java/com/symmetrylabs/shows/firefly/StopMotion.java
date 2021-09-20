package com.symmetrylabs.shows.firefly;

import art.lookingup.KaledoscopeModel;
import art.lookingup.LUButterfly;
import art.lookingup.LUFlower;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;

import java.util.logging.Logger;

public class StopMotion extends ColorPattern {
    public static final String GROUP_NAME = FireflyShow.SHOW_NAME;
    private static final Logger logger = Logger.getLogger(StopMotion.class.getName());

    public static final int MAX_BUTTERFLIES = 10;

    DiscreteParameter numButterflies = new DiscreteParameter("bflies", 5, 0, MAX_BUTTERFLIES);
    CompoundParameter onTime = new CompoundParameter("on", 1f, 0.1f, 2f);
    CompoundParameter offTime = new CompoundParameter("off", 1f, 0.1f, 2f);

    int currentIndex = 0;
    double onDuration = 0f;
    double offDuration = 0f;
    boolean on;

    int[] currentIndices = new int[MAX_BUTTERFLIES];
    int[] butterflyColors = new int[MAX_BUTTERFLIES];

    public StopMotion(LX lx) {
        super(lx);
        addParameter("bflies", numButterflies);
        addParameter(onTime);
        addParameter(offTime);
        LXParameterListener paletteUpdater = new LXParameterListener() {
            @Override
            public void onParameterChanged(LXParameter p) {
                updateColors();
            }
        };
        addParameter(paletteKnob);
        paletteKnob.addListener(paletteUpdater);
        addParameter("RandPlt",randomPaletteKnob);
        randomPaletteKnob.addListener(paletteUpdater);
        addParameter(saturation);
        addParameter(bright);
        addParameter(hue);
        LXParameterListener colorUpdater = new LXParameterListener() {
            @Override
            public void onParameterChanged(LXParameter parameter) {
                if (paletteKnob.getValuei() == 1) {
                    updateColors();
                }
            }
        };
        hue.addListener(colorUpdater);
        saturation.addListener(colorUpdater);
        bright.addListener(colorUpdater);

    }

    @Override
    public void onActive() {
        // TODO reset positions, etc.
        // Spacing
        int spacing = KaledoscopeModel.allButterflies.size() / numButterflies.getValuei();
        for (int i = 0; i < numButterflies.getValuei(); i++) {
            currentIndices[i] = i * spacing;
            butterflyColors[i] = getNewRGB();
        }
    }

    public void updateColors() {
        for (int i = 0; i < MAX_BUTTERFLIES; i++) {
            butterflyColors[i] = getNewRGB();
        }
    }

    @Override
    protected void renderFrame(double deltaMs) {
        for (LXPoint p : model.points) {
            colors[p.index] = LXColor.rgb(0, 0, 0);
        }

        if (on) {
            for (int i = 0; i < numButterflies.getValuei(); i++) {
                LUButterfly butterfly = KaledoscopeModel.allButterflies.get(currentIndices[i]);
                butterfly.setColor(colors, butterflyColors[i]);
            }
            onDuration += deltaMs;
        } else {
            offDuration += deltaMs;
        }

        if (onDuration > onTime.getValuef() * 1000f) {
            on = false;
            onDuration = 0f;
        } else if (offDuration > offTime.getValuef() * 1000f) {
            on = true;
            offDuration = 0f;
            for (int i = 0; i < numButterflies.getValuei(); i++) {
                currentIndices[i] = (currentIndices[i] + 1) % KaledoscopeModel.allButterflies.size();
            }
        }
    }
}
