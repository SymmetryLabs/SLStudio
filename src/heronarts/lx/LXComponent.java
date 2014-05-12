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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import heronarts.lx.modulator.LXModulator;
import heronarts.lx.parameter.LXParameterized;

public class LXComponent extends LXParameterized implements LXLoopTask {

    private final List<LXModulator> modulators = new ArrayList<LXModulator>();
    private final List<LXModulator> unmodifiableModulators = Collections.unmodifiableList(this.modulators);

    public final class Timer {
        public long loopNanos;
    }

    public final Timer timer = new Timer();

    protected final LXModulator addModulator(LXModulator modulator) {
        this.modulators.add(modulator);
        return modulator;
    }

    protected final LXComponent removeModulator(LXModulator modulator) {
        this.modulators.remove(modulator);
        return this;
    }

    public final List<LXModulator> getModulators() {
        return this.unmodifiableModulators;
    }

    @Override
    public void loop(double deltaMs) {
        for (LXModulator modulator : this.modulators) {
            modulator.loop(deltaMs);
        }
    }

}
