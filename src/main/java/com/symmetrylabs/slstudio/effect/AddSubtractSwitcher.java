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
    boolean subMode = false;
    boolean setToAdd = false;
    LXChannel c = null;

    public AddSubtractSwitcher(LX lx) {
        super(lx);

        add = new BooleanParameter("add", false);
        add.setMode(BooleanParameter.Mode.MOMENTARY);
        addParameter(add);

        sub = new BooleanParameter("sub", false);
        sub.setMode(BooleanParameter.Mode.MOMENTARY);
        addParameter(sub);

        add.addListener(param -> {
            if (add.isOn() && c != null) {
                c.blendMode.setValue(0);
                boolean alreadyOn = c.enabled.isOn();
                if (!alreadyOn) {
                    c.enabled.setValue(true);
                } else if (!subMode) {
                    c.enabled.setValue(false);
                }
                subMode = false;
            }
        });

        sub.addListener(param -> {
            if (sub.isOn() && c != null) {
                c.blendMode.setValue(1);
                boolean alreadyOn = c.enabled.isOn();
                if (!alreadyOn) {
                    c.enabled.setValue(true);
                } else if (subMode) {
                    c.enabled.setValue(false);
                }
                subMode = true;
            }
        });


    }

    @Override
    public void run(double deltaMs, double enabledAmount) {
        LXBus bus = getBus();
        if ((bus instanceof LXChannel)) {
            c = (LXChannel)bus;            
        }
        if (c == null) {
            return;
        }

        if (!setToAdd) {
            c.blendMode.setValue(0);
            setToAdd = true;
        }

        if (subMode) {
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
