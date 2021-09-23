package com.symmetrylabs.shows.firefly;

import art.lookingup.KaledoscopeModel;
import com.symmetrylabs.slstudio.palettes.PaletteLibrary;
import com.symmetrylabs.slstudio.palettes.ZigzagPalette;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;

public class Blobs extends ColorPattern {
    public static final String GROUP_NAME = FireflyShow.SHOW_NAME;

    public static final int MAX_BLOBS = 100;
    private final PaletteLibrary paletteLibrary = PaletteLibrary.getInstance();

    public CompoundParameter slope = new CompoundParameter("slope", 30.0, 0.001, 100.0);
    public CompoundParameter maxValue = new CompoundParameter("maxv", 1.0, 0.0, 1.0);
    public CompoundParameter speed = new CompoundParameter("speed", 1.0, 0.0, 10.0);
    public CompoundParameter randSpeed = new CompoundParameter("randspd", 0.2, 0.0, 10);
    public DiscreteParameter numBlobs = new DiscreteParameter("blobs", 5, 1, MAX_BLOBS);
    public DiscreteParameter fxKnob = new DiscreteParameter("fx", 0, 0, 3).setDescription("0=none 1=sparkle 2=cosine");
    public CompoundParameter fxDepth = new CompoundParameter("fxDepth", 1.0f, 0.1f, 1.0f);
    public DiscreteParameter waveKnob = new DiscreteParameter("wave", 0, 0, 3).setDescription("Waveform type");
    public CompoundParameter widthKnob = new CompoundParameter("width", 0.1f, 0.0f, 10.0f).setDescription("Square wave width");
    public CompoundParameter cosineFreq = new CompoundParameter("cfreq", 1.0, 1.0, 400.0);

    BooleanParameter useGradientPal = new BooleanParameter("usegpal", false);
    DiscreteParameter gradpal = new DiscreteParameter("gradpal", paletteLibrary.getNames());
    // selected colour palette
    CompoundParameter palStart = new CompoundParameter("palStart", 0, 0, 1);  // palette start point (fraction 0 - 1)
    CompoundParameter palStop = new CompoundParameter("palStop", 1, 0, 1);  // palette stop point (fraction 0 - 1)
    CompoundParameter palBias = new CompoundParameter("palBias", 0, -6, 6);  // bias colour palette toward zero (dB)
    CompoundParameter palShift = new CompoundParameter("palShift", 0, -1, 1);  // shift in colour palette (fraction 0 - 1)
    CompoundParameter palCutoff = new CompoundParameter("palCutoff", 0, 0, 1);  // palette value cutoff (fraction 0 - 1)
    ZigzagPalette pal = new ZigzagPalette();

    public Blob[] blobs = new Blob[MAX_BLOBS];

    public Blobs(LX lx) {
        super(lx);
        addParameter(fpsKnob);
        addParameter(paletteKnob);
        addParameter(randomPaletteKnob);
        addParameter(hue);
        addParameter(saturation);
        addParameter(bright);
        addParameter(slope);
        addParameter(maxValue);
        addParameter(speed);
        addParameter(numBlobs);
        addParameter(randSpeed);
        addParameter(fxKnob);
        addParameter(fxDepth);
        addParameter(waveKnob);
        addParameter(widthKnob);
        addParameter(cosineFreq);
        addParameter(useGradientPal);
        addParameter(gradpal);
        addParameter(palStart);
        addParameter(palStop);
        addParameter(palShift);
        addParameter(palBias);
        addParameter(palCutoff);
        gradpal.setValue(3);
        resetBlobs();
    }

    public void resetBlobs() {
        for (int i = 0; i < MAX_BLOBS; i++) {
            blobs[i] = new Blob();
            // TODO(tracy): Pick an initial random lightBarNum such that we are restricted to a specific fixture.
            blobs[i].reset(i % KaledoscopeModel.allButterflyRuns.size(), 0.0f, randSpeed.getValuef()/10f, true);
            blobs[i].color = getNewRGB();
            blobs[i].pos = -0.001f * i;
        }
    }

    /**
     * onActive is called when the pattern starts playing and becomes the active pattern.  Here we re-assigning
     * our speeds to generate some randomness in the speeds.
     */
    @Override
    public void onActive() {
        resetBlobs();
    }

    @Override
    public void renderFrame(double deltaMs) {
        pal.setPalette(paletteLibrary.get(gradpal.getOption()));
        pal.setBottom(palStart.getValue());
        pal.setTop(palStop.getValue());
        pal.setBias(palBias.getValue());
        pal.setShift(palShift.getValue());
        pal.setCutoff(palCutoff.getValue());

        for (LXPoint pt : lx.model.points) {
            colors[pt.index] = LXColor.rgba(0,0,0, 255);
        }

        float fadeLevel = maxValue.getValuef();
        if (bangIsRunning())
            fadeLevel = bangFadeLevel();

        for (int i = 0; i < numBlobs.getValuei(); i++) {
            blobs[i].pal = pal;
            blobs[i].useGradient = useGradientPal.getValueb();
            blobs[i].renderBlob(colors, speed.getValuef()/10f, widthKnob.getValuef()/10f, slope.getValuef(), fadeLevel,
                waveKnob.getValuei(), false, fxKnob.getValuei(), fxDepth.getValuef(),
                cosineFreq.getValuef());
            if (blobs[i].pos > 0.333f) {
                blobs[i].pos = -0.000f;
                // Reset the random speed on each loop so we don't have to exit and re-enter pattern for the random
                // speed component to update.
                blobs[i].speed =  randSpeed.getValuef()/10f * (float)Math.random();
            }
        }
    }
}
