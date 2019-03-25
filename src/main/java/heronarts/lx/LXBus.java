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
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.model.ModelMetrics;
import heronarts.lx.osc.LXOscComponent;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.transform.LXVector;
import heronarts.lx.warp.LXWarp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

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


    public final Timer timer = constructTimer();

    protected final LX lx;

    protected final List<LXWarp> mutableWarps = new ArrayList<>();
    public final List<LXWarp> warps = Collections.unmodifiableList(mutableWarps);

    protected final List<LXEffect> mutableEffects = new ArrayList<>();
    public final List<LXEffect> effects = Collections.unmodifiableList(mutableEffects);

    private final List<Listener> listeners = new ArrayList<>();

    /** The (possibly warped) coordinates of the model points, for use by patterns and effects */
    protected LXVector[] vectorArray = null;
    /** The LXWarp that last produced the contents of vectorArray, or null if the vectors came directly from the model. */
    protected LXWarp vectorSource = null;
    /** A cached list of all the non-null elements in vectorArray. */
    protected List<LXVector> vectorList = null;

    private ModelMetrics warpedMetrics = new ModelMetrics();

    LXBus(LX lx) {
        this(lx, null);
    }

    LXBus(LX lx, String label) {
        super(lx, label);
        this.lx = lx;
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

    public ModelMetrics getOriginalMetrics() {
        return model.getMetrics();
    }

    public ModelMetrics getWarpedMetrics() {
        return warpedMetrics;
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

    protected static LXVector[] getVectorArray(LXBus bus, LXModel model) {
        if (bus == null) {
            return model.getVectorArray();
        }
        if (bus.vectorArray == null) {
            bus.vectorArray = model.getVectorArray();
            bus.warpedMetrics.recompute(bus.vectorArray);
        }
        return bus.vectorArray;
    }

    protected void setVectorArray(LXVector[] newVectorArray, LXWarp newVectorSource) {
        vectorArray = newVectorArray;
        vectorSource = newVectorSource;
        vectorList = null;

        warpedMetrics.recompute(vectorArray);

        for (LXEffect effect : effects) {
            effect.onVectorsChanged();
        }
    }

    protected static List<LXVector> getVectorList(LXBus bus, LXModel model) {
        if (bus == null) {
            return Arrays.asList(model.getVectorArray());
        }
        if (bus.vectorList == null) {
            bus.vectorList = new ArrayList<LXVector>();
            for (LXVector v : getVectors(bus, model)) {
                bus.vectorList.add(v);
            }
        }
        return bus.vectorList;
    }

    /**
     * An iterator that increments an index starting from 0, calling get(index)
     * to retrieve the iterated element, skipping over any nulls returned by
     * get(index), and stopping when the index reaches the given stopIndex.
     */
    abstract static class VectorIterator implements Iterator<LXVector> {
        final int stopIndex;
        int index = -1;
        int nextIndex = -1;

        public VectorIterator(int stopIndex) {
            this.stopIndex = stopIndex;
        }

        public boolean hasNext() {
            if (nextIndex == index) {
                do {
                    nextIndex++;
                } while (nextIndex < stopIndex && get(nextIndex) == null);
            }
            return nextIndex < stopIndex;
        }

        public LXVector next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            LXVector result = get(nextIndex);
            index = nextIndex;
            return result;
        }

        // Will always be called with 0 <= index < stopIndex.
        abstract LXVector get(int index);
    }

    protected static Iterable<LXVector> getVectors(LXBus bus, LXModel model) {
        final LXVector[] vectors = getVectorArray(bus, model);
        return new Iterable<LXVector>() {
            public Iterator<LXVector> iterator () {
                return new VectorIterator(vectors.length) {
                    public LXVector get(int index) {
                        return vectors[index];
                    }
                };
            }
        };
    }

    protected static Iterable<LXVector> getVectors(LXBus bus, LXModel model, final List<LXPoint> points) {
        final LXVector[] vectors = getVectorArray(bus, model);
        return new Iterable<LXVector>() {
            public Iterator<LXVector> iterator() {
                return new VectorIterator(points.size()) {
                    public LXVector get(int index) {
                        return vectors[points.get(index).index];
                    }
                };
            }
        };
    }

    protected static Iterable<LXVector> getVectors(LXBus bus, LXModel model, LXPoint[] points) {
        final LXVector[] vectors = getVectorArray(bus, model);
        return new Iterable<LXVector>() {
            public Iterator<LXVector> iterator() {
                return new VectorIterator(points.length) {
                    public LXVector get(int index) {
                        return vectors[points[index].index];
                    }
                };
            }
        };
    }

    protected static Iterable<LXVector> getVectors(LXBus bus, LXModel model, final int start, final int stop) {
        final LXVector[] vectors = getVectorArray(bus, model);
        return new Iterable<LXVector>() {
            public Iterator<LXVector> iterator() {
                return new VectorIterator(stop - start) {
                    public LXVector get(int index) {
                        return vectors[start + index];
                    }
                };
            }
        };
    }

    @Override
    public void loop(double deltaMs) {
        long loopStart = System.nanoTime();

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
        for (LXWarp warp : this.mutableWarps) {
            warp.dispose();
        }
        this.mutableWarps.clear();
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
    }

    @Override
    public void load(LX lx, JsonObject obj) {
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

        super.load(lx, obj);
    }

}
