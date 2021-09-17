package com.symmetrylabs.shows.firefly;

import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;

abstract public class FPSPattern extends SLPattern {

    public final CompoundParameter fpsKnob =
        new CompoundParameter("Fps", 61, 0.0f, 61 + 10)
            .setDescription("Controls the frames per second.");
    public final BooleanParameter fbang = new BooleanParameter("fbang", false);
    public final BooleanParameter bangOn = new BooleanParameter("bngOn", false).setDescription("Enable frame banging");
    public final DiscreteParameter bangFrames = new DiscreteParameter("bngFrms", 1, 1, 200);
    public final CompoundParameter bangFade = new CompoundParameter("bngFade", 0f, 0f, 1f);
    public final BooleanParameter bangClear = new BooleanParameter("bngClr", true).setDescription("Clear frame after bang");

    protected double currentFrame = 0.0;
    protected int previousFrame = -1;
    protected double deltaDrawMs = 0.0;
    protected int currentBangFrames = -1;

    public FPSPattern(LX lx) {
        super(lx);
    }

    @Override
    public void run(double deltaMs) {

        double fps = fpsKnob.getValue();
        currentFrame += (deltaMs / 1000.0) * fps;
        // We don't call draw() every frame so track the accumulated deltaMs for them.
        deltaDrawMs += deltaMs;

        // If we received a bang, reset our currentBangFrames counter and reset the fbang trigger.
        if (fbang.getValueb() && bangOn.getValueb()) {
            currentBangFrames = 0;
            fbang.setValue(false);
            currentFrame = 0.0;
            previousFrame = -1;
        }

        // If we are not relying on bangs or a bang is currently running, render a frame at the
        // configured FPS.
        if (!bangOn.getValueb() || bangIsRunning()) {
            if ((int) currentFrame > previousFrame) {
                // Time for new frame.  Draw
                renderFrame(deltaDrawMs);
                previousFrame = (int) currentFrame;
                deltaDrawMs = 0.0;
                if (bangIsRunning()) {
                    currentBangFrames++;
                    if (currentBangFrames == bangFrames.getValuei()) {
                        currentBangFrames = -1;
                        // Allow for a final black frame for bangs to better represent the ephemeral nature of bangs.
                    }
                }
            }
        }

        // Don't let current frame increment forever.  Otherwise float will
        // begin to lose precision and things get wonky.
        if (currentFrame > 10000.0) {
            currentFrame = 0.0;
            previousFrame = -1;
        }
    }

    public boolean bangIsRunning() {
        return (currentBangFrames < bangFrames.getValuei() &&
            currentBangFrames != -1);
    }

    public float bangFadeLevel() {
        return 1f - ((float)currentBangFrames/(float)(bangFrames.getValuei()-1f) * bangFade.getValuef());
    }

    protected abstract void renderFrame(double deltaDrawMs);
}
