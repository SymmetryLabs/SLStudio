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

package heronarts.lx;

import heronarts.lx.midi.MidiAftertouch;
import heronarts.lx.midi.MidiControlChange;
import heronarts.lx.midi.LXMidiListener;
import heronarts.lx.midi.MidiNote;
import heronarts.lx.midi.MidiNoteOn;
import heronarts.lx.midi.MidiPitchBend;
import heronarts.lx.midi.MidiProgramChange;
import heronarts.lx.midi.LXShortMessage;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.StringParameter;
import heronarts.lx.parameter.LXListenableParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.osc.LXOscComponent;
import heronarts.lx.osc.OscMessage;
import heronarts.lx.osc.LXOscEngine;
import heronarts.lx.warp.LXWarp;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * An automation recorder contains meta-data about all the controls on the
 * patterns and effects in the system, which can be recorded or played back.
 *
 * This is deprecated and replaced by the more robust LXClip framework.
 */
@Deprecated
public class LXAutomationRecorder extends LXRunnableComponent implements LXEngine.MessageListener, LXMidiListener, LXOscComponent {

    private final static String EVENT_PATTERN = "PATTERN";
    private final static String EVENT_PARAMETER = "PARAMETER";
    private final static String EVENT_MESSAGE = "MESSAGE";
    private final static String EVENT_MIDI = "MIDI";
    private final static String EVENT_FINISH = "FINISH";

    private final static String KEY_EVENT = "event";
    private final static String KEY_MILLIS = "millis";
    private final static String KEY_CHANNEL = "channel";
    private final static String KEY_PATTERN = "pattern";
    private final static String KEY_PARAMETER = "parameter";
    private final static String KEY_VALUE = "value";
    private final static String KEY_COMMAND = "command";
    private final static String KEY_DATA_1 = "data1";
    private final static String KEY_DATA_2 = "data2";
    private final static String KEY_MESSAGE = "message";

    private final static String DEFAULT_VEZER_IP_ADDRESS = "localhost";
    private final static String DEFAULT_VEZER_OSC_PORT = "7777";
    private final static String DEFAULT_VEZER_SEQUENCE = "";

    private final LX lx;
    private final LXEngine engine;
    private LXOscEngine.Transmitter osc = null;

    public final BooleanParameter armRecord = new BooleanParameter("ARM", false);
    public final BooleanParameter triggerVezer = new BooleanParameter("VEZER", false);

    public final StringParameter vezerIpAddress = new StringParameter("IpAddress", DEFAULT_VEZER_IP_ADDRESS);
    public final StringParameter vezerOscPort = new StringParameter("Port", DEFAULT_VEZER_OSC_PORT);
    public final StringParameter vezerSequence = new StringParameter("Sequence", DEFAULT_VEZER_SEQUENCE);

    public final BooleanParameter looping = new BooleanParameter("LOOP", false);

    private final List<LXChannel> channels = new ArrayList<LXChannel>();
    private final List<LXParameter> automatableParameters = new ArrayList<>();

    private final List<LXAutomationEvent> events = new ArrayList<LXAutomationEvent>();
    private final Map<String, LXParameter> pathToParameter = new HashMap<String, LXParameter>();
    private final Map<LXParameter, String> parameterToPath = new HashMap<LXParameter, String>();

    private int cursor = 0;
    private double elapsedMillis = 0;

    private FinishAutomationEvent lastDuration = null;

