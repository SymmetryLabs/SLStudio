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

import heronarts.lx.model.LXModel;
import heronarts.lx.osc.LXOscComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

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

        public void effectAdded(LXBus channel, LXEffect effect);

        public void effectRemoved(LXBus channel, LXEffect effect);

        public void effectMoved(LXBus channel, LXEffect effect);

    }

    protected final LX lx;

    protected final List<LXEffect> internalEffects = new ArrayList<LXEffect>();

    public final List<LXEffect> effects = Collections.unmodifiableList(internalEffects);

    private final List<Listener> listeners = new ArrayList<Listener>();

    LXBus(LX lx) {
        this(lx, null);
    }

    LXBus(LX lx, String label) {
        super(lx, label);
        this.lx = lx;
    }

    @Override
    protected void onModelChanged(LXModel model) {
        for (LXEffect effect : this.internalEffects) {
            effect.setModel(model);
        }
    }

    public final void addListener(Listener listener) {
        this.listeners.add(listener);
    }

    public final void removeListener(Listener listener) {
        this.listeners.remove(listener);
    }

    public final LXBus addEffect(LXEffect effect) {
        this.internalEffects.add(effect);
        effect.setBus(this);
        effect.setIndex(this.internalEffects.size() - 1);
        for (Listener listener : this.listeners) {
            listener.effectAdded(this, effect);
        }
        return this;
    }

    public final LXBus removeEffect(LXEffect effect) {
        int index = this.internalEffects.indexOf(effect);
        if (index >= 0) {
            effect.setIndex(-1);
            this.internalEffects.remove(index);
            while (index < this.internalEffects.size()) {
                this.internalEffects.get(index).setIndex(index);
                ++index;
            }
            for (Listener listener : this.listeners) {
                listener.effectRemoved(this, effect);
            }
            effect.dispose();
        }
        return this;
    }

    public LXBus moveEffect(LXEffect effect, int index) {
        this.internalEffects.remove(effect);
        this.internalEffects.add(index, effect);
        int i = 0;
        for (LXEffect e : this.internalEffects) {
             e.setIndex(i++);
        }
        for (Listener listener : this.listeners) {
            listener.effectMoved(this, effect);
        }
        return this;
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

    @Override
    public void loop(double deltaMs) {
        long loopStart = System.nanoTime();

        // Run modulators and components
        super.loop(deltaMs);

        this.timer.loopNanos = System.nanoTime() - loopStart;
    }

    @Override
    public void dispose() {
        for (LXEffect effect : this.internalEffects) {
            effect.dispose();
        }
        this.internalEffects.clear();
        super.dispose();
    }

    private static final String KEY_EFFECTS = "effects";

    @Override
    public void save(LX lx, JsonObject obj) {
        super.save(lx, obj);;
        obj.add(KEY_EFFECTS, LXSerializable.Utils.toArray(lx, this.internalEffects));
    }

    @Override
    public void load(LX lx, JsonObject obj) {
        super.load(lx, obj);
        // Remove effects
        for (int i = this.internalEffects.size() - 1; i >= 0; --i) {
            removeEffect(this.internalEffects.get(i));
        }
        // Add the effects
        JsonArray effectsArray = obj.getAsJsonArray(KEY_EFFECTS);
        for (JsonElement effectElement : effectsArray) {
            JsonObject effectObj = (JsonObject) effectElement;
            LXEffect effect = this.lx.instantiateEffect(effectObj.get("class").getAsString());
            effect.load(lx, effectObj);
            addEffect(effect);
        }
    }

}
