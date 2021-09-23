package com.symmetrylabs.shows.firefly;

import art.lookingup.KaledoscopeModel;
import com.symmetrylabs.slstudio.palettes.ZigzagPalette;
import heronarts.lx.color.LXColor;

public class Blob {
    public static final int WAVEFORM_TRIANGLE = 0;
    public static final int WAVEFORM_SQUARE = 1;
    public static final int WAVEFORM_STEPDECAY = 2;

    public float pos = 0f;
    public float speed = 1f;
    public int color = LXColor.rgba(255, 255, 255, 255);
    public boolean enabled = true;
    public float intensity = 1.0f;
    public float blobWidth = -1.0f;
    int runId;
    public boolean useGradient = false;

    public ZigzagPalette pal = null;

    public void reset(int runId, float initialPos, float randomSpeed, boolean forward) {
        pos = initialPos;
        speed = randomSpeed * (float)Math.random();
        this.runId = runId;
    }

    public void renderBlob(int[] colors, float baseSpeed, float defaultWidth, float slope,
                           float maxValue, int waveform, boolean initialTail,
                           int whichEffect, float fxDepth, float cosineFreq) {
        renderBlob(colors, baseSpeed, defaultWidth, slope, maxValue, waveform, initialTail, LXColor.Blend.ADD,
            whichEffect, fxDepth, cosineFreq);
    }

    /**
     * Renders a 'blob'.  Could we a number of different 'waveforms' centered at the current
     * position.  Position will be incremented by baseSpeed + the blobs random speed component.
     * @param colors
     * @param baseSpeed
     * @param defaultWidth
     * @param slope
     * @param maxValue
     * @param waveform
     */
    public void renderBlob(int[] colors, float baseSpeed, float defaultWidth, float slope,
                           float maxValue, int waveform, boolean initialTail, LXColor.Blend blend,
                           int whichEffect, float fxDepth, float cosineFreq) {
        if (!enabled) return;

        KaledoscopeModel.Run run = KaledoscopeModel.allRuns.get(runId);

        float resolvedWidth = defaultWidth;
        if (blobWidth >= 0f)
            resolvedWidth = blobWidth;

        // -- Render on our target run --
        renderWaveform(colors, run, pos, resolvedWidth, slope, intensity * maxValue, waveform, blend);

        if (whichEffect == 1) {
            RunRender1D.randomGrayBaseDepth(colors, run, LXColor.Blend.MULTIPLY, (int)(255*(1f - fxDepth)),
                        (int)(255*fxDepth));
        } else if (whichEffect == 2) {
            RunRender1D.cosine(colors, run, pos, cosineFreq, 0f, 1f - fxDepth, fxDepth, LXColor.Blend.MULTIPLY);
        }
        pos += (baseSpeed + speed)/100f;
    }

    /**
     * Render the specified waveform at the specified position.  maxValue already includes the blob intensity override multiplied
     * into it by this point.
     * @param colors
     * @param run
     * @param position
     * @param width
     * @param slope
     * @param maxValue
     * @param waveform
     * @param blend
     * @return
     */
    public float[] renderWaveform(int[] colors, KaledoscopeModel.Run run, float position, float width, float slope,
                                  float maxValue, int waveform, LXColor.Blend blend) {
        if (waveform == WAVEFORM_TRIANGLE) {
            if (useGradient) {
                return RunRender1D.renderTriangle(colors, run, position, slope, maxValue, blend, pal);
            } else {
                return RunRender1D.renderTriangle(colors, run, position, slope, maxValue, blend, color);
            }
        } else if (waveform == WAVEFORM_SQUARE) {
            if (useGradient) {
                return RunRender1D.renderSquare(colors, run, position, width, maxValue, blend, pal);
            } else {
                return RunRender1D.renderSquare(colors, run, position, width, maxValue, blend, color);
            }
        } else {
            if (useGradient) {
                return RunRender1D.renderStepDecay(colors, run, position, width, slope, maxValue, true, LXColor.Blend.ADD, pal);
            } else {
                return RunRender1D.renderStepDecay(colors, run, position, width, slope,
                    maxValue, true, LXColor.Blend.ADD, color);
            }
        }
    }
}
