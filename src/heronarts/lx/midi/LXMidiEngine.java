/**
 * Copyright 2013- Mark C. Slee, Heron Arts LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author Mark C. Slee <mark@heronarts.com>
 */

package heronarts.lx.midi;

import heronarts.lx.LX;
import heronarts.lx.LXChannel;
import heronarts.lx.LXEffect;
import heronarts.lx.LXPattern;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.sound.midi.ShortMessage;

public class LXMidiEngine {

    private final List<LXMidiListener> listeners = new ArrayList<LXMidiListener>();

    private final List<LXShortMessage> threadSafeEventQueue =
        Collections.synchronizedList(new ArrayList<LXShortMessage>());

    private final List<LXShortMessage> engineThreadEventQueue =
        new ArrayList<LXShortMessage>();

    final LX lx;

    public LXMidiEngine(LX lx) {
        this.lx = lx;
    }

    public LXMidiEngine addInput(LXMidiInput input) {
        input.setEngineInput(true);
        return this;
    }

    public LXMidiEngine removeInput(LXMidiInput input) {
        input.setEngineInput(false);
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
        this.threadSafeEventQueue.add(message);
    }

    /**
     * Invoked by the main engine to dispatch all midi messages on the
     * input queue.
     */
    public void dispatch() {
        this.engineThreadEventQueue.clear();
        synchronized (this.threadSafeEventQueue) {
            this.engineThreadEventQueue.addAll(this.threadSafeEventQueue);
            this.threadSafeEventQueue.clear();
        }
        for (LXShortMessage message : this.engineThreadEventQueue) {
            message.getInput().dispatch(message);
        }
    }

    private final List<LXMidiListener> listenerSnapshot = new ArrayList<LXMidiListener>();

    public void dispatch(LXShortMessage message) {
        this.listenerSnapshot.clear();
        this.listenerSnapshot.addAll(this.listeners);
        for (LXMidiListener listener : this.listenerSnapshot) {
            dispatch(message, listener);
        }
        for (LXChannel channel : this.lx.engine.getChannels()) {
            if (channel.midiMonitor.isOn()) {
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
        // TODO(mcslee): send MIDI to the master FX bus? should effects really
        // monitor all MIDI input, or just patterns? with a richer MIDI mapping
        // implementation effects could just get control...
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
