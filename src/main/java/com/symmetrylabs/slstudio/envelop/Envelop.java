package com.symmetrylabs.slstudio.envelop;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

import processing.core.PVector;
import com.google.gson.JsonObject;

import heronarts.lx.LX;
import heronarts.lx.LXRunnableComponent;
import heronarts.lx.LXSerializable;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.NormalizedParameter;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.osc.OscMessage;

import static com.symmetrylabs.util.MathUtils.constrain;

public class Envelop extends LXRunnableComponent {

    public final Source source = new Source();
    public final Decode decode = new Decode();

    private static Map<LX, WeakReference<Envelop>> instanceByLX = new WeakHashMap<>();

    public static synchronized Envelop getInstance(LX lx) {
        WeakReference<Envelop> weakRef = instanceByLX.get(lx);
        Envelop ref = weakRef == null ? null : weakRef.get();
        if (ref == null) {
            instanceByLX.put(lx, new WeakReference<>(ref = new Envelop(lx)));
        }
        return ref;
    }

    private Envelop(LX lx) {
        super(lx, "Envelop");
        addSubcomponent(source);
        addSubcomponent(decode);
        source.start();
        decode.start();
        start();
    }

    @Override
    public void run(double deltaMs) {
        source.loop(deltaMs);
        decode.loop(deltaMs);
    }

    private final static String KEY_SOURCE = "source";
    private final static String KEY_DECODE = "decode";

    @Override
    public void save(LX lx, JsonObject obj) {
        super.save(lx, obj);
        obj.add(KEY_SOURCE, LXSerializable.Utils.toObject(lx, this.source));
        obj.add(KEY_DECODE, LXSerializable.Utils.toObject(lx, this.decode));
    }

    @Override
    public void load(LX lx, JsonObject obj) {
        if (obj.has(KEY_SOURCE)) {
            this.source.load(lx, obj.getAsJsonObject(KEY_SOURCE));
        }
        if (obj.has(KEY_DECODE)) {
            this.decode.load(lx, obj.getAsJsonObject(KEY_DECODE));
        }
        super.load(lx, obj);
    }

    abstract class Meter extends LXRunnableComponent {

        private static final double TIMEOUT = 1000;

        private final double[] targets;
        private final double[] timeouts;

        public final BoundedParameter gain = (BoundedParameter)new BoundedParameter("Gain", 0, -24, 24)
                .setDescription("Sets the dB gain of the meter")
                .setUnits(LXParameter.Units.DECIBELS);

        public final BoundedParameter range = (BoundedParameter)new BoundedParameter("Range", 24, 6, 96)
                .setDescription("Sets the dB range of the meter")
                .setUnits(LXParameter.Units.DECIBELS);

        public final BoundedParameter attack = (BoundedParameter)new BoundedParameter("Attack", 25, 0, 50)
                .setDescription("Sets the attack time of the meter response")
                .setUnits(LXParameter.Units.MILLISECONDS);

        public final BoundedParameter release = (BoundedParameter)new BoundedParameter("Release", 50, 0, 500)
                .setDescription("Sets the release time of the meter response")
                .setUnits(LXParameter.Units.MILLISECONDS);

        protected Meter(String label, int numChannels) {
            super(label);
            this.targets = new double[numChannels];
            this.timeouts = new double[numChannels];
            addParameter(this.gain);
            addParameter(this.range);
            addParameter(this.attack);
            addParameter(this.release);
        }

        public void run(double deltaMs) {
            NormalizedParameter[] channels = getChannels();
            for (int i = 0; i < channels.length; ++i) {
                this.timeouts[i] += deltaMs;
                if (this.timeouts[i] > TIMEOUT) {
                    this.targets[i] = 0;
                }
                double target = this.targets[i];
                double value = channels[i].getValue();
                double gain = (target >= value) ? Math.exp(-deltaMs / attack.getValue()) : Math.exp(-deltaMs / release.getValue());
                channels[i].setValue(target + gain * (value - target));
            }
        }

        public void setLevel(int index, OscMessage message) {
            double gainValue = this.gain.getValue();
            double rangeValue = this.range.getValue();
            this.targets[index] = constrain((float) (1 + (message.getFloat() + gainValue) / rangeValue), 0, 1);
            this.timeouts[index] = 0;
        }

        public void setLevels(OscMessage message) {
            double gainValue = this.gain.getValue();
            double rangeValue = this.range.getValue();
            for (int i = 0; i < this.targets.length; ++i) {
                this.targets[i] = constrain((float) (1 + (message.getFloat() + gainValue) / rangeValue), 0, 1);
                this.timeouts[i] = 0;
            }
        }

        protected abstract NormalizedParameter[] getChannels();
    }

    class Source extends Meter {
        public static final int NUM_CHANNELS = 16;

        class Channel extends NormalizedParameter {

            public final int index;
            public boolean active;
            public final PVector xyz = new PVector();

            float tx;
            float ty;
            float tz;

            Channel(int i) {
                super("Source-" + (i+1));
                this.index = i+1;
                this.active = false;
            }
        }

        public final Channel[] channels = new Channel[NUM_CHANNELS];

        Source() {
            super("Source", NUM_CHANNELS);
            for (int i = 0; i < channels.length; ++i) {
                addParameter(channels[i] = new Channel(i));
            }
        }

        public NormalizedParameter[] getChannels() {
            return this.channels;
        }
    }

    class Decode extends Meter {

        public static final int NUM_CHANNELS = 8;
        public final NormalizedParameter[] channels = new NormalizedParameter[NUM_CHANNELS];

        Decode() {
            super("Decode", NUM_CHANNELS);
            for (int i = 0; i < channels.length; ++i) {
                addParameter(channels[i] = new NormalizedParameter("Decode-" + (i+1)));
            }
        }

        public NormalizedParameter[] getChannels() {
            return this.channels;
        }
    }
}
