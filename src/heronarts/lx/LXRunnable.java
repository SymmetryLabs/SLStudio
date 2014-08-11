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

package heronarts.lx;

import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.parameter.LXParameterized;

public abstract class LXRunnable extends LXParameterized implements LXLoopTask {

    /**
     * Whether this modulator is currently running.
     */
    public final BooleanParameter isRunning = new BooleanParameter("RUN", false);

    protected LXRunnable() {
        addParameter(this.isRunning);
        this.isRunning.addListener(new LXParameterListener() {
            @Override
            public void onParameterChanged(LXParameter parameter) {
                if (LXRunnable.this.isRunning.isOn()) {
                    onStart();
                } else {
                    onStop();
                }
            }
        });
    }

    /**
     * Sets the runnable in motion
     */
    public final LXRunnable start() {
        this.isRunning.setValue(true);
        return this;
    }

    /**
     * Pauses the runnable wherever it is. Internal state should be maintained. A
     * subsequent call to start() should result in the runnable continuing as it
     * was running before.
     */
    public final LXRunnable stop() {
        this.isRunning.setValue(false);
        return this;
    }

    /**
     * Indicates whether this runnable is running.
     */
    public final boolean isRunning() {
        return this.isRunning.isOn();
    }

    /**
     * Invoking the trigger() method restarts a runnable from its initial state,
     * and should also start the runnable if it is not already running.
     */
    public final LXRunnable trigger() {
        return this.reset().start();
    }

    /**
     * Resets the runnable to its default condition and stops it.
     *
     * @return this, for method chaining
     */
    public final LXRunnable reset() {
        this.stop();
        this.onReset();
        return this;
    }

    /**
     * Optional subclass method when start happens.
     */
    protected/* abstract */void onStart() {

    }

    /**
     * Optional subclass method when stop happens.
     */
    protected/* abstract */void onStop() {

    }

    /**
     * Optional subclass method when reset happens.
     */
    protected/* abstract */void onReset() {
    }

    @Override
    public void loop(double deltaMs) {
        if (this.isRunning.isOn()) {
            run(deltaMs);
        }
    }

    protected abstract void run(double deltaMs);

}
