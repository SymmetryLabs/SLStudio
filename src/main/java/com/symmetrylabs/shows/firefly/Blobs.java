package com.symmetrylabs.shows.firefly;

import art.lookingup.KaledoscopeModel;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;

public class Blobs extends ColorPattern {
    public static final String GROUP_NAME = FireflyShow.SHOW_NAME;

    public static final int MAX_BLOBS = 100;

    public CompoundParameter slope = new CompoundParameter("slope", 30.0, 0.001, 100.0);
    public CompoundParameter maxValue = new CompoundParameter("maxv", 1.0, 0.0, 1.0);
    public CompoundParameter speed = new CompoundParameter("speed", 1.0, 0.0, 10.0);
    public CompoundParameter randSpeed = new CompoundParameter("randspd", 0.2, 0.0, 5.0);
    public DiscreteParameter numBlobs = new DiscreteParameter("blobs", 1, 1, MAX_BLOBS);
    public DiscreteParameter fxKnob = new DiscreteParameter("fx", 0, 0, 3).setDescription("0=none 1=sparkle 2=cosine");
    public CompoundParameter fxDepth = new CompoundParameter("fxDepth", 1.0f, 0.1f, 1.0f);
    public DiscreteParameter waveKnob = new DiscreteParameter("wave", 0, 0, 3).setDescription("Waveform type");
    public CompoundParameter widthKnob = new CompoundParameter("width", 0.1f, 0.0f, 10.0f).setDescription("Square wave width");
    public CompoundParameter cosineFreq = new CompoundParameter("cfreq", 1.0, 1.0, 400.0);

    public Blob[] blobs = new Blob[MAX_BLOBS];

    public Blobs(LX lx) {
        super(lx);
        addParameter(fpsKnob);
        addParameter(fbang);
        addParameter(bangOn);
        addParameter(bangFrames);
        addParameter(bangClear);
        addParameter(bangFade);
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
        resetBlobs();
    }

    public void resetBlobs() {
        for (int i = 0; i < MAX_BLOBS; i++) {
            blobs[i] = new Blob();
            // TODO(tracy): Pick an initial random lightBarNum such that we are restricted to a specific fixture.
            blobs[i].reset(i % KaledoscopeModel.allButterflyRuns.size(), 0.0f, randSpeed.getValuef(), true);
            blobs[i].color = getNewRGB();
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
        for (LXPoint pt : lx.model.points) {
            colors[pt.index] = LXColor.rgba(0,0,0, 255);
        }

        float fadeLevel = maxValue.getValuef();
        if (bangIsRunning())
            fadeLevel = bangFadeLevel();

        for (int i = 0; i < numBlobs.getValuei(); i++) {
            blobs[i].renderBlob(colors, speed.getValuef(), widthKnob.getValuef(), slope.getValuef(), fadeLevel,
                waveKnob.getValuei(), false, fxKnob.getValuei(), fxDepth.getValuef(),
                cosineFreq.getValuef());
            if (blobs[i].pos > 1f) {
                blobs[i].pos = -0.2f;
            }
        }
    }
}
