package heronarts.lx;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import heronarts.lx.osc.LXOscComponent;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.ObjectParameter;
import heronarts.lx.LXEngine.BlendTarget;
import com.symmetrylabs.color.Spaces;
import heronarts.lx.LXEngine.EngineBuffer;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.blend.LXBlend;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.blend.AddBlend;


public class LXLook extends LXModelComponent implements PolyBufferProvider {
    protected final LX lx;

    public interface Listener {
        public void channelAdded(LXLook look, LXChannel channel);
        public void channelRemoved(LXLook look, LXChannel channel);
        public void channelMoved(LXLook look, LXChannel channel);
    }

    protected final List<LXChannel> mutableChannels = new ArrayList<>();
    public final List<LXChannel> channels = Collections.unmodifiableList(mutableChannels);
    public final DiscreteParameter focusedChannel = new DiscreteParameter("Channel", 1);

    private final List<Listener> listeners = new ArrayList<Listener>();

    protected final PolyBuffer black;  // always black, read-only
    protected final BlendTarget groupA;  // working area for blending group A
    protected final BlendTarget groupB;  // working area for blending group B
    protected final CuedBuffer buffer;

    protected final AddBlend addBlend;

    public final ObjectParameter<LXBlend> crossfaderBlendMode;

    public final CompoundParameter crossfader = (CompoundParameter)
        new CompoundParameter("Crossfader", 0.5)
        .setDescription("Applies blending between output groups A and B")
        .setPolarity(LXParameter.Polarity.BIPOLAR);

    public final BooleanParameter cueA =
        new BooleanParameter("Cue-A", false)
        .setDescription("Enables cue preview of crossfade group A");

    public final BooleanParameter cueB =
        new BooleanParameter("Cue-B", false)
        .setDescription("Enables cue preview of crossfade group B");

    public LXLook(LX lx) {
        this(lx, null);
    }

    public LXLook(LX lx, String label) {
        super(lx, label);
        this.lx = lx;

        addParameter("focusedChannel", focusedChannel);

        addBlend = new AddBlend(lx);

        // An all-black buffer is used as the initial base for blending.
        // Its arrays are filled with zeroes on allocation and then never modified.
        black = new PolyBuffer(lx);

        // Blending buffers
        groupA = new BlendTarget(lx);
        groupB = new BlendTarget(lx);
        buffer = new CuedBuffer(lx);

        crossfaderBlendMode =
            new ObjectParameter<>("Crossfader Blend", lx.engine.crossfaderBlends)
            .setDescription("Sets the blend mode used for the master crossfader");

        cueA.addListener(p -> {
            if (cueA.isOn()) {
                cueB.setValue(false);
                lx.palette.cue.setValue(false);
                for (LXChannel channel : mutableChannels) {
                    channel.cueActive.setValue(false);
                }
            }
        });
        cueB.addListener(p -> {
            if (cueB.isOn()) {
                cueA.setValue(false);
                lx.palette.cue.setValue(false);
                for (LXChannel channel : mutableChannels) {
                    channel.cueActive.setValue(false);
                }
            }
        });
        lx.palette.cue.addListener(p -> {
            if (lx.palette.cue.isOn()) {
                cueA.setValue(false);
                cueB.setValue(false);
                for (LXChannel channel : mutableChannels) {
                    channel.cueActive.setValue(false);
                }
            }
        });
    }

    @Override
    public PolyBuffer getPolyBuffer() {
        return buffer.get();
    }

    public LXLook addListener(Listener listener) {
        this.listeners.add(listener);
        return this;
    }

    public LXLook removeListener(Listener listener) {
        this.listeners.remove(listener);
        return this;
    }

    public LXChannel addChannel() {
        LXChannel channel = new LXChannel(lx, this.mutableChannels.size(), new LXPattern[] {});
        channel.setParent(this);

        int oldSize = mutableChannels.size();
        mutableChannels.add(channel);
        focusedChannel.setRange(this.mutableChannels.size() + 1);
        if (focusedChannel.getValuei() == oldSize) {
            focusedChannel.bang();
        }
        for (Listener listener : this.listeners) {
            listener.channelAdded(this, channel);
        }
        return channel;
    }

    public void removeChannel(LXChannel channel) {
        removeChannel(channel, true);
    }

