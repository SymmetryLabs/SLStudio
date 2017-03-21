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
public abstract class LXBus extends LXModelComponent {

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

    protected final List<LXEffect> effects = new ArrayList<LXEffect>();
    protected final List<LXEffect> unmodifiableEffects = Collections.unmodifiableList(effects);

    private final List<Listener> listeners = new ArrayList<Listener>();

    LXBus(LX lx) {
        super(lx);
        this.lx = lx;
    }

    @Override
    protected void onModelChanged(LXModel model) {
        for (LXEffect effect : this.effects) {
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
        this.effects.add(effect);
        effect.setBus(this);
        effect.setIndex(this.effects.size() - 1);
        for (Listener listener : this.listeners) {
            listener.effectAdded(this, effect);
        }
        return this;
    }

    public final LXBus removeEffect(LXEffect effect) {
        int index = this.effects.indexOf(effect);
        if (index >= 0) {
            effect.setIndex(-1);
            this.effects.remove(index);
            while (index < this.effects.size()) {
                this.effects.get(index).setIndex(index);
                ++index;
            }
            for (Listener listener : this.listeners) {
                listener.effectRemoved(this, effect);
            }
            effect.dispose();
        }
        return this;
    }

    public void moveEffect(LXEffect effect, int index) {
        this.effects.remove(effect);
        this.effects.add(index, effect);
        int i = 0;
        for (LXEffect e : this.effects) {
             e.setIndex(i++);
        }
        for (Listener listener : this.listeners) {
            listener.effectMoved(this, effect);
        }
    }

    public final List<LXEffect> getEffects() {
        return this.unmodifiableEffects;
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
        for (LXEffect effect : this.effects) {
            effect.dispose();
        }
        this.effects.clear();
        super.dispose();
    }

    private static final String KEY_EFFECTS = "effects";

    @Override
    public void save(JsonObject obj) {
        super.save(obj);;
        JsonArray effects = new JsonArray();
        for (LXEffect effect : this.effects) {
            JsonObject effectObj = new JsonObject();
            effect.save(effectObj);
            effects.add(effectObj);
        }
        obj.add(KEY_EFFECTS, effects);
    }

    @Override
    public void load(JsonObject obj) {
        super.load(obj);
        // Remove effects
        for (int i = this.effects.size() - 1; i >= 0; --i) {
            removeEffect(this.effects.get(i));
        }
        // Add the effects
        JsonArray effectsArray = obj.getAsJsonArray(KEY_EFFECTS);
        for (JsonElement effectElement : effectsArray) {
            JsonObject effectObj = (JsonObject) effectElement;
            LXEffect effect = this.lx.instantiateEffect(effectObj.get("class").getAsString());
            effect.load(effectObj);
            addEffect(effect);
        }
    }

}
