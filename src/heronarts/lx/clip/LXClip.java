/**
 * Copyright 2017- Mark C. Slee, Heron Arts LLC
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
 * ##library.name##
 * ##library.sentence##
 * ##library.url##
 *
 * @author      ##author##
 * @modified    ##date##
 * @version     ##library.prettyVersion## (##library.version##)
 */

package heronarts.lx.clip;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import heronarts.lx.LX;
import heronarts.lx.LXBus;
import heronarts.lx.LXChannel;
import heronarts.lx.LXComponent;
import heronarts.lx.LXEffect;
import heronarts.lx.LXPattern;
import heronarts.lx.LXRunnableComponent;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.LXListenableNormalizedParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.parameter.MutableParameter;

public class LXClip extends LXRunnableComponent implements LXChannel.Listener {

    public interface TargetListener {
        public void targetAdded(LXClip clip, LXParameter parameter);
        public void targetRemoved(LXClip clip, LXParameter parameter);
    }

    private final List<TargetListener> targetListeners = new ArrayList<TargetListener>();

    double cursor = 0;

    public final MutableParameter length = (MutableParameter)
        new MutableParameter("Length", 0)
        .setDescription("The length of the clip")
        .setUnits(LXParameter.Units.MILLISECONDS);

    public final BooleanParameter loop = new BooleanParameter("Loop")
    .setDescription("Whether to loop the clip");

    private final List<LXParameter> internalTargets = new ArrayList<LXParameter>();
    public final List<LXParameter> targets = Collections.unmodifiableList(this.internalTargets);

    public final LXChannel channel;

    private int index;

    private final List<LXClipEvent> internalEvents = new ArrayList<LXClipEvent>();
    public final List<LXClipEvent> events = Collections.unmodifiableList(this.internalEvents);

    private final LXParameterListener parameterRecorder = new LXParameterListener() {
        public void onParameterChanged(LXParameter p) {
            if (isRunning() && channel.arm.isOn()) {
                LXListenableNormalizedParameter parameter = (LXListenableNormalizedParameter) p;
                addEvent(new ParameterClipEvent(LXClip.this, parameter));
                addTarget(parameter);
            }
        }
    };

    public LXClip(LX lx, LXChannel channel, int index) {
        super(lx);
        this.label.setDescription("The name of this clip");
        this.channel = channel;
        this.index = index;
        setParent(this.channel);
        addParameter("length", this.length);
        addParameter("loop", this.loop);

        this.channel.addListener(this);

        registerComponent(channel);
        for (LXPattern pattern : channel.patterns) {
            registerComponent(pattern);
        }
        for (LXEffect effect : channel.effects) {
            registerComponent(effect);
        }
    }

    private void addTarget(LXParameter parameter) {
        if (!this.internalTargets.contains(parameter)) {
            this.internalTargets.add(parameter);
            for (TargetListener listener : this.targetListeners) {
                listener.targetAdded(this, parameter);
            }
        }
    }

    public LXClip addTargetListener(TargetListener listener) {
        if (this.targetListeners.contains(listener)) {
            throw new IllegalStateException("Already registered target listener: " + listener);
        }
        this.targetListeners.add(listener);
        return this;
    }

    public LXClip removeTargetListener(TargetListener listener) {
        this.targetListeners.remove(listener);
        return this;
    }

    public double getCursor() {
        return this.cursor;
    }

    public double getBasis() {
        double lengthValue = this.length.getValue();
        if (lengthValue > 0) {
            return this.cursor / lengthValue;
        }
        return 0;
    }

    @Override
    public void onParameterChanged(LXParameter p) {
        super.onParameterChanged(p);
        if (p == this.trigger) {
            this.cursor = 0;
        } else if (p == this.running) {
            if (this.running.isOn()) {
                if (this.channel.arm.isOn()) {
                    this.cursor = 0;
                    this.length.setValue(0);
                    addEvent(new PatternClipEvent(this, this.channel.getActivePattern()));
                    this.internalEvents.clear();
                }
            } else {
                // Finished recording
                if (this.channel.arm.isOn()) {
                    this.length.setValue(this.cursor);
                }
            }
        }
    }

    private void addEvent(LXClipEvent event) {
        this.internalEvents.add(event);
    }

    private void registerComponent(LXComponent component) {
        for (LXParameter p : component.getParameters()) {
            if (p instanceof LXListenableNormalizedParameter) {
                ((LXListenableNormalizedParameter) p).addListener(this.parameterRecorder);
            }
        }
    }

    private void unregisterComponent(LXComponent component) {
        for (LXParameter p : channel.getParameters()) {
            if (p instanceof LXListenableNormalizedParameter) {
                ((LXListenableNormalizedParameter) p).removeListener(this.parameterRecorder);
            }
        }
        Iterator<LXClipEvent> iter = this.internalEvents.iterator();
        while (iter.hasNext()) {
            LXClipEvent event = iter.next();
            if (event.component == component) {
                iter.remove();
            }
        }
    }

    public int getIndex() {
        return this.index;
    }

    public LXClip setIndex(int index) {
        this.index = index;
        return this;
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    private void executeEvents(double from, double to) {
        for (LXClipEvent event : this.internalEvents) {
            if (from <= event.cursor && to > event.cursor) {
                event.execute();
            }
        }
    }

    @Override
    protected void run(double deltaMs) {
        double nextCursor = this.cursor + deltaMs;
        double lengthValue = this.length.getValue();
        if (!this.channel.arm.isOn()) {
            // TODO(mcslee): make this more efficient, keep track of index
            executeEvents(this.cursor, nextCursor);
            while (nextCursor > lengthValue) {
                if (!this.loop.isOn()) {
                    this.cursor = nextCursor = lengthValue;
                    stop();
                    break;
                } else {
                    nextCursor -= lengthValue;
                    executeEvents(0, nextCursor);
                }
            }
        } else {
            this.length.setValue(nextCursor);
        }
        this.cursor = nextCursor;
    }

    @Override
    public void effectAdded(LXBus channel, LXEffect effect) {
        registerComponent(effect);
    }

    @Override
    public void effectRemoved(LXBus channel, LXEffect effect) {
        unregisterComponent(effect);
    }

    @Override
    public void effectMoved(LXBus channel, LXEffect effect) {}

    @Override
    public void indexChanged(LXChannel channel) {}

    @Override
    public void patternAdded(LXChannel channel, LXPattern pattern) {
        registerComponent(pattern);
    }

    @Override
    public void patternRemoved(LXChannel channel, LXPattern pattern) {
        unregisterComponent(pattern);
    }

    @Override
    public void patternMoved(LXChannel channel, LXPattern pattern) {
    }

    @Override
    public void patternWillChange(LXChannel channel, LXPattern pattern, LXPattern nextPattern) {
        if (isRunning() && this.channel.arm.isOn()) {
            addEvent(new PatternClipEvent(this, nextPattern));
        }
    }

    @Override
    public void patternDidChange(LXChannel channel, LXPattern pattern) {

    }
}
