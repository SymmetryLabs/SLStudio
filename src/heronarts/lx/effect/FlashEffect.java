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
import heronarts.lx.LXUtils;
import heronarts.lx.control.BasicParameter;
import heronarts.lx.modulator.LinearEnvelope;

import java.lang.Math;

import processing.core.PConstants;

public class FlashEffect extends LXEffect {
    
    private final LinearEnvelope flash;
    private final BasicParameter attack;
    private final BasicParameter decay;
    private final BasicParameter intensity;
    private final BasicParameter sat;
    
    public FlashEffect(HeronLX lx) {
        super(lx, true);
        this.addModulator(this.flash = new LinearEnvelope(0, 0, 0));
        this.addParameter(this.attack = new BasicParameter("ATTACK", 0.1));
        this.addParameter(this.decay = new BasicParameter("DECAY", 0.5));
        this.addParameter(this.intensity = new BasicParameter("INTENSITY", 1));
        this.addParameter(this.sat = new BasicParameter("SAT", 0));
    }
    
    private double getAttackTime() {
        return this.attack.getValue() * 1000.;
    }
    
    private double getDecayTime() {
        return this.decay.getValue() * 3000.;
    }
            
    public void onEnable() {
        this.flash.setRange(this.flash.getValue(), this.intensity.getValue(), getAttackTime()).trigger();
    }
    
    public void onDisable() {
        this.flash.setRange(this.flash.getValue(), 0, getDecayTime()).trigger();
    }
    
    public void onTrigger() {
        this.flash.setRange(this.intensity.getValue(), 0, getDecayTime()).trigger();
    }
    
    protected void doApply(int[] colors) {
        float flashValue = this.flash.getValuef();
        double satValue = this.sat.getValue() * 100.;
        double hueValue = this.lx.getBaseHue();
        if (flashValue > 0) {
            for (int i = 0; i < this.lx.total; ++i) {
                colors[i] = this.lx.applet.lerpColor(
                        colors[i],
                        this.lx.colord(hueValue, satValue, 100.),
                        flashValue,
                        PConstants.RGB);
            }
        }
    }
}
