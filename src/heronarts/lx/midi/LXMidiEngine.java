/**
 * Copyright 2013- Mark C. Slee, Heron Arts LLC
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * @author Mark C. Slee <mark@heronarts.com>
 */

package heronarts.lx.midi;

import heronarts.lx.LX;
import heronarts.lx.LXChannel;
import heronarts.lx.LXComponent;
import heronarts.lx.LXMappingEngine;
import heronarts.lx.LXPattern;
import heronarts.lx.LXSerializable;
import heronarts.lx.parameter.LXParameter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.ShortMessage;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class LXMidiEngine implements LXSerializable {

    public enum Channel {
        CH_1, CH_2, CH_3, CH_4, CH_5, CH_6, CH_7, CH_8, CH_9, CH_10, CH_11, CH_12, CH_13, CH_14, CH_15, CH_16, OMNI;

        public boolean matches(ShortMessage message) {
            switch (this) {
            case OMNI: return true;
            default: return message.getChannel() == ordinal();
            }
        }

        public int getChannel() {
            switch (this) {
            case OMNI:
                return -1;
            default:
                return ordinal();
            }
        }

        @Override
        public String toString() {
            switch (this) {
            case OMNI:
                return "Omni";
            default:
                return "Ch." + (ordinal() + 1);
            }
        }
    }

    public interface MappingListener {
        public void mappingAdded(LXMidiEngine engine, LXMidiMapping mapping);
        public void mappingRemoved(LXMidiEngine engine, LXMidiMapping mapping);
    }

    private final List<LXMidiListener> listeners = new ArrayList<LXMidiListener>();
    private final List<MappingListener> mappingListeners = new ArrayList<MappingListener>();

    private final List<LXShortMessage> threadSafeInputQueue =
        Collections.synchronizedList(new ArrayList<LXShortMessage>());

    private final List<LXShortMessage> engineThreadInputQueue =
        new ArrayList<LXShortMessage>();

    private final List<LXMidiInput> inputs = new ArrayList<LXMidiInput>();
    private final List<LXMidiOutput> outputs = new ArrayList<LXMidiOutput>();

    private final List<LXMidiInput> unmodifiableInputs = Collections.unmodifiableList(this.inputs);
    private final List<LXMidiOutput> unmodifiableOutputs = Collections.unmodifiableList(this.outputs);

    private final List<LXMidiMapping> mappings = new ArrayList<LXMidiMapping>();
    private final List<LXMidiMapping> unmodifiableMappings = Collections.unmodifiableList(this.mappings);

    private final LX lx;

    private class InitializationLock {
        private final List<Runnable> listeners = new ArrayList<Runnable>();
        private boolean ready = false;
    }

    private final InitializationLock initializationLock = new InitializationLock();

    public LXMidiEngine(LX lx) {
        this.lx = lx;
    }

    public void initialize() {
        new Thread() {
            @Override
            public void run() {
                // NOTE(mcslee): this can sometimes hang or be slow for unclear reasons...
                // do it in a separate thread so that we don't delay the whole application
                // starting up.
                for (MidiDevice.Info deviceInfo : MidiSystem.getMidiDeviceInfo()) {
                    try {
                        MidiDevice device = MidiSystem.getMidiDevice(deviceInfo);
                        if (device.getMaxTransmitters() != 0) {
                            inputs.add(new LXMidiInput(LXMidiEngine.this, device));
                        }
                        if (device.getMaxReceivers() != 0) {
                            outputs.add(new LXMidiOutput(LXMidiEngine.this, device));
                        }
                    } catch (MidiUnavailableException mux) {
                        mux.printStackTrace();
                    }
                }
                lx.engine.addTask(new Runnable() {
                    public void run() {
                        synchronized (initializationLock) {
                            initializationLock.ready = true;
                            for (Runnable runnable : initializationLock.listeners) {
                                runnable.run();
                            }
                            initializationLock.notifyAll();
                        }
                    }
                });
            }
        }.start();
    }

    public void waitUntilReady() {
        synchronized (this.initializationLock) {
            while (!this.initializationLock.ready) {
                try {
                    this.initializationLock.wait();
                } catch (InterruptedException ix) {
                    System.err.println(ix.getLocalizedMessage());
                }
            }
        }
    }

    public void whenReady(Runnable runnable) {
        synchronized (this.initializationLock) {
            if (this.initializationLock.ready) {
                runnable.run();
            } else {
                this.initializationLock.listeners.add(runnable);
            }
        }
    }

    public List<LXMidiInput> getInputs() {
        return this.unmodifiableInputs;
    }

    public List<LXMidiOutput> getOutputs() {
        return this.unmodifiableOutputs;
    }

    public List<LXMidiMapping> getMappings() {
        return this.unmodifiableMappings;
    }

    public LXMidiInput matchInput(String name) {
        return matchInput(new String[] { name });
    }

    public LXMidiInput matchInput(String[] names) {
        return (LXMidiInput) matchDevice(this.inputs, names);
    }

    public LXMidiOutput matchOutput(String name) {
        return matchOutput(new String[] { name });
    }

    public LXMidiOutput matchOutput(String[] names) {
        return (LXMidiOutput) matchDevice(this.outputs, names);
    }

    private LXMidiDevice matchDevice(List<? extends LXMidiDevice> devices, String[] names) {
        for (LXMidiDevice device : devices) {
            String deviceName = device.getName();
            for (String name : names) {
                if (deviceName.contains(name)) {
                    return device;
                }
            }
        }
        return null;
    }

    public LXMidiEngine addListener(LXMidiListener listener) {
        this.listeners.add(listener);
        return this;
    }

    public LXMidiEngine removeListener(LXMidiListener listener) {
        this.listeners.remove(listener);
        return this;
    }

    public LXMidiEngine addMappingListener(MappingListener listener) {
        this.mappingListeners.add(listener);
        return this;
    }

    public LXMidiEngine removeMappingListener(MappingListener listener) {
        this.mappingListeners.remove(listener);
        return this;
    }

    void queueInputMessage(LXShortMessage message) {
        this.threadSafeInputQueue.add(message);
    }

    /**
     * Invoked by the main engine to dispatch all midi messages on the
     * input queue.
     */
    public void dispatch() {
        this.engineThreadInputQueue.clear();
        synchronized (this.threadSafeInputQueue) {
            this.engineThreadInputQueue.addAll(this.threadSafeInputQueue);
            this.threadSafeInputQueue.clear();
        }
        for (LXShortMessage message : this.engineThreadInputQueue) {
            LXMidiInput input = message.getInput();
            if (input.enabled.isOn()) {
                dispatch(message);
                input.dispatch(message);
            }
        }
    }

    private void createMapping(LXShortMessage message) {
        // Is there a control parameter selected?
        LXParameter parameter = lx.engine.mapping.getControlTarget();
        if (parameter == null) {
            return;
        }

        // Is this a valid mapping type?
        if (!LXMidiMapping.isValidMessageType(message)) {
            return;
        }

        // Does this mapping already exist?
        for (LXMidiMapping mapping : this.mappings) {
            if (mapping.parameter == parameter && mapping.matches(message)) {
                return;
            }
        }

        // Bada-boom, add it!
        addMapping(LXMidiMapping.create(this.lx, message, parameter));
    }

    private boolean applyMapping(LXShortMessage message) {
        boolean applied = false;
        for (LXMidiMapping mapping : this.mappings) {
            if (mapping.matches(message)) {
                mapping.apply(message);
                applied = true;
            }
        }
        return applied;
    }

    private LXMidiEngine addMapping(LXMidiMapping mapping) {
        this.mappings.add(mapping);
        for (MappingListener mappingListener : this.mappingListeners) {
            mappingListener.mappingAdded(this, mapping);
        }
        return this;
    }

    /**
     * Removes a midi mapping
     *
     * @param mapping The mapping to remove
     * @return this
     */
    public LXMidiEngine removeMapping(LXMidiMapping mapping) {
        this.mappings.remove(mapping);
        for (MappingListener mappingListener : this.mappingListeners) {
            mappingListener.mappingRemoved(this, mapping);
        }
        return this;
    }

    /**
     * Called when a component is disposed. Remove any midi mappings
     * pointing to the now-nonexistent component.
     *
     * @param component
     * @return
     */
    public LXMidiEngine removeMappings(LXComponent component) {
        Iterator<LXMidiMapping> iterator = this.mappings.iterator();
        while (iterator.hasNext()) {
            LXMidiMapping mapping = iterator.next();
            if (mapping.parameter.getComponent() == component) {
                iterator.remove();
                for (MappingListener mappingListener : this.mappingListeners) {
                    mappingListener.mappingRemoved(this, mapping);
                }
            }
        }
        return this;
    }

    public void dispatch(LXShortMessage message) {
        LXMidiInput input = message.getInput();
        if (input != null) {
            if (input.controlEnabled.isOn()) {
                if (lx.engine.mapping.getMode() == LXMappingEngine.Mode.MIDI) {
                    createMapping(message);
                    return;
                }
                if (applyMapping(message)) {
                    return;
                }
            }
            if (input.syncEnabled.isOn() && message instanceof MidiBeat) {
                MidiBeat beat = (MidiBeat) message;
                this.lx.tempo.trigger(((MidiBeat) message).getBeat());
                double period = beat.getPeriod();
                if (period != MidiBeat.PERIOD_UNKNOWN) {
                    this.lx.tempo.setPeriod(period);
                }
            }
        }

        for (LXMidiListener listener : this.listeners) {
            dispatch(message, listener);
        }

        if (input == null || input.channelEnabled.isOn()) {
            for (LXChannel channel : this.lx.engine.getChannels()) {
                if (channel.midiMonitor.isOn() && channel.midiChannel.getEnum().matches(message)) {
                    channel.midiMessage(message);
                    dispatch(message, channel.getActivePattern());
                    LXPattern nextPattern = channel.getNextPattern();
                    if (nextPattern != null) {
                        dispatch(message, nextPattern);
                    }
                }
            }
        }
    }

    public void dispatch(LXShortMessage message, LXMidiListener listener) {
        switch (message.getCommand()) {
        case ShortMessage.NOTE_ON:
            MidiNoteOn note = (MidiNoteOn) message;
            if (note.getVelocity() == 0) {
                listener.noteOffReceived(note);
            } else {
                listener.noteOnReceived(note);
            }
            break;
        case ShortMessage.NOTE_OFF:
            listener.noteOffReceived((MidiNoteOff) message);
            break;
        case ShortMessage.CONTROL_CHANGE:
            listener.controlChangeReceived((MidiControlChange) message);
            break;
        case ShortMessage.PROGRAM_CHANGE:
            listener.programChangeReceived((MidiProgramChange) message);
            break;
        case ShortMessage.PITCH_BEND:
            listener.pitchBendReceived((MidiPitchBend) message);
            break;
        case ShortMessage.CHANNEL_PRESSURE:
            listener.aftertouchReceived((MidiAftertouch) message);
            break;
        }
    }

    private static final String KEY_INPUTS = "inputs";
    private static final String KEY_MAPPINGS = "mapping";

    private final List<String> rememberMidiInputs = new ArrayList<String>();

    @Override
    public void save(LX lx, JsonObject object) {
        waitUntilReady();
        JsonArray inputs = new JsonArray();
        for (LXMidiInput input : this.inputs) {
            if (input.enabled.isOn()) {
                inputs.add(input.getName());
            }
        }
        for (String remembered : this.rememberMidiInputs) {
            inputs.add(remembered);
        }
        object.add(KEY_INPUTS, inputs);
        object.add(KEY_MAPPINGS, LXSerializable.Utils.toArray(lx, this.mappings));
    }

    @Override
    public void load(LX lx, final JsonObject object) {
        this.rememberMidiInputs.clear();
        this.mappings.clear();
        if (object.has(KEY_MAPPINGS)) {
            JsonArray mappings = object.getAsJsonArray(KEY_MAPPINGS);
            for (JsonElement element : mappings) {
                addMapping(LXMidiMapping.create(this.lx, element.getAsJsonObject()));
            }
        }
        whenReady(new Runnable() {
            public void run() {
                if (object.has(KEY_INPUTS)) {
                    JsonArray inputNames = object.getAsJsonArray(KEY_INPUTS);
                    if (inputNames.size() > 0) {
                        for (JsonElement element : inputNames) {
                            String inputName = element.getAsString();
                            boolean found = false;
                            for (LXMidiInput input : inputs) {
                                if (inputName.equals(input.getName())) {
                                    found = true;
                                    input.enabled.setValue(true);
                                }
                            }
                            if (!found) {
                                rememberMidiInputs.add(inputName);
                            }
                        }
                    }
                }
            }
        });
    }

}
