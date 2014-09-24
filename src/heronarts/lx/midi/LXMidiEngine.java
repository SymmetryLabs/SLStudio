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

package heronarts.lx.midi;

import heronarts.lx.LX;
import heronarts.lx.LXChannel;
import heronarts.lx.effect.LXEffect;
import heronarts.lx.pattern.LXPattern;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.sound.midi.ShortMessage;

public class LXMidiEngine {

    private final List<LXMidiListener> listeners = new ArrayList<LXMidiListener>();

    private final List<LXShortMessage> midiThreadEventQueue =
        Collections.synchronizedList(new ArrayList<LXShortMessage>());

    private final List<LXShortMessage> localThreadEventQueue =
        new ArrayList<LXShortMessage>();

    final LX lx;

    public LXMidiEngine(LX lx) {
        this.lx = lx;
    }

    public LXMidiEngine addInput(LXMidiInput input) {
        input.setEngineInput(true);
        return this;
    }

    public LXMidiEngine addInput(String deviceName) {
        LXMidiInput input = LXMidiSystem.matchInput(this.lx, deviceName);
        if (input != null) {
            addInput(input);
        }
        return this;
    }

    public LXMidiEngine addListener(LXMidiListener listener) {
        this.listeners.add(listener);
        return this;
    }

    public LXMidiEngine removeListener(LXMidiListener listener) {
        this.listeners.remove(listener);
        return this;
    }

    void queueMessage(LXShortMessage message) {
        this.midiThreadEventQueue.add(message);
    }

    /**
     * Invoked by the main engine to dispatch all midi messages on the
     * input queue.
     */
    public void dispatch() {
        this.localThreadEventQueue.clear();
        synchronized (this.midiThreadEventQueue) {
            this.localThreadEventQueue.addAll(this.midiThreadEventQueue);
            this.midiThreadEventQueue.clear();
        }
        for (LXShortMessage message : this.localThreadEventQueue) {
            message.getInput().dispatch(message);
        }
    }

    public void dispatch(LXShortMessage message) {
        for (LXMidiListener listener : this.listeners) {
            dispatch(message, listener);
        }
        for (LXChannel channel : this.lx.engine.getChannels()) {
            if (channel.midiEnabled.isOn()) {
                dispatch(message, channel.getActivePattern());
                LXPattern nextPattern = channel.getNextPattern();
                if (nextPattern != null) {
                    dispatch(message, nextPattern);
                }
                for (LXEffect effect : channel.getEffects()) {
                    dispatch(message, effect);
                }
            }
        }
    }

    void dispatch(LXShortMessage message, LXMidiListener listener) {
        switch (message.getCommand()) {
        case ShortMessage.NOTE_ON:
            LXMidiNoteOn note = (LXMidiNoteOn) message;
            if (note.getVelocity() == 0) {
                listener.noteOffReceived(note);
            } else {
                listener.noteOnReceived(note);
            }
            break;
        case ShortMessage.NOTE_OFF:
            listener.noteOffReceived((LXMidiNoteOff) message);
            break;
        case ShortMessage.CONTROL_CHANGE:
            listener.controlChangeReceived((LXMidiControlChange) message);
            break;
        case ShortMessage.PROGRAM_CHANGE:
            listener.programChangeReceived((LXMidiProgramChange) message);
            break;
        case ShortMessage.PITCH_BEND:
            listener.pitchBendReceived((LXMidiPitchBend) message);
            break;
        case ShortMessage.CHANNEL_PRESSURE:
            listener.aftertouchReceived((LXMidiAftertouch) message);
            break;
        }
    }

}
