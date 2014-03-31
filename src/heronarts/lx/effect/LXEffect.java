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
import heronarts.lx.LXLayer;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;

/**
 * Class to represent an effect that may be applied to the color array. Effects
 * may be stateless or stateful, though typically they operate on a single
 * frame. Only the current frame is provided at runtime.
 */
public abstract class LXEffect extends LXLayer {

    protected final LX lx;
    private final boolean isMomentary;

    public final BooleanParameter enabled = new BooleanParameter("ENABLED", false);

    public class Timer {
        public long runNanos = 0;
    }

    public final Timer timer = new Timer();

    protected LXEffect(LX lx) {
        this(lx, false);
    }

    protected LXEffect(LX lx, boolean isMomentary) {
        this.lx = lx;
        this.isMomentary = isMomentary;
        this.enabled.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter parameter) {
                if (LXEffect.this.enabled.isOn()) {
                    onEnable();
                } else {
                    onDisable();
                }
            }
        });
    }

    /**
     * @return whether the effect is currently enabled
     */
    public final boolean isEnabled() {
        return this.enabled.isOn();
    }

    /**
     * @return Whether this is a momentary effect or not
     */
    public final boolean isMomentary() {
        return this.isMomentary;
    }

    /**
     * Toggles the effect.
     * 
     * @return this
     */
    public final LXEffect toggle() {
        this.enabled.toggle();
        return this;
    }

    /**
     * Enables the effect.
     * 
     * @return this
     */
    public final LXEffect enable() {
        this.enabled.setValue(true);
        return this;
    }

    /**
     * Disables the effect.
     */
    public final LXEffect disable() {
        this.enabled.setValue(false);
        return this;
    }

    /**
     * This is to trigger special one-shot effects. If the effect is enabled, then
     * it is disabled. Otherwise, it's enabled state is never changed and it
     * simply has its onTrigger method invoked.
     */
    public final void trigger() {
        if (this.enabled.isOn()) {
            this.disable();
        } else {
            this.onTrigger();
        }
    }

    protected/* abstract */void onEnable() {
    }

    protected/* abstract */void onDisable() {
    }

    protected/* abstract */void onTrigger() {
    }

    /**
     * Applies this effect to the current frame
     * 
     * @param deltaMs Milliseconds since last frame
     * @param colors Array of this frame's colors
     */
    public final void run(double deltaMs, int[] colors) {
        long runStart = System.nanoTime();
        for (LXModulator m : this.modulators) {
            m.run(deltaMs);
        }
        this.apply(colors);
        for (LXLayer layer : this.layers) {
            layer.run(deltaMs, colors);
        }
        this.timer.runNanos = System.nanoTime() - runStart;
    }

    /**
     * Implementation of the effect. Subclasses need to override this to implement
     * their functionality.
     * 
     * @param colors
     */
    protected abstract void apply(int[] colors);
}