    public LXAutomationRecorder(LX lx, LXEngine engine) {
        super(lx, "automation");
        this.lx = lx;
        this.engine = engine;
        setParent(engine);
        engine.addParameter(triggerVezer);
        engine.addParameter(vezerSequence);
        engine.addParameter(vezerOscPort);
        engine.addParameter(vezerIpAddress);

        try {
            int port = Integer.parseInt(vezerOscPort.getString());
            this.osc = engine.osc.transmitter(vezerIpAddress.getString(), port);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        vezerIpAddress.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter parameter) {
                try {
                    osc.setHost(vezerIpAddress.getString());
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }
        });
        vezerOscPort.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter parameter) {
                osc.setPort(Integer.parseInt(vezerOscPort.getString()));
            }
        });
    }

    private abstract class LXAutomationEvent {

        private final String eventType;
        public double millis;

        private LXAutomationEvent(String eventType) {
            this.eventType = eventType;
            this.millis = elapsedMillis;
        }

        public final JsonObject toJson() {
            JsonObject jsonObj = new JsonObject();
            jsonObj.addProperty(KEY_EVENT, this.eventType);
            jsonObj.addProperty(KEY_MILLIS, (float) this.millis);
            toJson(jsonObj);
            return jsonObj;
        }

        protected abstract void toJson(JsonObject jsonObj);

        abstract void play();
    }

    private class PatternAutomationEvent extends LXAutomationEvent {

        final LXChannel channel;
        final LXPattern pattern;

        private PatternAutomationEvent(LXChannel channel, LXPattern pattern) {
            super(EVENT_PATTERN);
            this.channel = channel;
            this.pattern = pattern;
        }

        @Override
        void play() {
            this.channel.goPattern(this.pattern);
        }

        @Override
        protected void toJson(JsonObject jsonObj) {
            jsonObj.addProperty(KEY_CHANNEL, this.channel.getIndex());
            jsonObj.addProperty(KEY_PATTERN, "class name"); //this.pattern.getClass().getName());
        }
    }

    private class ParameterAutomationEvent extends LXAutomationEvent {

        final LXParameter parameter;
        final double value;

        private ParameterAutomationEvent(LXParameter parameter) {
            this(parameter, parameter.getValue());
        }

        private ParameterAutomationEvent(LXParameter parameter, double value) {
            super(EVENT_PARAMETER);
            this.parameter = parameter;
            this.value = value;
        }

        @Override
        void play() {
            this.parameter.setValue(this.value);
        }

        @Override
        protected void toJson(JsonObject jsonObj) {
            jsonObj.addProperty(KEY_PARAMETER, parameterToPath.get(this.parameter));
            jsonObj.addProperty(KEY_VALUE, (float) this.value);
        }
    }

    private class MidiAutomationEvent extends LXAutomationEvent {

        private final LXShortMessage shortMessage;

        private MidiAutomationEvent(LXShortMessage shortMessage) {
            super(EVENT_MIDI);
            this.shortMessage = shortMessage;
        }

        @Override
        protected void toJson(JsonObject jsonObj) {
            jsonObj.addProperty(KEY_CHANNEL, this.shortMessage.getChannel());
            jsonObj.addProperty(KEY_COMMAND, this.shortMessage.getCommand());
            jsonObj.addProperty(KEY_DATA_1, this.shortMessage.getData1());
            jsonObj.addProperty(KEY_DATA_2, this.shortMessage.getData2());
        }

        @Override
        void play() {
            engine.midi.dispatch(this.shortMessage);
        }
    }

    private class MessageAutomationEvent extends LXAutomationEvent {

        private final String message;

        private MessageAutomationEvent(String message) {
            super(EVENT_MESSAGE);
            this.message = message;
        }

        @Override
        protected void toJson(JsonObject jsonObj) {
            jsonObj.addProperty(KEY_MESSAGE, this.message);
        }

        @Override
        void play() {
            engine.broadcastMessage(this.message);
        }
    }

    private class FinishAutomationEvent extends LXAutomationEvent {

        private FinishAutomationEvent() {
            super(EVENT_FINISH);
            lastDuration = this;
        }

        @Override
        void play() {
            if (looping.isOn()) {
                elapsedMillis = 0;
                cursor = 0;
            } else {
                reset();
            }
        }

        @Override
        protected void toJson(JsonObject jsonObj) {
            // Nothing extra needed
        }
    }

    public boolean isEmpty() {
        return !(events.size() > 0);
    }

    public String getDurationString() {
        if (lastDuration != null) {
            return millisToString((int) lastDuration.millis);
        }
        return millisToString(0);
    }

    public String getPlayheadString() {
        return millisToString((int) elapsedMillis);
    }

    private String millisToString(int millis) {
        if (millis == 0) {
            return "0:0:0:0";
        }
        return millis / (60*60*1000) % 24 + ":" + // hours
               millis / (60*1000) % 60 + ":" + // minutes
               millis / 1000 % 60 + ":" + // seconds
               (int) (((float) millis % 1000 / 1000) * 30); // approximation of "frames"
    }

    public String getOscAddress() {
        return ((LXOscComponent) this.engine).getOscAddress() + "/automation";
    }

    private LXAutomationRecorder registerEngine() {
        for (LXParameter parameter : this.engine.getParameters()) {
            if (parameter instanceof LXListenableParameter) {
                registerParameter("engine/" + parameter.getLabel(), (LXListenableParameter) parameter);
            }
        }
        return this;
    }

    private LXAutomationRecorder registerChannel(LXChannel channel) {
        String path = "channel/" + channel.getIndex();
        this.channels.add(channel);
        channel.addListener(new LXChannel.AbstractListener() {
            @Override
            public void patternWillChange(LXChannel channel, LXPattern pattern,
                    LXPattern nextPattern) {
                if (armRecord.isOn()) {
                    events.add(new PatternAutomationEvent(channel, nextPattern));
                }
            }
        });
        registerParameter(path + "/fader", channel.fader);
        registerParameter(path + "/blendMode", channel.blendMode);
        for (LXPattern pattern : channel.getPatterns()) {
            registerComponent(path + "/pattern/" + pattern.getClass().getName(), pattern);
        }
        for (LXEffect effect : channel.getEffects()) {
            registerComponent(path + "/effect/" + effect.getClass().getName(), effect);
        }
        for (LXWarp warp : channel.getWarps()) {
            registerComponent(path + "/warp/" + warp.getClass().getName(), warp);
        }

        return this;
    }

    private LXAutomationRecorder registerComponent(String prefix, LXComponent component) {
        for (LXParameter parameter : component.getParameters()) {
            if (parameter instanceof LXListenableParameter) {
                registerParameter(prefix + "/" + parameter.getLabel(), (LXListenableParameter) parameter);
            }
        }
        return this;
    }

    private LXAutomationRecorder registerParameter(String path, LXListenableParameter parameter) {
        this.pathToParameter.put(path, parameter);
        this.parameterToPath.put(parameter, path);
        this.automatableParameters.add(parameter);

        parameter.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter parameter) {
                if (armRecord.isOn()) {
                    if (parameterToPath.containsKey(parameter)) {
                        events.add(new ParameterAutomationEvent(parameter));
                    }
                    //System.out.println("Created an event for " + parameter.getLabel());
                }
            }
        });
        return this;
    }

    private void playVezerSequence() {
        triggerVezerSequence(1);
    }

    private void pauseVezerSequence() {
        triggerVezerSequence(0);
    }

    private void triggerVezerSequence(int on) {
        if (!triggerVezer.isOn()) {
            return;
        }
        OscMessage message = new OscMessage("/vezer/" + vezerSequence.getString() + "/play").add(on); // something like this.
        try {
            osc.send(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        this.elapsedMillis = 0;
        if (this.armRecord.isOn()) {
            initialize();
            for (LXParameter parameter : automatableParameters) {
                this.events.add(new ParameterAutomationEvent(parameter));
            }
            for (LXChannel channel : this.channels) {
                this.events.add(new PatternAutomationEvent(channel, channel.getActivePattern()));
            }
            playVezerSequence();
        } else {
            lx.engine.audio.output.trigger.setValue(true);
        }
    }

    @Override
    protected void onStop() {
        if (this.armRecord.isOn()) {
            this.events.add(new FinishAutomationEvent());
            this.armRecord.setValue(false);
        }
        pauseVezerSequence();
    }

    @Override
    protected void onReset() {
        this.cursor = 0;
        this.elapsedMillis = 0;
    }

    @Override
    protected void run(double deltaMs) {
        this.elapsedMillis += deltaMs;
        if (!this.armRecord.isOn()) {
            if (isEmpty()) {
                stop();
                return;
            }
            while (isRunning() && (this.cursor < this.events.size())) {
                LXAutomationEvent event = this.events.get(this.cursor);
                if (this.elapsedMillis < event.millis) {
                    return;
                }
                ++this.cursor;
                event.play();
            }
        }
    }

    // @Override
    // public void onParameterChanged(LXParameter parameter) {
    //     if (this.armRecord.isOn()) {
    //         if (this.parameterToPath.containsKey(parameter)) {
    //             this.events.add(new ParameterAutomationEvent(parameter));
    //         }
    //         System.out.println("Created an event for " + parameter.getLabel());
    //     }
    // }

    public final JsonArray toJson() {
        JsonArray jsonArr = new JsonArray();
        for (LXAutomationEvent event : this.events) {
            jsonArr.add(event.toJson());
        }
        return jsonArr;
    }

    public void clear() {
        channels.clear();
        automatableParameters.clear();
        events.clear();
        pathToParameter.clear();
        parameterToPath.clear();
    }

    private void initialize() {
        clear();
        registerEngine();
        for (LXChannel channel : engine.getChannels()) {
            registerChannel(channel);
        }
        for (LXEffect effect : engine.masterChannel.getEffects()) {
            registerComponent("effect/" + effect.getClass().getName(), effect);
        }
        for (LXWarp warp : engine.masterChannel.getWarps()) {
            registerComponent("warp/" + warp.getClass().getName(), warp);
        }
        engine.midi.addListener(this);
        engine.addMessageListener(this);
    }

    @Override
    public void save(LX lx, JsonObject obj) {
        obj.add("AUTOMATION_EVENTS", toJson());
    }

    @Override
    public void load(LX lx, JsonObject json) {
        // Remove everything first
        clear();
        initialize();

        if (json.has("AUTOMATION_EVENTS")) {
            JsonArray jsonArr = json.getAsJsonArray("AUTOMATION_EVENTS");

            for (JsonElement element : jsonArr) {
                try {
                    JsonObject obj = element.getAsJsonObject();
                    LXAutomationEvent event = null;
                    String eventType = obj.get(KEY_EVENT).getAsString();
                    if (eventType.equals(EVENT_PARAMETER)) {
                        String parameterPath = obj.get(KEY_PARAMETER).getAsString();
                        LXParameter parameter = pathToParameter.get(parameterPath);
                        if (parameter != null) {
                            event = new ParameterAutomationEvent(parameter, obj.get(KEY_VALUE).getAsFloat());
                        } else {
                            System.err.println("LXAutomationRecorder::Invalid parameter: " + parameterPath);
                        }
                    } else if (eventType.equals(EVENT_PATTERN)) {
                        int channelIndex = obj.get(KEY_CHANNEL).getAsInt();
                        String patternClassName = obj.get(KEY_PATTERN).getAsString();
                        LXChannel channel = this.engine.getChannel(channelIndex);
                        LXPattern pattern = channel.getPatternByClassName(patternClassName);
                        event = new PatternAutomationEvent(channel, pattern);
                    } else if (eventType.equals(EVENT_MIDI)) {
                        int command = obj.get(KEY_COMMAND).getAsInt();
                        int channel = obj.get(KEY_CHANNEL).getAsInt();
                        int data1 = obj.get(KEY_DATA_1).getAsInt();
                        int data2 = obj.get(KEY_DATA_2).getAsInt();
                        try {
                            ShortMessage sm = new ShortMessage();
                            sm.setMessage(command, channel, data1, data2);
                            event = new MidiAutomationEvent(LXShortMessage.fromShortMessage(sm));
                        } catch (InvalidMidiDataException imdx) {
                            System.err.println("LXAutomationRecorder::Invalid midi data: " + imdx.getMessage());
                        }
                    } else if (eventType.equals(EVENT_MESSAGE)) {
                        String message = obj.get(KEY_MESSAGE).getAsString();
                        event = new MessageAutomationEvent(message);
                    } else if (eventType.equals(EVENT_FINISH)) {
                        event = new FinishAutomationEvent();
                    }
                    if (event != null) {
                        event.millis = obj.get(KEY_MILLIS).getAsFloat();
                        this.events.add(event);
                    }
                } catch (UnsupportedOperationException uox) {
                    System.err.println("LXAutomationRecorder::Invalid automation event: " + element);
                }
            }
        }
    }

    // public final void loadJson(JsonArray jsonArr) {
    //     this.events.clear();
    //     for (JsonElement element : jsonArr) {
    //         try {
    //             JsonObject obj = element.getAsJsonObject();
    //             LXAutomationEvent event = null;
    //             String eventType = obj.get(KEY_EVENT).getAsString();
    //             if (eventType.equals(EVENT_PARAMETER)) {
    //                 String parameterPath = obj.get(KEY_PARAMETER).getAsString();
    //                 LXParameter parameter = pathToParameter.get(parameterPath);
    //                 if (parameter != null) {
    //                     event = new ParameterAutomationEvent(parameter, obj.get(KEY_VALUE).getAsFloat());
    //                 } else {
    //                     System.err.println("LXAutomationRecorder::Invalid parameter: " + parameterPath);
    //                 }
    //             } else if (eventType.equals(EVENT_PATTERN)) {
    //                 int channelIndex = obj.get(KEY_CHANNEL).getAsInt();
    //                 String patternClassName = obj.get(KEY_PATTERN).getAsString();
    //                 LXChannel channel = this.engine.getChannel(channelIndex);
    //                 LXPattern pattern = channel.getPatternByClassName(patternClassName);
    //                 event = new PatternAutomationEvent(channel, pattern);
    //             } else if (eventType.equals(EVENT_MIDI)) {
    //                 int command = obj.get(KEY_COMMAND).getAsInt();
    //                 int channel = obj.get(KEY_CHANNEL).getAsInt();
    //                 int data1 = obj.get(KEY_DATA_1).getAsInt();
    //                 int data2 = obj.get(KEY_DATA_2).getAsInt();
    //                 try {
    //                     ShortMessage sm = new ShortMessage();
    //                     sm.setMessage(command, channel, data1, data2);
    //                     event = new MidiAutomationEvent(LXShortMessage.fromShortMessage(sm));
    //                 } catch (InvalidMidiDataException imdx) {
    //                     System.err.println("LXAutomationRecorder::Invalid midi data: " + imdx.getMessage());
    //                 }
    //             } else if (eventType.equals(EVENT_MESSAGE)) {
    //                 String message = obj.get(KEY_MESSAGE).getAsString();
    //                 event = new MessageAutomationEvent(message);
    //             } else if (eventType.equals(EVENT_FINISH)) {
    //                 event = new FinishAutomationEvent();
    //             }
    //             if (event != null) {
    //                 event.millis = obj.get(KEY_MILLIS).getAsFloat();
    //                 this.events.add(event);
    //             }
    //         } catch (UnsupportedOperationException uox) {
    //             System.err.println("LXAutomationRecorder::Invalid automation event: " + element);
    //         }
    //     }
    // }

    private void midiEventReceived(LXShortMessage shortMessage) {
        if (this.armRecord.isOn()) {
            this.events.add(new MidiAutomationEvent(shortMessage));
        }
    }

    @Override
    public void noteOnReceived(MidiNoteOn note) {
        midiEventReceived(note);
    }

    @Override
    public void noteOffReceived(MidiNote note) {
        midiEventReceived(note);
    }

    @Override
    public void controlChangeReceived(MidiControlChange cc) {
        midiEventReceived(cc);
    }

    @Override
    public void programChangeReceived(MidiProgramChange pc) {
        midiEventReceived(pc);
    }

    @Override
    public void pitchBendReceived(MidiPitchBend pitchBend) {
        midiEventReceived(pitchBend);
    }

    @Override
    public void aftertouchReceived(MidiAftertouch aftertouch) {
        midiEventReceived(aftertouch);
    }

    @Override
    public void onMessage(LXEngine engine, String message) {
        if (this.armRecord.isOn()) {
            this.events.add(new MessageAutomationEvent(message));
        }
    }
}