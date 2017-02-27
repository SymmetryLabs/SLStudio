/**
 * Copyright 2013- Mark C. Slee, Heron Arts LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author Mark C. Slee <mark@heronarts.com>
 */

package heronarts.lx;

import heronarts.lx.color.LXPalette;
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
public abstract class LXBus extends LXComponent {

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

    @Override
    protected void onPaletteChanged(LXPalette palette) {
        for (LXEffect effect : this.effects) {
            effect.setPalette(palette);
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
