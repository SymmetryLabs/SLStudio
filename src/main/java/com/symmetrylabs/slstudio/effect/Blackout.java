package com.symmetrylabs.slstudio.effect;

import heronarts.lx.LX;
import heronarts.lx.LXChannel;
import heronarts.lx.LXEffect;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.transform.LXVector;

public class Blackout extends LXEffect {
    BooleanParameter kill;
    BooleanParameter hold;

    public Blackout(LX lx) {
        super(lx);

        kill = new BooleanParameter("kill", false);
        kill.setMode(BooleanParameter.Mode.MOMENTARY);
        addParameter(kill);

        hold = new BooleanParameter("hold", false);
        hold.setMode(BooleanParameter.Mode.MOMENTARY);
        addParameter(hold);
    }

    @Override
    public void run(double deltaMs, double enabledAmount) {
        if (hold.isOn()) {
            for (LXVector v : getVectors()) {
                colors[v.index] = LXColor.BLACK;
            }
        }

        if (kill.isOn()) {
            for (LXChannel c : lx.engine.channels) {
                c.enabled.setValue(false);
            }
        }
    }
}
