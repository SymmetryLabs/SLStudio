package com.symmetrylabs.slstudio.effect;

import heronarts.lx.LX;
import heronarts.lx.LXEffect;
import heronarts.lx.parameter.BoundedParameter;

/**
 * SpeedEffect — controls the rate at which a channel or pattern advances time.
 *
 * When added to a channel it scales the deltaMs seen by all patterns and
 * per-pattern effects on that channel (same as the channel speed knob).
 * When added to a pattern's per-pattern effect list it scales the deltaMs
 * seen by that pattern only.
 *
 * The effect has no color-processing action of its own; the speed scaling is
 * applied by LXChannel before patterns are run.
 */
public class SpeedEffect extends LXEffect {

    public final BoundedParameter speed =
        new BoundedParameter("Speed", 1, 0, 2)
            .setDescription("Speed multiplier: 0=frozen, 1=normal, 2=double speed");

    public SpeedEffect(LX lx) {
        super(lx);
        addParameter("speed", this.speed);
    }

    @Override
    protected void run(double deltaMs, double enabledAmount) {
        // No color processing — speed scaling is applied in LXChannel.loop()
    }
}
