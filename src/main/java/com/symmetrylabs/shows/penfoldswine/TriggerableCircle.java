package com.symmetrylabs.shows.penfoldswine;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.EnumParameter;
import heronarts.lx.transform.LXVector;
import heronarts.lx.modulator.QuadraticEnvelope;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;

import static com.symmetrylabs.util.MathUtils.*;


/**
 * Should be made more interesting in the future,
 * but for now just does what it needs for PenfoldsWine gig.
 * Ideally, this would have the ability to pick the origin of the "explosion"
 * or have a random origin, also trigger multiple layering explosions.
 * Add falloff, or ripple, etc.
 * (Or can probably just revamp Explosions pattern to do this)
 */
public class TriggerableCircle extends LXPattern {

    final CompoundParameter rate = new CompoundParameter("rate", 5000, 500, 20000);

    final EnumParameter<QuadraticEnvelope.Ease> easing = new EnumParameter<>("easing", QuadraticEnvelope.Ease.BOTH);

    final QuadraticEnvelope envelope = new QuadraticEnvelope(0, 1, rate);

    final CompoundParameter hue = new CompoundParameter("hue", 0, 0, 360);

    final BooleanParameter trigger = new BooleanParameter("trigger")
        .setMode(BooleanParameter.Mode.MOMENTARY);

    private float targetRadius;

    public TriggerableCircle(LX lx) {
        super(lx);
        addParameter(rate);
        addParameter(easing);
        addModulator(envelope);
        addParameter(hue);
        addParameter(trigger);

        this.targetRadius = sqrt(pow(model.xRange/2, 2) + pow(model.yRange/2, 2)) + 1;

        easing.addListener(parameter -> {
            envelope.setEase(easing.getEnum());
        });

        trigger.addListener(parameter -> {
            if (trigger.isOn()) {
                envelope.trigger();
            }
        });
    }

    public void run(double deltaMs) {
        for (LXVector p : getVectors()) {
            if (dist(p.x, p.y, model.cx, model.cy) < (envelope.getValuef() * targetRadius)) {
                colors[p.index] = lx.hsb(hue.getValuef(), 100, 100);
            } else {
                colors[p.index] = LXColor.BLACK;
            }
        }
    }
}
