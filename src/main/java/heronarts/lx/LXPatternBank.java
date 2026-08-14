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

import heronarts.lx.blend.LXBlend;
import heronarts.lx.model.LXModel;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.ObjectParameter;
import heronarts.lx.transform.LXVector;
import heronarts.lx.warp.LXWarp;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * A pattern bank is a collection of patterns within a channel that can run concurrently.
 * Each bank maintains its own active pattern, auto-cycle state, and bank-level effects.
 */
public class LXPatternBank extends LXModulatorComponent implements LXComponent.Renamable, LXUtils.IndexedElement {

    public interface Listener {
        void patternAdded(LXPatternBank bank, LXPattern pattern);
        void patternRemoved(LXPatternBank bank, LXPattern pattern);
        void patternMoved(LXPatternBank bank, LXPattern pattern);
        void patternWillChange(LXPatternBank bank, LXPattern pattern, LXPattern nextPattern);
        void patternDidChange(LXPatternBank bank, LXPattern pattern);
        void bankEffectAdded(LXPatternBank bank, LXEffect effect);
        void bankEffectRemoved(LXPatternBank bank, LXEffect effect);
        void bankEffectMoved(LXPatternBank bank, LXEffect effect);
    }

    public abstract static class AbstractListener implements Listener {
        @Override
        public void patternAdded(LXPatternBank bank, LXPattern pattern) {}
        @Override
        public void patternRemoved(LXPatternBank bank, LXPattern pattern) {}
        @Override
        public void patternMoved(LXPatternBank bank, LXPattern pattern) {}
        @Override
        public void patternWillChange(LXPatternBank bank, LXPattern pattern, LXPattern nextPattern) {}
        @Override
        public void patternDidChange(LXPatternBank bank, LXPattern pattern) {}
        @Override
        public void bankEffectAdded(LXPatternBank bank, LXEffect effect) {}
        @Override
        public void bankEffectRemoved(LXPatternBank bank, LXEffect effect) {}
        @Override
        public void bankEffectMoved(LXPatternBank bank, LXEffect effect) {}
    }

    private final List<Listener> listeners = new ArrayList<>();
    private final List<Listener> listenerSnapshot = new ArrayList<>();

    private final LXChannel channel;
    private int index;

    private final List<LXPattern> mutablePatterns = new ArrayList<>();
    public final List<LXPattern> patterns = Collections.unmodifiableList(mutablePatterns);

    private final List<LXEffect> mutableBankEffects = new ArrayList<>();
    public final List<LXEffect> bankEffects = Collections.unmodifiableList(mutableBankEffects);

    private final Map<LXPattern, List<LXEffect>> patternEffects = new IdentityHashMap<>();
    private final Map<LXPattern, List<LXWarp>> patternWarps = new IdentityHashMap<>();

    public final DiscreteParameter focusedPattern;

    public final BooleanParameter autoCycleEnabled =
            new BooleanParameter("Auto-Cycle", false)
                    .setDescription("When enabled, this bank will automatically cycle between its patterns");

    public final BoundedParameter autoCycleTimeSecs = (BoundedParameter)
            new BoundedParameter("Cycle Time", 60, .1, 10 * 60)
                    .setDescription("Sets the number of seconds after which the bank cycles to the next pattern")
                    .setUnits(LXParameter.Units.SECONDS);

    public final BoundedParameter transitionTimeSecs = (BoundedParameter)
            new BoundedParameter("Trans Time", 5, .1, 180)
                    .setDescription("Sets the duration of blending transitions between patterns")
                    .setUnits(LXParameter.Units.SECONDS);

    public final BooleanParameter transitionEnabled =
            new BooleanParameter("Transitions", false)
                    .setDescription("When enabled, transitions between patterns use a blend");

    public final ObjectParameter<LXBlend> transitionBlendMode;

    private final PolyBuffer polyBuffer;

    private double autoCycleProgress = 0;
    private double transitionProgress = 0;
    private int activePatternIndex = 0;
    private int nextPatternIndex = 0;

