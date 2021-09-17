package com.symmetrylabs.shows.firefly;

import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;

import java.util.logging.Logger;

/**
 * Uses blob rendering code to render a tracer on a run.
 */
public class BlobTest extends SLPattern {
    public static final String GROUP_NAME = FireflyShow.SHOW_NAME;

    private static final Logger logger = Logger.getLogger(BlobTest.class.getName());

    static public final float MAX_INTENSITY = 1.0f;

    public CompoundParameter slope = new CompoundParameter("slope", 1.0, 0.001, 60.0);
    public CompoundParameter speed = new CompoundParameter("speed", 0.01, 0.0, 5.0);
    public DiscreteParameter waveKnob = new DiscreteParameter("wave", 0, 0, 3).setDescription("Waveform type");
    public CompoundParameter widthKnob = new CompoundParameter("width", 0.1f, 0.0f, 1.0f).setDescription("Square wave width");
    public DiscreteParameter fxKnob = new DiscreteParameter("fx", 0, 0, 3).setDescription("0=none 1=sparkle 2=cosine");
    public CompoundParameter fxDepth = new CompoundParameter("fxDepth", 1.0f, 0.1f, 1.0f);
    public CompoundParameter cosineFreq = new CompoundParameter("cfreq", 1.0, 1.0, 400.0);

    public Blob blob = new Blob();

    public BlobTest(LX lx) {
        super(lx);
        addParameter(slope);
        addParameter(speed);
        addParameter(waveKnob);
        addParameter(widthKnob);
        addParameter(fxKnob);
        addParameter(fxDepth);
        addParameter(cosineFreq);
        resetBlobs();
    }

    public void resetBlobs() {
        blob.reset(0, 1f,0f, false);
    }

    /**
     * onActive is called when the pattern starts playing and becomes the active pattern.  Here we re-assigning
     * our speeds to generate some randomness in the speeds.
     */
    @Override
    public void onActive() {
        resetBlobs();
    }

    public void run(double deltaMs) {
        for (LXPoint pt : lx.model.points) {
            colors[pt.index] = LXColor.rgba(0,0,0, 255);
        }
        blob.renderBlob(colors, speed.getValuef(), widthKnob.getValuef(), slope.getValuef(), MAX_INTENSITY,
            waveKnob.getValuei(),  false, fxKnob.getValuei(), fxDepth.getValuef(), cosineFreq.getValuef());
        if (blob.pos > 1f) {
            blob.pos = 0f;
        }
    }
}
