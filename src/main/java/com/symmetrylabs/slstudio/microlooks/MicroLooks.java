package com.symmetrylabs.slstudio.microlooks;

import com.google.gson.JsonObject;
import heronarts.lx.LX;
import heronarts.lx.LXComponent;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.StringParameter;
import heronarts.lx.parameter.EnumParameter;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.DiscreteParameter;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.symmetrylabs.slstudio.ApplicationState;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Collections; 

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import org.apache.commons.collections4.iterators.IteratorChain;
import java.net.URL;

public class MicroLooks extends LXComponent {
	public DiscreteParameter looks;

	private static String NAME_FILENAME = "looks.json";

	public String[] getLookNames() {
		ArrayList<String> lookNames = new ArrayList();

		ClassLoader cl = MicroLooks.class.getClassLoader();

		InputStream fstream = cl.getResourceAsStream(NAME_FILENAME);

		JsonObject record = new Gson().fromJson(
		    new InputStreamReader(fstream), JsonObject.class);

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

    public MicroLooks(LX lx) {
    	looks = new DiscreteParameter("looks", getLookNames());
    }
}
