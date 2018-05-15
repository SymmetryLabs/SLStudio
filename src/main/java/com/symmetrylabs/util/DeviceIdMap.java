package com.symmetrylabs.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.symmetrylabs.slstudio.SLStudio;

import java.util.HashMap;
import java.util.Map;

public class DeviceIdMap {
    protected final Map<String, String> physicalIds = new HashMap<>();
    protected final String filename;

    public DeviceIdMap(String filename) {
        this.filename = filename;
        byte[] bytes = SLStudio.applet.loadBytes(filename);
        if (bytes != null) {
            try {
                JsonObject json = new Gson().fromJson(new String(bytes), JsonObject.class);
                for (String physId : json.keySet()) {
                    String mac = json.get(physId).getAsString().replaceAll(":", "");
                    physicalIds.put(mac, physId);
                }
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    public String getPhysicalId(String deviceId) {
        if (!physicalIds.containsKey(deviceId)) {
            System.err.println("WARNING: Device ID " + deviceId + " not in " + filename);
        }
        return physicalIds.getOrDefault(deviceId, deviceId);
    }
}
