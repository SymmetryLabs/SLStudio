package com.symmetrylabs.slstudio.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.symmetrylabs.slstudio.Environment;
import com.symmetrylabs.slstudio.mappings.*;
import com.symmetrylabs.util.RuntimeTypeAdapterFactory;
import com.symmetrylabs.util.Utils;
import processing.core.PApplet;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;

/* this class reads in the model JSON and builds up the Java Object representation

 */
public class JSON_Builder {

    private static final Gson gson;

    static {
        gson = new GsonBuilder()
            .setPrettyPrinting()
            .excludeFieldsWithoutExposeAnnotation()
//
//            .registerTypeAdapterFactory(
//                RuntimeTypeAdapterFactory.of(Strip.class)
//            )

            .create();
    }



    public static JSON_Builder loadMappingData(Environment environment) {
        try (FileReader reader = new FileReader(Utils.sketchFile(environment.getMappingsFilename()))) {
            return gson.fromJson(reader, JSON_Builder.class);
        } catch (FileNotFoundException e) {
            return null;
        } catch (java.io.IOException e) {
            PApplet.println("Failed to load mapping data");
            e.printStackTrace();
            return null;
        }
    }

    public void saveMappingData(Environment environment) {
        try (FileWriter writer = new FileWriter(Utils.sketchFile(environment.getMappingsFilename()))) {
            gson.toJson(this, writer);
        } catch (java.io.IOException e) {
            PApplet.println("Failed to save mapping data");
            e.printStackTrace();
        }
    }
}
