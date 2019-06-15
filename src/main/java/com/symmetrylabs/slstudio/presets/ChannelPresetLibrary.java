package com.symmetrylabs.slstudio.presets;

import com.google.common.base.Preconditions;
import com.google.gson.*;
import heronarts.lx.LX;
import heronarts.lx.LXChannel;
import heronarts.lx.LXSerializable;

import java.util.*;

public class ChannelPresetLibrary implements LXSerializable {
    private static final String KEY_PRESETS = "presets";
    private final Map<String, JsonObject> presets;
    private final List<String> sortedPresetNames;
    private String[] optionCache;

    public ChannelPresetLibrary() {
        presets = new HashMap<>();
        sortedPresetNames = new ArrayList<>();
        updatePresetNames();
    }

    public synchronized void saveChannelAsPreset(LXChannel channel, String look) {
        JsonObject obj = new JsonObject();
        channel.save(channel.getLX(), obj);

        JsonObject params = obj.getAsJsonObject("parameters");
        String[] toDelete = new String[]{
            "enabled",
            "cue",
            "midiMonitor",
            "midiChannel",
            "autoCycleEnabled",
            "autoCycleTimeSecs",
            "fader",
            "crossfadeGroup",
            "transitionEnabled",
            "transitionTimeSecs",
            "transitionBlendMode",
            "autoDisable",
            "editorVisible",
            "label",
        };
        for (String toDel : toDelete) {
            if (params.has(toDel)) {
                params.remove(toDel);
            }
        }
        presets.put(look, obj);
        updatePresetNames();
    }

    public void saveChannelAsPreset(LXChannel channel) {
        saveChannelAsPreset(channel, sortedPresetNames.get(channel.linkedPreset));
    }

    public void applyPresetToChannel(LXChannel channel, String look) {
        Preconditions.checkArgument(presets.containsKey(look));
        channel.load(channel.getLX(), presets.get(look));
    }

    public void applyPresetToChannel(LXChannel channel) {
        applyPresetToChannel(channel, sortedPresetNames.get(channel.linkedPreset));
    }

    private synchronized void updatePresetNames() {
        sortedPresetNames.clear();
        sortedPresetNames.addAll(presets.keySet());
        sortedPresetNames.sort(String::compareToIgnoreCase);

        optionCache = new String[sortedPresetNames.size() + 1];
        sortedPresetNames.toArray(optionCache);
        optionCache[optionCache.length - 1] = "New...";
    }

    public synchronized int newPresetOptionIndex() {
        return optionCache.length - 1;
    }

    public synchronized String[] getOptions() {
        return optionCache;
    }

    @Override
    public void save(LX lx, JsonObject object) {
        JsonObject lookObject = new JsonObject();
        for (String lookId : presets.keySet()) {
            lookObject.add(lookId, presets.get(lookId));
        }
        object.add(KEY_PRESETS, lookObject);
    }

    @Override
    public void load(LX lx, JsonObject object) {
        presets.clear();
        if (object.has(KEY_PRESETS)) {
            JsonObject presetsObject = object.getAsJsonObject(KEY_PRESETS);
            for (String lookId : presetsObject.keySet()) {
                presets.put(lookId, presetsObject.getAsJsonObject(lookId));
            }
        }
        updatePresetNames();
    }

    public List<String> getNames() {
        return Collections.unmodifiableList(sortedPresetNames);
    }
}
