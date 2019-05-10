package com.symmetrylabs.shows.cubes;

import heronarts.lx.LX;
import com.symmetrylabs.slstudio.ui.v2.CloseableWindow;
import com.symmetrylabs.color.PerceptualColorScale;
import com.symmetrylabs.slstudio.ui.v2.UI;


public class OutputScaleWindow extends CloseableWindow {
    protected final LX lx;
    protected final PerceptualColorScale scale;

    protected final float[] lutRed = new float[256];
    protected final float[] lutGreen = new float[256];
    protected final float[] lutBlue = new float[256];
    protected double lutsGeneratedAt = 0;

    public OutputScaleWindow(LX lx, PerceptualColorScale scale) {
        super("Scaling");
        this.lx = lx;
        this.scale = scale;
    }

    @Override
    protected void drawContents() {
        double currentTarget = scale.getTargetLinearScale();
        if (lutsGeneratedAt != currentTarget) {
            int[][] intLut = new int[3][256];
            scale.copyLut8(intLut);
            for (int i = 0; i < 256; i++) {
                lutRed[i] = ((float) intLut[0][i]) / 255.0f;
            }
            for (int i = 0; i < 256; i++) {
                lutGreen[i] = ((float) intLut[1][i]) / 255.0f;
            }
            for (int i = 0; i < 256; i++) {
                lutBlue[i] = ((float) intLut[2][i]) / 255.0f;
            }
            lutsGeneratedAt = currentTarget;
        }
        float targetScale = UI.sliderFloat("outputMax", (float) currentTarget, 0, 1);
        if (Math.abs(targetScale - currentTarget) > 0.0001) {
            scale.setTargetLinearScale(targetScale);
        }
        UI.plot("LUT", lutBlue, 0, 1, 150);
    }
}
