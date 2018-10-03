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
    BooleanParameter fxKill;

    public Blackout(LX lx) {
        super(lx);

        kill = new BooleanParameter("kill", false);
        kill.setMode(BooleanParameter.Mode.MOMENTARY);
        addParameter(kill);

        hold = new BooleanParameter("hold", false);
        hold.setMode(BooleanParameter.Mode.MOMENTARY);
        addParameter(hold);

        fxKill = new BooleanParameter("fxKill", false);
        fxKill.setMode(BooleanParameter.Mode.MOMENTARY);
        addParameter(fxKill);
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
                if (c.getLabel().contains("Lattice")) {
                    continue;
                }
                c.enabled.setValue(false);
            }
        }

        if (fxKill.isOn()) {
            for (LXChannel c : lx.engine.channels) {
                LXEffect e = c.getEffect("StripFilter");
                if (e != null && c.getLabel().equals("Solid")) {
                    ((BooleanParameter)e.getParameter("X")).setValue(true);
                    ((BooleanParameter)e.getParameter("Y")).setValue(true);
                    ((BooleanParameter)e.getParameter("Z")).setValue(true);
                }

                if (!c.getLabel().contains("Overlay")) {
                    continue;
                }
                c.enabled.setValue(false);
            }
        }
    }
}