    private void removeChannel(LXChannel channel, boolean checkLast) {
        if (checkLast && (this.mutableChannels.size() == 1)) {
            throw new UnsupportedOperationException("Cannot remove last channel from LXEngine");
        }
        if (this.mutableChannels.remove(channel)) {
            int i = 0;
            for (LXChannel c : this.mutableChannels) {
                c.setIndex(i++);
            }
            boolean notified = false;
            if (this.focusedChannel.getValuei() > this.mutableChannels.size()) {
                notified = true;
                this.focusedChannel.decrement();
            }
            this.focusedChannel.setRange(this.mutableChannels.size() + 1);
            if (!notified) {
                this.focusedChannel.bang();
            }
            for (Listener listener : this.listeners) {
                listener.channelRemoved(this, channel);
            }
            channel.dispose();
        }
    }

    public void moveChannel(LXChannel channel, int index) {
        boolean focused = channel.getIndex() == this.focusedChannel.getValuei();
        this.mutableChannels.remove(channel);
        this.mutableChannels.add(index, channel);
        int i = 0;
        for (LXChannel c : this.mutableChannels) {
            c.setIndex(i++);
        }
        if (focused) {
            this.focusedChannel.setValue(index);
        }
        for (Listener listener : this.listeners) {
            listener.channelMoved(this, channel);
        }
    }

    public void duplicateChannel(LXChannel channel) {
        LXChannel chan = addChannel();
        chan.load(lx, LXSerializable.Utils.toObject(lx, channel));
        chan.label.setValue(chan.label.getString() + " copy");
    }

    public LXBus getFocusedChannel() {
        /* rework this to make master channel not focusable, currently needed for APC40Mk2 */
        if (focusedChannel.getValuei() == channels.size()) {
            return lx.engine.masterChannel;
        }
        return channels.get(focusedChannel.getValuei());
    }

    public LXLook setFocusedChannel(LXBus channel) {
        if (channel == lx.engine.masterChannel) {
            focusedChannel.setValue(channels.size());
        } else {
            int chanIndex = channels.indexOf(channel);
            Preconditions.checkArgument(chanIndex >= 0, "channel %s is not in look", channel.toString());
            focusedChannel.setValue(channels.indexOf(channel));
        }
        return this;
    }

    public void loop(double deltaMs, PolyBuffer.Space preferredSpace) {
        /* If we are in super-threaded mode, run the channels on their own
           threads! This needs significant rework once multiple looks can be run
           at once; this will be dispatching looks serially but channels in
           parallel; probably a better thing to do here is to submit all of
           these jobs to an executor shared across all looks, and then join on
           the executor for each frame. */
        if (lx.engine.isChannelMultithreaded.isOn()) {
            // Kick off threads per channel
            for (LXChannel channel : this.mutableChannels) {
                if (channel.shouldRun() || channel.cueActive.isOn()) {
                    synchronized (channel.thread) {
                        channel.thread.signal.workDone = false;
                        channel.thread.deltaMs = deltaMs;
                        channel.thread.workReady = true;
                        channel.thread.notify();
                        if (!channel.thread.hasStarted) {
                            channel.thread.hasStarted = true;
                            channel.thread.start();
                        }
                    }
                }
            }

            // Wait for all the channel threads to finish
            for (LXChannel channel : this.mutableChannels) {
                if (channel.shouldRun() || channel.cueActive.isOn()) {
                    synchronized (channel.thread.signal) {
                        while (!channel.thread.signal.workDone) {
                            try {
                                channel.thread.signal.wait();
                            } catch (InterruptedException ix) {
                                Thread.currentThread().interrupt();
                                break;
                            }
                        }
                        channel.thread.signal.workDone = false;
                    }
                }
            }
        } else {
            for (LXChannel channel : this.mutableChannels) {
                if (channel.shouldRun() || channel.cueActive.isOn()) {
                    channel.loop(deltaMs);
                }
            }
        }
        blendChannels(deltaMs, preferredSpace);
    }

