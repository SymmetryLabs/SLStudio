package com.symmetrylabs.slstudio.mappings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.symmetrylabs.slstudio.Environment;
import com.symmetrylabs.util.Utils;
import com.symmetrylabs.util.ClassUtils;
import com.symmetrylabs.util.RuntimeTypeAdapterFactory;
import processing.core.PApplet;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class Mappings extends MappingGroup {

        private static final Gson gson;

        static {
                gson = new GsonBuilder()
                        .setPrettyPrinting()
                        .excludeFieldsWithoutExposeAnnotation()

                        .registerTypeAdapterFactory(
                                RuntimeTypeAdapterFactory.of(MappingItem.class)
                                        .registerSubtype(StripMapping.class)
                        )

                        .registerTypeAdapterFactory(
                                RuntimeTypeAdapterFactory.of(OutputMapping.class)
                                        .registerSubtype(PixliteMapping.class)
                        )

                        .registerTypeAdapterFactory(
                                RuntimeTypeAdapterFactory.of(OutputMappingItemRef.class)
                                        .registerSubtype(PixliteDatalineRef.class)
                        )

                        .create();
        }

        public static Mappings loadMappingData(Environment environment) {
                try (FileReader reader = new FileReader(Utils.sketchFile(environment.getMappingsFilename()))) {
                        return gson.fromJson(reader, Mappings.class);
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

        @Expose private Map<String, OutputMapping> outputs;

        public <OutputMappingType extends OutputMapping> OutputMappingType getOutputById(String id, Class<OutputMappingType> type) {
                if (outputs == null) {
                        outputs = new TreeMap<>();
                }
                OutputMapping output = outputs.get(id);
                if (output == null) {
                        output = ClassUtils.tryCreateObject(type);
                        if (output == null) return null;
                        outputs.put(id, output);
                }
                Supplier<Stream<MappingItem>> mappingItems = () -> getAllDescendantItems()
                        .filter(item -> {
                                OutputMappingItemRef ref = item.getOutput();
                                return ref != null && id.equals(ref.outputId);
                        });
                output.collectRefs(mappingItems);
                return ClassUtils.tryCast(output, type);
        }

        public Collection<String> getOutputIds() {
                return outputs.keySet();
        }
}
