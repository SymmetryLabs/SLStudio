package com.symmetrylabs.slstudio.ui.v2;

import com.badlogic.gdx.Gdx;
import heronarts.lx.LX;
import heronarts.lx.LXEngine;
import java.util.LinkedList;

public class InternalsWindow extends CloseableWindow {
    private static final int FRAMES_TO_KEEP = 300; // 5 seconds at 60fps
    private final LX lx;
    private final SLStudioGDX parent;
    private final ModelRenderer modelRenderer;
    private final LinkedList<Float> frameTimes = new LinkedList<>();

    InternalsWindow(LX lx, SLStudioGDX parent, ModelRenderer modelRenderer) {
        super("Internals");
        this.lx = lx;
        this.parent = parent;
        this.modelRenderer = modelRenderer;
        for (int i = 0; i < FRAMES_TO_KEEP; i++) {
            frameTimes.push(0.f);
        }
    }

    @Override
    protected void windowSetup() {
        UI.setNextWindowDefaults(25, 500, UIConstants.DEFAULT_WINDOW_WIDTH, 300);
    }

    @Override
    protected void drawContents() {
        UI.text("engine average: % 4.0fms, % 3.0ffps",
                        1e-6f * lx.engine.timer.runAvgNanos,
                        1e9f / lx.engine.timer.runAvgNanos);
        UI.text("worst-case: % 4.0fms, % 3.0ffps",
                        1e-6f * lx.engine.timer.runWorstNanos,
                        1e9f / lx.engine.timer.runWorstNanos);

        frameTimes.addLast(lx.engine.timer.runCurrentNanos * 1e-6f);
        while (frameTimes.size() > FRAMES_TO_KEEP) {
            frameTimes.removeFirst();
        }
        float[] frameTimeArr = new float[frameTimes.size()];
        float max = 0;
        int i = 0;
        for (Float ft : frameTimes) {
            frameTimeArr[i] = ft;
            if (ft > max) {
                max = ft;
            }
            i++;
        }
        UI.plot("ms per f", frameTimeArr, 0, Float.max(1.2f * max, 1), 100);

        UI.separator();

        UI.text("ui frame rate: %3.0ffps", UI.getFrameRate());
        UI.text("ui density: %3.2f", Gdx.graphics.getDensity());
        parent.allowUiScale = UI.checkbox("scale UI", parent.allowUiScale);

        modelRenderer.scalePointSize = UI.sliderFloat("point size", modelRenderer.scalePointSize, 0.01f, 5);
        parent.clearRGB = UI.colorPicker("background", parent.clearRGB);
    }
}