    private LXBlend transition = null;
    private long transitionMillis = 0;

    LXPatternBank(LX lx, LXChannel channel, int index) {
        super(lx, "Bank-" + (index + 1));
        this.channel = channel;
        this.index = index;
        this.polyBuffer = new PolyBuffer(lx);

        this.focusedPattern = new DiscreteParameter("Focused Pattern", 0, 1)
                .setDescription("Which pattern has focus in the UI");

        this.transitionBlendMode = new ObjectParameter<>("Trans Blend", lx.engine.crossfaderBlends)
                .setDescription("Specifies the blending function used for transitions between patterns in the bank");

        this.transitionMillis = lx.engine.nowMillis;

        addParameter("autoCycleEnabled", this.autoCycleEnabled);
        addParameter("autoCycleTimeSecs", this.autoCycleTimeSecs);
        addParameter("transitionEnabled", this.transitionEnabled);
        addParameter("transitionTimeSecs", this.transitionTimeSecs);
        addParameter("transitionBlendMode", this.transitionBlendMode);

        this.focusedPattern.addListener(p -> this.channel.onBankPatternFocused(this));
    }

    public LXChannel getChannel() {
        return this.channel;
    }

    public int getIndex() {
        return this.index;
    }

    public void setIndex(int index) {
        this.index = index;
        this.label.setValue("Bank-" + (index + 1));
    }

    public void addListener(Listener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        this.listeners.remove(listener);
    }

    public LXPatternBank addPattern(LXPattern pattern) {
        if (pattern.getBus() != this.channel) {
            pattern.setChannel(this.channel);
        }
        pattern.setModel(this.channel.getModel());
        this.mutablePatterns.add(pattern);
        LXUtils.updateIndexes(mutablePatterns);
        this.focusedPattern.setRange(Math.max(1, this.mutablePatterns.size()));

        this.listenerSnapshot.clear();
        this.listenerSnapshot.addAll(this.listeners);
        for (Listener listener : this.listenerSnapshot) {
            listener.patternAdded(this, pattern);
        }

        if (this.mutablePatterns.size() == 1) {
            this.focusedPattern.bang();
        }

        return this;
    }

    public LXPatternBank removePattern(LXPattern pattern) {
        int index = this.mutablePatterns.indexOf(pattern);
        if (index >= 0) {
            boolean wasActive = (this.activePatternIndex == index);
            int focusedPatternIndex = this.focusedPattern.getValuei();

            if ((this.transition != null) && (
                    (this.activePatternIndex == index) ||
                    (this.nextPatternIndex == index)
            )) {
                finishTransition();
            }

            this.mutablePatterns.remove(index);
            LXUtils.updateIndexes(mutablePatterns);

            if (this.activePatternIndex > index) {
                --this.activePatternIndex;
            } else if (this.activePatternIndex >= this.mutablePatterns.size()) {
                this.activePatternIndex = this.mutablePatterns.size() - 1;
            }
            if (this.nextPatternIndex > index) {
                --this.nextPatternIndex;
            } else if (this.nextPatternIndex >= this.mutablePatterns.size()) {
                this.nextPatternIndex = this.mutablePatterns.size() - 1;
            }
            if (focusedPatternIndex > index) {
                --focusedPatternIndex;
            } else if (focusedPatternIndex >= this.mutablePatterns.size()) {
                focusedPatternIndex = this.mutablePatterns.size() - 1;
            }

            if (this.activePatternIndex < 0) {
                this.activePatternIndex = 0;
                this.nextPatternIndex = 0;
            }

            if (focusedPatternIndex >= 0) {
                if (this.focusedPattern.getValuei() != focusedPatternIndex) {
                    this.focusedPattern.setValue(focusedPatternIndex);
                } else {
                    this.focusedPattern.bang();
                }
            }

            this.focusedPattern.setRange(Math.max(1, this.mutablePatterns.size()));

            this.listenerSnapshot.clear();
            this.listenerSnapshot.addAll(this.listeners);
            for (Listener listener : this.listenerSnapshot) {
                listener.patternRemoved(this, pattern);
            }

            if (wasActive && (this.mutablePatterns.size() > 0)) {
                LXPattern newActive = getActivePattern();
                if (newActive != null) {
                    newActive.onActive();
                }
                for (Listener listener : this.listeners) {
                    listener.patternDidChange(this, newActive);
                }
            }

            disposePatternScopedComponents(pattern);
            pattern.dispose();
        }
        return this;
    }

