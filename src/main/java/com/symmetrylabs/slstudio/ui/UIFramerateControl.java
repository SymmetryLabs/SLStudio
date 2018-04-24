package com.symmetrylabs.slstudio.ui;

import heronarts.lx.LX;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.component.UILabel;
import heronarts.p3lx.ui.component.UIIntegerBox;

import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.parameter.LXParameter;

public class UIFramerateControl extends UI2dContainer {
    private final DiscreteParameter fpsProxy;

    public UIFramerateControl(UI ui, final LX lx, float x, float y, float w) {
        super(x, y, w, 20);
        setBackgroundColor(0xff404040); //ui.theme.getDeviceBackgroundColor()
        setBorderRounding(4);

        final BoundedParameter fps = lx.engine.framesPerSecond;
        fpsProxy = new DiscreteParameter("fpsProxy", (int)fps.getValue(), (int)fps.range.min, (int)fps.range.max);

        new UILabel(5, 2, 70, 12)
            .setLabel("FRAMERATE")
            .addToContainer(this);

        new UIIntegerBox(80, 2, 90, 16)
            .setParameter(fpsProxy)
            .addToContainer(this);

        fps.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter parameter) {
                fpsProxy.setValue((int)fps.getValue());
            }
        });

        fpsProxy.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter parameter) {
                fps.setValue((int)fpsProxy.getValue());
            }
        });
    }
}
