package com.symmetrylabs.slstudio.effect;

import heronarts.lx.LX;
import heronarts.lx.LXEffect;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;

public class SLDesaturation extends LXEffect {
    private final CompoundParameter attack = (new CompoundParameter("Attack", 100.0D, 0.0D, 1000.0D)).setDescription("Sets the attack time of the desaturation");
    private final CompoundParameter decay = (new CompoundParameter("Decay", 100.0D, 0.0D, 1000.0D)).setDescription("Sets the decay time of the desaturation");
    private final CompoundParameter amount = (new CompoundParameter("Amount", 1.0D)).setDescription("Sets the amount of desaturation to apply");

    public SLDesaturation(LX lx) {
        super(lx);
        this.addParameter("amount", this.amount);
    }

    public void onParameterChanged(LXParameter p) {
        if (p == this.attack) {
        } else if (p == this.decay) {
        }

    }

    protected void run(double deltaMs, double amount) {
        double d = amount * this.amount.getValue();
        if (d > 0.0D) {
            d = 1.0D - d;

            for(int i = 0; i < this.colors.length; ++i) {
                this.colors[i] = LXColor.hsba((double)LXColor.h(this.colors[i]), Math.max(0.0D, (double)LXColor.s(this.colors[i]) * d), (double)LXColor.b(this.colors[i]), (double)(this.colors[i] >>> 24));
            }
        }

    }
}