    public LXPatternBank movePattern(LXPattern pattern, int index) {
        LXPattern focusedPattern = getFocusedPattern();
        LXPattern activePattern = getActivePattern();
        LXPattern nextPattern = getNextPattern();
        this.mutablePatterns.remove(pattern);
        this.mutablePatterns.add(index, pattern);
        LXUtils.updateIndexes(mutablePatterns);
        this.activePatternIndex = activePattern.getIndex();
        this.nextPatternIndex = nextPattern.getIndex();
        for (Listener listener : this.listeners) {
            listener.patternMoved(this, pattern);
        }
        if (pattern == focusedPattern) {
            this.focusedPattern.setValue(pattern.getIndex());
        }
        return this;
    }

    public List<LXEffect> getPatternEffects(LXPattern pattern) {
        return patternEffects.computeIfAbsent(pattern, k -> new ArrayList<>());
    }

    public List<LXWarp> getPatternWarps(LXPattern pattern) {
        return patternWarps.computeIfAbsent(pattern, k -> new ArrayList<>());
    }

    public LXPatternBank addPatternEffect(LXPattern pattern, LXEffect effect) {
        List<LXEffect> list = getPatternEffects(pattern);
        list.add(effect);
        effect.setBus(this.channel);
        effect.setPattern(pattern);
        LXUtils.updateIndexes(list);
        return this;
    }

    public LXPatternBank removePatternEffect(LXPattern pattern, LXEffect effect) {
        List<LXEffect> list = getPatternEffects(pattern);
        int idx = list.indexOf(effect);
        if (idx >= 0) {
            effect.setIndex(-1);
            effect.setPattern(null);
            list.remove(idx);
            LXUtils.updateIndexes(list);
            effect.dispose();
        }
        return this;
    }

    public LXPatternBank addPatternWarp(LXPattern pattern, LXWarp warp) {
        List<LXWarp> list = getPatternWarps(pattern);
        list.add(warp);
        warp.setBus(this.channel);
        warp.setPattern(pattern);
        LXUtils.updateIndexes(list);
        return this;
    }

    public LXPatternBank removePatternWarp(LXPattern pattern, LXWarp warp) {
        List<LXWarp> list = getPatternWarps(pattern);
        int idx = list.indexOf(warp);
        if (idx >= 0) {
            warp.setIndex(-1);
            warp.setPattern(null);
            list.remove(idx);
            LXUtils.updateIndexes(list);
            warp.dispose();
        }
        return this;
    }

    public LXPatternBank addBankEffect(LXEffect effect) {
        this.mutableBankEffects.add(effect);
        effect.setBus(this.channel);
        LXUtils.updateIndexes(this.mutableBankEffects);
        for (Listener listener : this.listeners) {
            listener.bankEffectAdded(this, effect);
        }
        return this;
    }

    public LXPatternBank removeBankEffect(LXEffect effect) {
        int index = this.mutableBankEffects.indexOf(effect);
        if (index >= 0) {
            this.mutableBankEffects.remove(index);
            LXUtils.updateIndexes(this.mutableBankEffects);
            for (Listener listener : this.listeners) {
                listener.bankEffectRemoved(this, effect);
            }
            effect.dispose();
        }
        return this;
    }

    public LXPatternBank moveBankEffect(LXEffect effect, int index) {
        this.mutableBankEffects.remove(effect);
        this.mutableBankEffects.add(index, effect);
        LXUtils.updateIndexes(this.mutableBankEffects);
        for (Listener listener : this.listeners) {
            listener.bankEffectMoved(this, effect);
        }
        return this;
    }

