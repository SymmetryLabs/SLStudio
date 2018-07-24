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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import heronarts.lx.clip.LXClip;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.osc.LXOscComponent;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.transform.LXVector;
import heronarts.lx.warp.LXWarp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Abstract representation of a channel, which could be a normal channel with patterns
 * or the master channel.
 */
public abstract class LXBus extends LXModelComponent implements LXOscComponent {

    /**
     * Listener interface for objects which want to be notified when the internal
     * channel state is modified.
     */
    public interface Listener {
        public void warpAdded(LXBus channel, LXWarp warp);
        public void warpRemoved(LXBus channel, LXWarp warp);
        public void warpMoved(LXBus channel, LXWarp warp);
        public void effectAdded(LXBus channel, LXEffect effect);
        public void effectRemoved(LXBus channel, LXEffect effect);
        public void effectMoved(LXBus channel, LXEffect effect);
    }

    public interface ClipListener {
        public void clipAdded(LXBus bus, LXClip clip);
        public void clipRemoved(LXBus bus, LXClip clip);
    }


    public final Timer timer = constructTimer();

    /**
     * Arms the channel for clip recording.
     */
    public final BooleanParameter arm =
        new BooleanParameter("Arm")
        .setDescription("Arms the channel for clip recording");

    protected final LX lx;

    protected final List<LXWarp> mutableWarps = new ArrayList<>();
    public final List<LXWarp> warps = Collections.unmodifiableList(mutableWarps);

    protected final List<LXEffect> mutableEffects = new ArrayList<>();
    public final List<LXEffect> effects = Collections.unmodifiableList(mutableEffects);

    private final List<LXClip> mutableClips = new ArrayList<>();
    public final List<LXClip> clips = Collections.unmodifiableList(this.mutableClips);

    private final List<Listener> listeners = new ArrayList<>();
    private final List<ClipListener> clipListeners = new ArrayList<>();

    /** The (possibly warped) coordinates of the model points, for use by patterns and effects */
    protected List<LXVector> vectors = null;

    LXBus(LX lx) {
        this(lx, null);
    }

    LXBus(LX lx, String label) {
        super(lx, label);
        this.lx = lx;
        addParameter("arm", this.arm);
    }

    @Override
    protected void onModelChanged(LXModel model) {
        for (LXEffect effect : this.mutableEffects) {
            effect.setModel(model);
        }
    }

    public final void addListener(Listener listener) {
        this.listeners.add(listener);
    }

    public final void removeListener(Listener listener) {
        this.listeners.remove(listener);
    }

    public LXBus addClipListener(ClipListener listener) {
        this.clipListeners.add(listener);
        return this;
    }

    public LXBus removeClipListener(ClipListener listener) {
        this.clipListeners.remove(listener);
        return this;
    }

    public final void addWarp(LXWarp warp) {
        mutableWarps.add(warp);
        warp.setBus(this);
        LXUtils.updateIndexes(mutableWarps);
        for (Listener listener : listeners) {
            listener.warpAdded(this, warp);
        }
    }

    public final void removeWarp(LXWarp warp) {
        int index = mutableWarps.indexOf(warp);
        if (index >= 0) {
            warp.setIndex(-1);
            mutableWarps.remove(index);
            LXUtils.updateIndexes(mutableWarps);
            for (Listener listener : listeners) {
                listener.warpRemoved(this, warp);
            }
            warp.dispose();
        }
    }

    public final void moveWarp(LXWarp warp, int index) {
        mutableWarps.remove(warp);
        mutableWarps.add(index, warp);
        LXUtils.updateIndexes(mutableWarps);
        for (Listener listener : this.listeners) {
            listener.warpMoved(this, warp);
        }
    }

    public final List<LXWarp> getWarps() { return warps; }

    public LXWarp getWarp(int i) {
        return warps.get(i);
    }

    public LXWarp getWarp(String label) {
        for (LXWarp warp : warps) {
            if (warp.getLabel().equals(label)) {
                return warp;
            }
        }
        return null;
    }

    public final void addEffect(LXEffect effect) {
        this.mutableEffects.add(effect);
        effect.setBus(this);
        LXUtils.updateIndexes(mutableEffects);
        for (Listener listener : this.listeners) {
            listener.effectAdded(this, effect);
        }
    }

    public final void removeEffect(LXEffect effect) {
        int index = this.mutableEffects.indexOf(effect);
        if (index >= 0) {
            effect.setIndex(-1);
            this.mutableEffects.remove(index);
            LXUtils.updateIndexes(mutableEffects);
            for (Listener listener : this.listeners) {
                listener.effectRemoved(this, effect);
            }
            effect.dispose();
        }
    }

    public final void moveEffect(LXEffect effect, int index) {
        this.mutableEffects.remove(effect);
        this.mutableEffects.add(index, effect);
        LXUtils.updateIndexes(mutableEffects);
        for (Listener listener : this.listeners) {
            listener.effectMoved(this, effect);
        }
    }

    public final List<LXEffect> getEffects() {
        return this.effects;
    }

    public LXEffect getEffect(int i) {
        return this.effects.get(i);
    }

    public LXEffect getEffect(String label) {
        for (LXEffect effect : this.effects) {
            if (effect.getLabel().equals(label)) {
                return effect;
            }
        }
        return null;
    }

    public LXClip getClip(int index) {
        return getClip(index, false);
    }

    public LXClip getClip(int index, boolean create) {
        if (index < this.clips.size()) {
            return this.clips.get(index);
        }
        if (create) {
            return addClip(index);
        }
        return null;
    }

    public LXClip addClip() {
        return addClip(this.mutableClips.size());
    }

