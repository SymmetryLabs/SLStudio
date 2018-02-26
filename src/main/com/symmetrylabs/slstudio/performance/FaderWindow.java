package com.symmetrylabs.slstudio.performance;

import com.symmetrylabs.slstudio.SLStudioLX;
import heronarts.lx.parameter.LXListenableNormalizedParameter;
import heronarts.p3lx.ui.UIWindow;
import heronarts.p3lx.ui.component.UISlider;
import processing.event.MouseEvent;


class FaderWindow extends UIWindow {
    final UISlider slider;

    FaderWindow(LXListenableNormalizedParameter param, SLStudioLX.UI ui, String title, float x, float y, float w, float h) {
        super(ui, title, x, y, w, h);

        float pad = 5;
        slider = new UISlider(UISlider.Direction.HORIZONTAL, pad, pad, w - (2 * pad), h - (2 * pad));
        slider.addToContainer(this);

        slider.setParameter(param);

    }

    @Override
    public void onMouseDragged(MouseEvent mouseEvent, float mx, float my, float dx, float dy) {
    }
}
