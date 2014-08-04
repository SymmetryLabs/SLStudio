/**
 * ##library.name##
 * ##library.sentence##
 * ##library.url##
 *
 * Copyright ##copyright## ##author##
 * All Rights Reserved
 *
 * @author      ##author##
 * @modified    ##date##
 * @version     ##library.prettyVersion## (##library.version##)
 */

package heronarts.lx.effect;

import heronarts.lx.LX;
import heronarts.lx.LXColor;
import heronarts.lx.modulator.LinearEnvelope;
import heronarts.lx.parameter.BasicParameter;

public class DesaturationEffect extends LXEffect {

    private final LinearEnvelope desaturation;
    private final BasicParameter attack;
    private final BasicParameter decay;
    private final BasicParameter amount;

    public DesaturationEffect(LX lx) {
        super(lx);
        this.addModulator(this.desaturation = new LinearEnvelope(0, 0, 100));
        this.addParameter(this.attack = new BasicParameter("ATTACK", 0.1));
        this.addParameter(this.decay = new BasicParameter("DECAY", 0.1));
        this.addParameter(this.amount = new BasicParameter("AMOUNT", 1.));
    }

    private double getAttackTime() {
        return this.attack.getValue() * 1000.;
    }

    private double getDecayTime() {
        return this.decay.getValue() * 1000.;
    }

    @Override
    protected void onEnable() {
        this.desaturation.setRangeFromHereTo(amount.getValue() * 100.,
                getAttackTime()).start();
    }

    @Override
    protected void onDisable() {
        this.desaturation.setRangeFromHereTo(0, getDecayTime()).start();
    }

    @Override
    protected void run(double deltaMs) {
        double value = this.desaturation.getValue();
        if (value > 0) {
            for (int i = 0; i < colors.length; ++i) {
                colors[i] = LXColor.hsb(LXColor.h(colors[i]),
                        Math.max(0, LXColor.s(colors[i]) - value), LXColor.b(colors[i]));

            }
        }
    }

}