    private void disposePatternScopedComponents(LXPattern pattern) {
        List<LXEffect> es = patternEffects.remove(pattern);
        if (es != null) {
            for (LXEffect e : es) e.dispose();
        }
        List<LXWarp> ws = patternWarps.remove(pattern);
        if (ws != null) {
            for (LXWarp w : ws) w.dispose();
        }
    }

    public LXPattern getFocusedPattern() {
        if (mutablePatterns.isEmpty()) {
            return null;
        }
        return this.mutablePatterns.get(this.focusedPattern.getValuei());
    }

    public LXPattern getActivePattern() {
        if (mutablePatterns.isEmpty()) {
            return null;
        }
        return this.mutablePatterns.get(this.activePatternIndex);
    }

    public LXPattern getNextPattern() {
        if (mutablePatterns.isEmpty()) {
            return null;
        }
        return this.mutablePatterns.get(this.nextPatternIndex);
    }

    public void goPrev() {
        if (this.transition != null || mutablePatterns.isEmpty()) {
            return;
        }
        this.nextPatternIndex = this.activePatternIndex - 1;
        if (this.nextPatternIndex < 0) {
            this.nextPatternIndex = this.mutablePatterns.size() - 1;
        }
        startTransition();
    }

    public void goNext() {
        if (this.transition != null || mutablePatterns.isEmpty()) {
            return;
        }
        this.nextPatternIndex = this.activePatternIndex;
        do {
            this.nextPatternIndex = (this.nextPatternIndex + 1) % this.mutablePatterns.size();
        } while ((this.nextPatternIndex != this.activePatternIndex)
                && !getNextPattern().isAutoCycleEligible());
        if (this.nextPatternIndex != this.activePatternIndex) {
            startTransition();
        }
    }

    public void goPattern(LXPattern pattern) {
        int pi = 0;
        for (LXPattern p : this.mutablePatterns) {
            if (p == pattern) {
                goIndex(pi);
                return;
            }
            ++pi;
        }
    }

    public void goIndex(int i) {
        if (i < 0 || i >= this.mutablePatterns.size()) {
            return;
        }
        if (this.transition != null) {
            finishTransition();
        }
        this.nextPatternIndex = i;
        startTransition();
    }

    private void startTransition() {
        LXPattern activePattern = getActivePattern();
        LXPattern nextPattern = getNextPattern();
        if (activePattern == nextPattern || activePattern == null || nextPattern == null) {
            return;
        }
        nextPattern.onActive();
        for (Listener listener : this.listeners) {
            listener.patternWillChange(this, activePattern, nextPattern);
        }
        if (this.transitionEnabled.isOn()) {
            this.transition = this.channel.lx.engine.crossfaderBlends[this.transitionBlendMode.getValuei()];
            nextPattern.onTransitionStart();
            this.transitionMillis = this.channel.lx.engine.nowMillis;
        } else {
            finishTransition();
        }
    }

    private void finishTransition() {
        getActivePattern().onInactive();
        this.activePatternIndex = this.nextPatternIndex;
        LXPattern activePattern = getActivePattern();
        if (this.transition != null) {
            activePattern.onTransitionEnd();
        }
        this.transition = null;
        this.transitionMillis = this.channel.lx.engine.nowMillis;
        for (Listener listener : listeners) {
            listener.patternDidChange(this, activePattern);
        }
    }

    public double getAutoCycleProgress() {
        return this.autoCycleProgress;
    }

    public double getTransitionProgress() {
        return this.transitionProgress;
    }

    public PolyBuffer getPolyBuffer() {
        return this.polyBuffer;
    }

