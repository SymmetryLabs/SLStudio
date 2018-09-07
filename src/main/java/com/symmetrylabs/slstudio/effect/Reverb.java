package com.symmetrylabs.slstudio.effect;

import java.util.SortedSet;
import java.util.TreeSet;

import heronarts.lx.LX;
import heronarts.lx.LXEffect;
import heronarts.lx.PolyBuffer;
import heronarts.lx.PolyBuffer.Space;
import heronarts.lx.blend.AddBlend;
import heronarts.lx.blend.LXBlend;
import heronarts.lx.parameter.CompoundParameter;

/** Visual reverb.  Warning: very memory-heavy; don't overuse! */
public class Reverb extends LXEffect {
    public final CompoundParameter wetParam = new CompoundParameter("Wet", 0.5, 0, 0.8);
    public final CompoundParameter delayParam = new CompoundParameter("Delay", 0.5, 0, 1);
    protected double time = 0;
    protected SortedSet<Frame> frames = new TreeSet<>();
    protected LXBlend addBlend = new AddBlend(lx);

    public Reverb(LX lx) {
        super(lx);
        addParameter(wetParam);
        addParameter(delayParam);
    }

    @Override
    public void onDisable() {
        frames.clear();
    }

    public void run(double deltaMs, double amount, Space preferredSpace) {
        double deltaSec = deltaMs / 1000;
        time += deltaSec;
        double wet = wetParam.getValue() * amount;
        double delay = delayParam.getValue();

        PolyBuffer buffer = getPolyBuffer();
        Frame sourceFrame = findSourceFrame(time - delay);
        if (sourceFrame != null) {
            sourceFrame.blendOver(buffer, Space.RGB16, wet);
        }

        PolyBuffer saveBuffer;
        if (!frames.isEmpty() && frames.first().time < time - delay) {
            // If the oldest frame has expired, reuse it instead of allocating a new buffer.
            saveBuffer = frames.first().buffer;
        } else {
            saveBuffer = new PolyBuffer(lx);
        }
        Frame savedFrame = new Frame(time, saveBuffer);
        savedFrame.copyFrom(buffer, Space.RGB16);
        frames.add(savedFrame);

        removeFramesOlderThan(time - delay);
    }

    protected Frame findSourceFrame(double time) {
        SortedSet<Frame> tail = frames.tailSet(new Frame(time, null));
        return tail.isEmpty() ? null : tail.first();
    }

    protected void removeFramesOlderThan(double time) {
        // Free up any other unused frames and their buffers.
        while (!frames.isEmpty() && frames.first().time < time) {
            frames.remove(frames.first());
        }
    }

    protected class Frame implements Comparable<Frame> {
        public final double time;
        public final PolyBuffer buffer;

        public Frame(double time, PolyBuffer buffer) {
            this.time = time;
            this.buffer = buffer;
        }

        public int compareTo(Frame other) {
            if (time < other.time) return -1;
            if (time > other.time) return 1;
            return 0;
        }

        public void copyFrom(PolyBuffer src, Space space) {
            buffer.copyFrom(src, space);
        }

        public void blendOver(PolyBuffer dest, Space space, double amount) {
            addBlend.blend(dest, buffer, amount, dest, space);
        }
    }
}