    public LXClip addClip(int index) {
        while (this.mutableClips.size() <= index) {
            this.mutableClips.add(null);
        }
        LXClip clip = constructClip(index);
        clip.label.setValue("Clip-" + (index+1));
        this.mutableClips.set(index, clip);
        for (ClipListener listener : this.clipListeners) {
            listener.clipAdded(this, clip);
        }
        return clip;
    }

    public LXBus stopClips() {
        for (LXClip clip : this.clips) {
            if (clip != null) {
                clip.stop();
            }
        }
        return this;
    }

    protected abstract LXClip constructClip(int index);

    public void removeClip(LXClip clip) {
        int index = this.mutableClips.indexOf(clip);
        if (index < 0) {
            throw new IllegalArgumentException("Clip is not owned by channel: " + clip + " " + this);
        }
        removeClip(index);
    }

    public void removeClip(int index) {
        LXClip clip = this.mutableClips.get(index);
        this.mutableClips.set(index, null);
        for (ClipListener listener : this.clipListeners) {
            listener.clipRemoved(this, clip);
        }
        clip.dispose();
    }

    protected static List<LXVector> getVectors(LXBus bus, LXModel model) {
        if (bus == null) {
            return model.getVectors();
        }
        if (bus.vectors == null) {
            bus.vectors = model.getVectors();
        }
        return bus.vectors;
    }

    protected static List<LXVector> getVectors(LXBus bus, LXModel model, Set<Integer> indexes) {
        List<LXVector> result = new ArrayList<>();
        for (LXVector v : getVectors(bus, model)) {
            if (indexes.contains(v.index)) {
                result.add(v);
            }
        }
        return result;
    }

    protected static List<LXVector> getVectors(LXBus bus, LXModel model, Iterable<LXPoint> points) {
        HashSet<Integer> indexes = new HashSet<>();
        for (LXPoint p : points) {
            indexes.add(p.index);
        }
        return getVectors(bus, model, indexes);
    }

    protected static List<LXVector> getVectors(LXBus bus, LXModel model, LXPoint[] points) {
        HashSet<Integer> indexes = new HashSet<>();
        for (LXPoint p : points) {
            indexes.add(p.index);
        }
        return getVectors(bus, model, indexes);
    }

    protected static List<LXVector> getVectors(LXBus bus, LXModel model, int start, int stop) {
        List<LXVector> result = new ArrayList<>();
        for (LXVector v : getVectors(bus, model)) {
            if (v.index >= start && v.index < stop) {
                result.add(v);
            }
        }
        return result;
    }

    protected void setVectors(List<LXVector> newVectors) {
        if (newVectors != vectors) {
            vectors = newVectors;
            for (LXEffect effect : effects) {
                effect.onVectorsChanged();
            }
        }
    }
    
    @Override
    public void loop(double deltaMs) {
        long loopStart = System.nanoTime();

        // Run the active clip...
        // TODO(mcslee): keep tabs of which is active?
        for (LXClip clip : this.clips) {
            if (clip != null) {
                clip.loop(deltaMs);
            }
        }

        // Run modulators and components
        super.loop(deltaMs);

        this.timer.loopNanos = System.nanoTime() - loopStart;
    }

    @Override
    public void dispose() {
        for (LXEffect effect : this.mutableEffects) {
            effect.dispose();
        }
        this.mutableEffects.clear();
        super.dispose();
    }

    private static final String KEY_WARPS = "warps";
    private static final String KEY_EFFECTS = "effects";
    private static final String KEY_CLIPS = "clips";

    @Override
    public void save(LX lx, JsonObject obj) {
        super.save(lx, obj);;
        obj.add(KEY_WARPS, LXSerializable.Utils.toArray(lx, warps));
        obj.add(KEY_EFFECTS, LXSerializable.Utils.toArray(lx, effects));
        JsonArray clipsArr = new JsonArray();
        for (LXClip clip : this.clips) {
            if (clip != null) {
                clipsArr.add(LXSerializable.Utils.toObject(lx, clip));
            }
        }
        obj.add(KEY_CLIPS, clipsArr);
    }

    @Override
    public void load(LX lx, JsonObject obj) {
        // Remove clips
        for (LXClip clip : this.clips) {
            if (clip != null) {
                removeClip(clip);
            }
        }
        // Clear warps
        while (warps.size() > 0) {
            removeWarp(warps.get(0));
        }
        // Add warps
        JsonArray warpsArray = obj.getAsJsonArray(KEY_WARPS);
        if (warpsArray != null) {
            for (JsonElement warpElement : warpsArray) {
                JsonObject warpObject = (JsonObject) warpElement;
                LXWarp warp = lx.instantiateWarp(warpObject.get("class").getAsString());
                warp.load(lx, warpObject);
                addWarp(warp);
            }
        }

        // Clear effects
        while (effects.size() > 0) {
            removeEffect(effects.get(0));
        }
        // Add the effects
        JsonArray effectsArray = obj.getAsJsonArray(KEY_EFFECTS);
        if (effectsArray != null) {
            for (JsonElement effectElement : effectsArray) {
                JsonObject effectObj = (JsonObject) effectElement;
                LXEffect effect = this.lx.instantiateEffect(effectObj.get("class").getAsString());
                effect.load(lx, effectObj);
                addEffect(effect);
            }
        }
        // Add the new clips
        if (obj.has(KEY_CLIPS)) {
            JsonArray clipsArr = obj.get(KEY_CLIPS).getAsJsonArray();
            for (JsonElement clipElem : clipsArr) {
                JsonObject clipObj = clipElem.getAsJsonObject();
                int clipIndex = clipObj.get(LXClip.KEY_INDEX).getAsInt();
                LXClip clip = addClip(clipIndex);
                clip.load(lx, clipObj);
            }
        }

        super.load(lx, obj);
    }

}
