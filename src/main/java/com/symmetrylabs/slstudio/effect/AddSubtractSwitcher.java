package com.symmetrylabs.slstudio.effect;

import heronarts.lx.LX;
import heronarts.lx.LXBus;
import heronarts.lx.LXChannel;
import heronarts.lx.LXEffect;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.transform.LXVector;

public class AddSubtractSwitcher extends LXEffect {
    BooleanParameter add;
    BooleanParameter sub;
    boolean doInvert = false;

    public AddSubtractSwitcher(LX lx) {
        super(lx);

        add = new BooleanParameter("add", false);
        add.setMode(BooleanParameter.Mode.MOMENTARY);
        addParameter(add);

        sub = new BooleanParameter("sub", false);
        sub.setMode(BooleanParameter.Mode.MOMENTARY);
        addParameter(sub);
    }

    @Override
    public void run(double deltaMs, double enabledAmount) {
        LXBus bus = getBus();
        if (!(bus instanceof LXChannel)) {
            return;
        }

        LXChannel c = (LXChannel)bus;


        if (add.isOn()) {
            doInvert = false;
            c.blendMode.setValue(0);
        }

        if (sub.isOn()) {
            doInvert = true;
            c.blendMode.setValue(1);
        }

        if (doInvert) {
            for (LXVector v : getVectors()) {
                int col = colors[v.index];
                float h = LXColor.h(col);
                float s = LXColor.s(col);
                float b = LXColor.b(col);
                colors[v.index] = LXColor.hsb(h, s, 100.0f - b);
            }
        }
    }
}
