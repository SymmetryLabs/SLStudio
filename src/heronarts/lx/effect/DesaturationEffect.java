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

import heronarts.lx.HeronLX;
import heronarts.lx.control.BasicParameter;
import heronarts.lx.modulator.LinearEnvelope;

public class DesaturationEffect extends LXEffect {

    private final LinearEnvelope desaturation;
    private final BasicParameter attack;
    private final BasicParameter decay;
    private final BasicParameter amount;
    
    public DesaturationEffect(HeronLX lx) {
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
    
    public void onEnable() {
        this.desaturation.setEndVal(amount.getValue() * 100.).setDuration(getAttackTime()).start();
    }
    
    public void onDisable() {
        this.desaturation.setEndVal(0).setDuration(getDecayTime()).start();
    }
    
    protected void doApply(int[] colors) {
        double value = this.desaturation.getValue();
        if (value > 0) {
            for (int i = 0; i < colors.length; ++i) {
                colors[i] = this.lx.colord(
                        this.lx.applet.hue(colors[i]),
                        Math.max(0, this.lx.applet.saturation(colors[i]) - value),
                        this.lx.applet.brightness(colors[i])
                        );
                
            }
        }
    }

}