    private void runPatternScoped(LXPattern pat, double deltaMs, PolyBuffer.Space space,
                                  LXVector[] baseVectors, LXWarp baseVectorSource) {
        List<LXWarp> pWarps = patternWarps.get(pat);
        LXVector[] curVecs = baseVectors;
        LXWarp curSource = baseVectorSource;
        boolean changed = false;
        if (pWarps != null) {
            for (LXWarp w : pWarps) {
                if (w.isEnabled()) {
                    w.setInputVectors(curSource, curVecs, changed);
                    changed = w.applyWarp(deltaMs);
                    curSource = w;
                    curVecs = w.getOutputVectors();
                }
            }
        }
        if (curVecs != channel.vectorArray || curSource != channel.vectorSource || changed) {
            channel.setVectorArray(curVecs, curSource);
        }

        List<LXEffect> pEffects = patternEffects.get(pat);
        double patDeltaMs = deltaMs;
        if (pEffects != null) {
            for (LXEffect e : pEffects) {
                if (e instanceof com.symmetrylabs.slstudio.effect.SpeedEffect && e.isEnabled()) {
                    patDeltaMs *= ((com.symmetrylabs.slstudio.effect.SpeedEffect) e).speed.getValue();
                    break;
                }
            }
        }

        pat.setPreferredSpace(space);
        pat.loop(patDeltaMs);

        if (pEffects != null) {
            for (LXEffect e : pEffects) {
                e.setPolyBuffer(pat.getPolyBuffer());
                e.loop(patDeltaMs);
            }
        }
    }

    @Override
    public void loop(double deltaMs) {
        super.loop(deltaMs);

        if (this.mutablePatterns.isEmpty()) {
            this.polyBuffer.setZero();
            return;
        }

        if (this.transition != null) {
            double transitionMs = this.channel.lx.engine.nowMillis - this.transitionMillis;
            double transitionDone = 1000 * this.transitionTimeSecs.getValue();
            if (transitionMs >= transitionDone) {
                finishTransition();
            }
        }

        if (this.transition == null) {
            this.autoCycleProgress = (this.channel.lx.engine.nowMillis - this.transitionMillis) / (1000 * this.autoCycleTimeSecs.getValue());
            if (this.autoCycleProgress >= 1) {
                this.autoCycleProgress = 1;
                if (this.autoCycleEnabled.isOn()) {
                    goNext();
                }
            }
        }

        PolyBuffer.Space space = this.channel.colorSpace.getEnum();
        LXVector[] baseVectors = this.channel.vectorArray;
        LXWarp baseVectorSource = this.channel.vectorSource;

        LXPattern active = getActivePattern();
        if (active != null) {
            runPatternScoped(active, deltaMs, space, baseVectors, baseVectorSource);
        }

        if (this.transition != null) {
            this.autoCycleProgress = 1;
            this.transitionProgress = (this.channel.lx.engine.nowMillis - this.transitionMillis) / (1000 * this.transitionTimeSecs.getValue());
            LXPattern next = getNextPattern();
            runPatternScoped(next, deltaMs, space, baseVectors, baseVectorSource);
            this.transition.loop(deltaMs);

            if (transitionProgress < 0.5) {
                transition.blend(getActivePattern(), getNextPattern(),
                        transitionProgress * 2, polyBuffer, space);
            } else {
                transition.blend(getNextPattern(), getActivePattern(),
                        (1 - transitionProgress) * 2, polyBuffer, space);
            }
        } else if (active != null) {
            this.transitionProgress = 0;
            polyBuffer.copyFrom(active, space);
        } else {
            polyBuffer.setZero();
        }

        for (LXEffect effect : this.mutableBankEffects) {
            if (effect.isEnabled()) {
                effect.setPolyBuffer(polyBuffer);
                effect.loop(deltaMs);
            }
        }
    }

    @Override
    public void dispose() {
        for (LXPattern pattern : this.mutablePatterns) {
            disposePatternScopedComponents(pattern);
            pattern.dispose();
        }
        this.mutablePatterns.clear();
        for (LXEffect effect : this.mutableBankEffects) {
            effect.dispose();
        }
        this.mutableBankEffects.clear();
        patternEffects.clear();
        patternWarps.clear();
        super.dispose();
    }

