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
import heronarts.lx.color.LXColor;
import heronarts.lx.modulator.LinearEnvelope;
import heronarts.lx.parameter.BasicParameter;

public class FlashEffect extends LXEffect {

    private final LinearEnvelope flash;
    private final BasicParameter attack;
    private final BasicParameter decay;
    private final BasicParameter intensity;
    private final BasicParameter sat;

    public FlashEffect(LX lx) {
        super(lx, true);
        this.addModulator(this.flash = new LinearEnvelope(0, 0, 0));
        this.addParameter(this.attack = new BasicParameter("ATTACK", 100, 1000));
        this.addParameter(this.decay = new BasicParameter("DECAY", 1500, 3000));
        this.addParameter(this.intensity = new BasicParameter("INTENSITY", 1));
        this.addParameter(this.sat = new BasicParameter("SAT", 0));
    }

    private double getAttackTime() {
        return this.attack.getValue();
    }

    private double getDecayTime() {
        return this.decay.getValue();
    }

    @Override
    protected void onEnable() {
        this.flash.setRange(this.flash.getValue(), this.intensity.getValue(),
                getAttackTime()).trigger();
    }

    @Override
    protected void onDisable() {
        this.flash.setRange(this.flash.getValue(), 0, getDecayTime()).trigger();
    }

    @Override
    protected void onTrigger() {
        this.flash.setRange(this.intensity.getValue(), 0, getDecayTime()).trigger();
    }

    @Override
    protected void run(double deltaMs) {
        float flashValue = this.flash.getValuef();
        double satValue = this.sat.getValue() * 100.;
        double hueValue = this.lx.getBaseHue();
        if (flashValue > 0) {
            for (int i = 0; i < this.lx.total; ++i) {
                this.colors[i] = LXColor.lerp(this.colors[i], LXColor.hsb(hueValue, satValue, 100.), flashValue);
            }
        }
    }
}
