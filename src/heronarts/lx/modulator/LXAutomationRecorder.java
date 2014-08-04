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

package heronarts.lx.modulator;

import heronarts.lx.LXLayerComponent;
import heronarts.lx.LXChannel;
import heronarts.lx.LXEngine;
import heronarts.lx.effect.LXEffect;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.LXListenableParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.pattern.LXPattern;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * An automation recorder contains meta-data about all the controls on the
 * patterns and effects in the system, which can be recorded or played back.
 */
public class LXAutomationRecorder extends LXModulator implements
        LXParameterListener {

    private final static String EVENT_PATTERN = "PATTERN";
    private final static String EVENT_PARAMETER = "PARAMETER";
    private final static String EVENT_FINISH = "FINISH";

    private final static String KEY_EVENT = "event";
    private final static String KEY_MILLIS = "millis";
    private final static String KEY_CHANNEL = "channel";
    private final static String KEY_PATTERN = "pattern";
    private final static String KEY_PARAMETER = "parameter";
    private final static String KEY_VALUE = "value";

    private final LXEngine engine;

    /**
     * Whether the recorder is armed. If true, then playback records new
     * events.
     */
    public final BooleanParameter armRecord = new BooleanParameter("ARM", false);

    /**
     * Whether the automation should loop upon completion.
     */
    public final BooleanParameter looping = new BooleanParameter("LOOP", false);

    private final List<LXChannel> channels = new ArrayList<LXChannel>();

    private final List<LXAutomationEvent> events = new ArrayList<LXAutomationEvent>();

    private final Map<String, LXParameter> pathToParameter = new HashMap<String, LXParameter>();

    private final Map<LXParameter, String> parameterToPath = new HashMap<LXParameter, String>();

    private int cursor = 0;

    private double elapsedMillis = 0;

    private abstract class LXAutomationEvent {

        private final String eventType;
        private double millis;

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
            jsonObj.addProperty(KEY_PATTERN, this.pattern.getClass().getName());
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

    private class FinishAutomationEvent extends LXAutomationEvent {

        private FinishAutomationEvent() {
            super(EVENT_FINISH);
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

    public LXAutomationRecorder(LXEngine engine) {
        super("AUTOMATION");
        this.engine = engine;
        for (LXChannel channel : engine.getChannels()) {
            registerChannel(channel);
        }
        for (LXEffect effect : engine.getEffects()) {
            registerComponent("effect/" + effect.getClass().getName(), effect);
        }
    }

    public LXAutomationRecorder registerChannel(LXChannel channel) {
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
        registerParameter(path + "/fader", channel.getFader());
        registerComponent(path + "/fader", channel.getFaderTransition());
        for (LXPattern pattern : channel.getPatterns()) {
            registerComponent(path + "/pattern/" + pattern.getClass().getName(), pattern);
        }
        return this;
    }

    public LXAutomationRecorder registerComponent(String prefix, LXLayerComponent component) {
        for (LXParameter parameter : component.getParameters()) {
            if (parameter instanceof LXListenableParameter) {
                registerParameter(prefix + "/" + parameter.getLabel(), (LXListenableParameter) parameter);
            }
        }
        return this;
    }

    public LXAutomationRecorder registerParameter(String path, LXListenableParameter parameter) {
        this.pathToParameter.put(path, parameter);
        this.parameterToPath.put(parameter, path);
        addParameter(parameter);
        return this;
    }

    @Override
    public void onStart() {
        this.elapsedMillis = 0;
        if (this.armRecord.isOn()) {
            this.events.clear();
            for (LXParameter parameter : getParameters()) {
                this.events.add(new ParameterAutomationEvent(parameter));
            }
            for (LXChannel channel : this.channels) {
                this.events.add(new PatternAutomationEvent(channel, channel.getActivePattern()));
            }
        }
    }

    @Override
    public void onStop() {
        if (this.armRecord.isOn()) {
            this.events.add(new FinishAutomationEvent());
            this.armRecord.setValue(false);
        }
    }

    @Override
    public void onReset() {
        this.cursor = 0;
    }

    @Override
    public double computeValue(double deltaMs) {
        this.elapsedMillis += deltaMs;
        if (!this.armRecord.isOn()) {
            while (isRunning() && (this.cursor < this.events.size())) {
                LXAutomationEvent event = this.events.get(this.cursor);
                if (this.elapsedMillis < event.millis) {
                    return 0;
                }
                ++this.cursor;
                event.play();
            }
        }
        return 0;
    }

    @Override
    public void onParameterChanged(LXParameter parameter) {
        if (this.armRecord.isOn()) {
            this.events.add(new ParameterAutomationEvent(parameter));
        }
    }

    public final JsonArray toJson() {
        JsonArray jsonArr = new JsonArray();
        for (LXAutomationEvent event : this.events) {
            jsonArr.add(event.toJson());
        }
        return jsonArr;
    }

    public final void loadJson(JsonArray jsonArr) {
        this.events.clear();
        for (JsonElement element : jsonArr) {
            JsonObject obj = element.getAsJsonObject();
            LXAutomationEvent event = null;
            String eventType = obj.get(KEY_EVENT).getAsString();
            if (eventType.equals(EVENT_PARAMETER)) {
                String parameterPath = obj.get(KEY_PARAMETER).getAsString();
                LXParameter parameter = pathToParameter.get(parameterPath);
                if (parameter != null) {
                    event = new ParameterAutomationEvent(parameter, obj.get(KEY_VALUE).getAsFloat());
                } else {
                    System.out.println("Unknown parameter: " + parameterPath);
                }
            } else if (eventType.equals(EVENT_PATTERN)) {
                int channelIndex = obj.get(KEY_CHANNEL).getAsInt();
                String patternClassName = obj.get(KEY_PATTERN).getAsString();
                LXChannel channel = this.engine.getChannel(channelIndex);
                LXPattern pattern = channel.getPattern(patternClassName);
                event = new PatternAutomationEvent(channel, pattern);
            } else if (eventType.equals(EVENT_FINISH)) {
                event = new FinishAutomationEvent();
            }
            if (event != null) {
                event.millis = obj.get(KEY_MILLIS).getAsFloat();
                this.events.add(event);
            }
        }
    }
}