    private static final String KEY_PATTERNS = "patterns";
    private static final String KEY_PATTERN_INDEX = "patternIndex";
    private static final String KEY_PATTERN_EFFECTS = "patternEffects";
    private static final String KEY_PATTERN_WARPS = "patternWarps";
    private static final String KEY_BANK_EFFECTS = "bankEffects";

    public void save(LX lx, JsonObject obj) {
        super.save(lx, obj);
        obj.addProperty(KEY_PATTERN_INDEX, this.activePatternIndex);

        JsonArray patternsArr = new JsonArray();
        for (LXPattern p : this.mutablePatterns) {
            JsonObject pObj = new JsonObject();
            p.save(lx, pObj);
            List<LXEffect> es = patternEffects.get(p);
            if (es != null && !es.isEmpty()) {
                pObj.add(KEY_PATTERN_EFFECTS, LXSerializable.Utils.toArray(lx, es));
            }
            List<LXWarp> ws = patternWarps.get(p);
            if (ws != null && !ws.isEmpty()) {
                pObj.add(KEY_PATTERN_WARPS, LXSerializable.Utils.toArray(lx, ws));
            }
            patternsArr.add(pObj);
        }
        obj.add(KEY_PATTERNS, patternsArr);

        if (!this.mutableBankEffects.isEmpty()) {
            obj.add(KEY_BANK_EFFECTS, LXSerializable.Utils.toArray(lx, this.mutableBankEffects));
        }
    }

    public void load(LX lx, JsonObject obj) {
        for (int i = this.mutablePatterns.size() - 1; i >= 0; --i) {
            removePattern(this.mutablePatterns.get(i));
        }

        JsonArray patternsArray = obj.getAsJsonArray(KEY_PATTERNS);
        for (JsonElement patternElement : patternsArray) {
            JsonObject patternObj = (JsonObject) patternElement;
            LXPattern pattern = lx.instantiatePattern(patternObj.get("class").getAsString());
            if (pattern != null) {
                pattern.load(lx, patternObj);
                addPattern(pattern);

                if (patternObj.has(KEY_PATTERN_EFFECTS)) {
                    for (JsonElement el : patternObj.getAsJsonArray(KEY_PATTERN_EFFECTS)) {
                        JsonObject eObj = (JsonObject) el;
                        LXEffect e = lx.instantiateEffect(eObj.get("class").getAsString());
                        if (e != null) {
                            e.load(lx, eObj);
                            addPatternEffect(pattern, e);
                        }
                    }
                }
                if (patternObj.has(KEY_PATTERN_WARPS)) {
                    for (JsonElement el : patternObj.getAsJsonArray(KEY_PATTERN_WARPS)) {
                        JsonObject wObj = (JsonObject) el;
                        LXWarp w = lx.instantiateWarp(wObj.get("class").getAsString());
                        if (w != null) {
                            w.load(lx, wObj);
                            addPatternWarp(pattern, w);
                        }
                    }
                }
            }
        }

        if (obj.has(KEY_BANK_EFFECTS)) {
            for (JsonElement el : obj.getAsJsonArray(KEY_BANK_EFFECTS)) {
                JsonObject eObj = (JsonObject) el;
                LXEffect e = lx.instantiateEffect(eObj.get("class").getAsString());
                if (e != null) {
                    e.load(lx, eObj);
                    addBankEffect(e);
                }
            }
        }

        this.activePatternIndex = this.nextPatternIndex = 0;
        if (obj.has(KEY_PATTERN_INDEX)) {
            int patternIndex = obj.get(KEY_PATTERN_INDEX).getAsInt();
            if (patternIndex < this.patterns.size()) {
                this.activePatternIndex = this.nextPatternIndex = patternIndex;
            }
        }
        LXPattern activePattern = getActivePattern();
        if (activePattern != null) {
            activePattern.onActive();
            for (Listener listener : listeners) {
                listener.patternDidChange(this, activePattern);
            }
        }

        this.focusedPattern.setValue(this.activePatternIndex);

        super.load(lx, obj);
    }
}
