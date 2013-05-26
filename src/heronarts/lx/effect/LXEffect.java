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
import heronarts.lx.control.LXParameterized;
import heronarts.lx.modulator.LXModulator;

import java.util.ArrayList;

/**
 * Class to represent an effect that may be applied to the color
 * array. Effects may be stateless or stateful, though typically they
 * operate on a single frame. Only the current frame is provided at
 * runtime.
 */
public abstract class LXEffect extends LXParameterized {
    
    protected final HeronLX lx;
    private final boolean momentary;
    protected boolean enabled = false;

    protected final ArrayList<LXModulator> modulators = new ArrayList<LXModulator>();
    
    protected LXEffect(HeronLX lx) {
        this(lx, false);
    }
    
    protected LXEffect(HeronLX lx, boolean momentary) {
        this.lx = lx;
        this.momentary = momentary;
    }
    
    protected final LXModulator addModulator(LXModulator m) {
        this.modulators.add(m);
        return m;
    }
    
    public final boolean isEnabled() {
        return this.enabled;
    }
    
    public final boolean isMomentary() {
        return this.momentary;
    }
    
    public final void toggle() {
        if (this.enabled) {
            this.disable();
        } else {
            this.enable();
        }
    }
    
    /**
     * Enables the effect.
     */
    public final void enable() {
        if (!this.enabled) {
            this.enabled = true;
            this.onEnable();
        }
    }
    
    /**
     * Disables the effect.
     */
    public final void disable() {
        if (this.enabled) {
            this.enabled = false;
            this.onDisable();
        }
    }
    
    /**
     * This is for momentary effects, which don't work on a normal
     * enabled/disabled model. They are triggered by a single event,
     * not for instance by pushing and then releasing a button.
     * 
     * If the effect is already active, this just disables it.
     */
    public final void trigger() {
        if (this.enabled) {
            this.disable();
        } else {
            this.onTrigger();
        }
    }    
    
    protected /* abstract */ void onEnable() {}
    protected /* abstract */ void onDisable() {}
    protected /* abstract */ void onTrigger() {}
    
    /**
     * Applies this effect to the current frame
     * 
     * @param colors Array of this frame's colors
     * @param deltaMs Milliseconds since last frame 
     */
    public final void apply(int[] colors, int deltaMs) {
        for (LXModulator m : this.modulators) {
            m.run(deltaMs);
        }
        this.doApply(colors);
    }
    
    protected abstract void doApply(int[] colors);
}
