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

import heronarts.lx.color.LXPalette;
import heronarts.lx.midi.LXMidiAftertouch;
import heronarts.lx.midi.LXMidiControlChange;
import heronarts.lx.midi.LXMidiListener;
import heronarts.lx.midi.LXMidiNote;
import heronarts.lx.midi.LXMidiNoteOn;
import heronarts.lx.midi.LXMidiPitchBend;
import heronarts.lx.midi.LXMidiProgramChange;
import heronarts.lx.model.LXModel;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.parameter.LXParameterized;

public class LXComponent extends LXParameterized implements LXLoopTask, LXMidiListener {

    private final List<LXModulator> modulators = new ArrayList<LXModulator>();
    private final List<LXModulator> unmodifiableModulators = Collections.unmodifiableList(this.modulators);

    protected LXModel model;

    protected LXPalette palette;

    public class Timer {
        public long loopNanos;
    }

    protected Timer constructTimer() {
        return new Timer();
    }

    public final Timer timer = constructTimer();

    protected LXComponent(LX lx) {
        this.model = lx.model;
        this.palette = lx.palette;
    }

    public LXModel getModel() {
        return this.model;
    }

    public LXComponent setModel(LXModel model) {
        if (model == null) {
            throw new IllegalArgumentException("May not set null model");
        }
        if (this.model != model) {
            this.model = model;
            onModelChanged(model);
        }
        return this;
    }

    protected void onModelChanged(LXModel model) {

    }

    public LXPalette getPalette() {
        return this.palette;
    }

    public LXComponent setPalette(LXPalette palette) {
        if (palette == null) {
            throw new IllegalArgumentException("May not set null palette");
        }
        if (this.palette != palette) {
            this.palette = palette;
            onPaletteChanged(palette);
        }
        return this;
    }

    protected void onPaletteChanged(LXPalette palette) {

    }

    protected final LXModulator addModulator(LXModulator modulator) {
        this.modulators.add(modulator);
        return modulator;
    }

    protected final LXModulator removeModulator(LXModulator modulator) {
        this.modulators.remove(modulator);
        return modulator;
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

    @Override
    public void noteOnReceived(LXMidiNoteOn note) {

    }

    @Override
    public void noteOffReceived(LXMidiNote note) {

    }

    @Override
    public void controlChangeReceived(LXMidiControlChange cc) {

    }

    @Override
    public void programChangeReceived(LXMidiProgramChange cc) {

    }

    @Override
    public void pitchBendReceived(LXMidiPitchBend pitchBend) {

    }

    @Override
    public void aftertouchReceived(LXMidiAftertouch aftertouch) {

    }

}
