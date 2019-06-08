package com.symmetrylabs.slstudio.microlooks;

import com.google.gson.*;
import heronarts.lx.LX;
import heronarts.lx.LXChannel;
import heronarts.lx.LXComponent;
import heronarts.lx.parameter.DiscreteParameter;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;

public class MicroLooks extends LXComponent {
	public DiscreteParameter looks;

	private static JsonObject looksObject = null;

	private static String getFilePath() {
	    return System.getProperty("user.home") + "/Desktop/looks.json";
    }

	private static JsonObject getLooksObject() {
	    if (looksObject == null) {
	        try {
                InputStream fstream = new FileInputStream(getFilePath());

                JsonObject record = new Gson().fromJson(
                    new InputStreamReader(fstream), JsonObject.class);
                looksObject = record;
            } catch (FileNotFoundException e) {
	            System.out.println("No ~/Desktop/looks.json file found, microlooks disabled");
	            looksObject = new JsonObject();
	            looksObject.add("names", new JsonArray());
            }

        }
	    if (!looksObject.has("the_looks")) {
	        looksObject.add("the_looks", new JsonObject());
        }


        return looksObject;
    }

    private static void saveLooksObject(JsonObject obj) throws IOException {
        Writer writer = new FileWriter(getFilePath());
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        gson.toJson(obj, writer);
    }

	public static String[] getLookNames() {
		ArrayList<String> lookNames = new ArrayList();

        JsonObject record = getLooksObject();
        JsonArray names = record.getAsJsonArray("names");
		for (JsonElement e : names) {
		   String name = e.getAsString();
		   lookNames.add(name);
		}
		Collections.sort(lookNames);
		String[] res = new String[lookNames.size() + 1];
		res[0] = "SELECT LOOK";
		for (int i = 1; i < res.length; i++) {
			res[i] = lookNames.get(i - 1);
		}
		return res;
	}

	public static void saveChannel(LXChannel channel, String look) {
        JsonObject allLooks = getLooksObject();
        JsonObject realLooks = allLooks.getAsJsonObject("the_looks");
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
        };
        for (String toDel : toDelete) {
            if (params.has(toDel)) {
                params.remove(toDel);
            }
        }
        realLooks.add(look, obj);
        try {
            saveLooksObject(allLooks);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadChannel(LXChannel channel, String look) {
        JsonObject allLooks = MicroLooks.getLooksObject().getAsJsonObject("the_looks");
        if (!allLooks.has(look)) {
            return;
        }
        JsonObject obj = allLooks.getAsJsonObject(look);
        channel.load(channel.getLX(), obj);
    }
}