    void blendChannels(double deltaMs, PolyBuffer.Space space) {
        groupA.reset();
        groupB.reset();
        BlendTarget main = new BlendTarget(buffer.main);
        BlendTarget cue = new BlendTarget(buffer.cue);
        buffer.cueOn = false;

        for (LXChannel channel : mutableChannels) {
            long blendStart = System.nanoTime();
            double alpha = Spaces.cie_lightness_to_luminance(channel.fader.getValue());

            if (channel.shouldRun() && alpha > 0) {
                LXChannel.CrossfadeGroup group = channel.crossfadeGroup.getEnum();
                LXBlend blend = channel.blendMode.getObject();
                (group == LXChannel.CrossfadeGroup.A ? groupA : group == LXChannel.CrossfadeGroup.B ? groupB : main)
                    .blendFrom(channel, alpha, blend, space);
            }
            if (channel.cueActive.isOn()) {
                cue.copyFrom(channel, space);
                buffer.cueOn = true;
            }
            if (channel.shouldRun() || channel.cueActive.isOn()) {
                ((LXChannel.Timer) channel.timer).blendNanos = System.nanoTime() - blendStart;
            }
        }

        if (cueA.isOn()) {
            cue.copyFrom(groupA, space);
            buffer.cueOn = true;
        } else if (cueB.isOn()) {
            cue.copyFrom(groupB, space);
            buffer.cueOn = true;
        }

        // Crossfade between the A and B groups, and add that to the main buffer
        double crossfadeValue = crossfader.getValue();
        boolean useGroupA = crossfadeValue < 1 && !groupA.isBlack();
        boolean useGroupB = crossfadeValue > 0 && !groupB.isBlack();
        double fadeTowardB = Math.min(1, 2 * crossfadeValue);
        double fadeTowardA = Math.min(1, 2 * (1 - crossfadeValue));

        if (useGroupA && useGroupB) {
            LXBlend blend = crossfaderBlendMode.getObject();
            if (crossfadeValue <= 0.5) {
                groupA.blendFrom(groupB, fadeTowardB, blend, space);
                main.blendFrom(groupA, 1, addBlend, space);
            } else {
                groupB.blendFrom(groupA, fadeTowardA, blend, space);
                main.blendFrom(groupB, 1, addBlend, space);
            }
        } else if (useGroupA) {
            main.blendFrom(groupA, fadeTowardA, addBlend, space);
        } else if (useGroupB) {
            main.blendFrom(groupB, fadeTowardB, addBlend, space);
        }

        // Ensure the main buffer is written even if nothing was blended up to this point
        main.finish(space);
    }

    class BlendTarget implements PolyBufferProvider {
        private PolyBuffer lastResult = black;
        private final PolyBuffer dest;

        public BlendTarget(LX lx) {
            this(new PolyBuffer(lx));
        }

        public BlendTarget(PolyBuffer dest) {
            this.dest = dest;
        }

        public void reset() {
            lastResult = black;
        }

        public PolyBuffer getPolyBuffer() {
            return lastResult;
        }

        public boolean isBlack() {
            return lastResult == black;
        }

        /** Copies a given buffer into the current destination buffer. */
        public void copyFrom(PolyBufferProvider src, PolyBuffer.Space space) {
            dest.copyFrom(src.getPolyBuffer(), space);
            lastResult = dest;
        }

        /** Blends an overlay buffer on top of the current destination buffer. */
        public void blendFrom(PolyBufferProvider overlay, double alpha, LXBlend blend, PolyBuffer.Space space) {
            blend.blend(lastResult, overlay, alpha, dest, space);
            lastResult = dest;
        }

        /** Ensures that the result is written to the destination buffer. */
        public void finish(PolyBuffer.Space space) {
            copyFrom(lastResult, space);
        }
    }

    class CuedBuffer {
        boolean cueOn;
        PolyBuffer main;
        PolyBuffer cue;

        CuedBuffer(LX lx) {
            main = new PolyBuffer(lx);
            cue = new PolyBuffer(lx);
        }

        PolyBuffer get() {
            return cueOn ? cue : main;
        }
    }

    private static final String KEY_CHANNELS = "channels";

    @Override
    public void load(LX lx, JsonObject obj) {
        // Remove all channels
        for (int i = mutableChannels.size() - 1; i >= 0; --i) {
            removeChannel(mutableChannels.get(i), false);
        }
        // Add the new channels
        if (obj.has(KEY_CHANNELS)) {
            JsonArray channelsArray = obj.getAsJsonArray(KEY_CHANNELS);
            for (JsonElement channelElement : channelsArray) {
                LXChannel channel = addChannel();
                channel.load(lx, (JsonObject) channelElement);
            }
        } else {
            addChannel().fader.setValue(1);
        }

        super.load(lx, obj);
    }

    @Override
    public void save(LX lx, JsonObject obj) {
        super.save(lx, obj);
        obj.add(KEY_CHANNELS, LXSerializable.Utils.toArray(lx, channels));
    }
}
