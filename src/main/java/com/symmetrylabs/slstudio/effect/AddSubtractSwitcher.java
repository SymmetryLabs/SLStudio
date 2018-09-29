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
        LXBus b = getBus();
        if (!(b instanceof  LXChannel)) {
            return;
        }

        LXChannel c = (LXChannel)b;


        if (add.isOn()) {
            c.blendMode.setValue(0);
        }

        if (sub.isOn()) {
            c.blendMode.setValue(2);
        }
    }
}
