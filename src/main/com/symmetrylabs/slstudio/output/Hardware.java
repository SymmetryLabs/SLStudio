package com.symmetrylabs.slstudio.output;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.symmetrylabs.slstudio.Environment;
import com.symmetrylabs.slstudio.mappings.Mappings;
import com.symmetrylabs.slstudio.util.Utils;
import heronarts.lx.LX;
import heronarts.lx.output.LXOutput;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;

public abstract class Hardware {

        private Environment environment;
        private Gson gson;
        private Mappings mappings;
        private LXOutput[] outputs;
        private boolean noMappingsFileFound = false;

        public void setEnvironment(Environment environment) {
                this.environment = environment;
        }

        protected /* abstract */ void configureGsonBuilder(GsonBuilder gsonBuilder) {
        }

        private Gson getGson() {
                if (gson == null) {
                        GsonBuilder gsonBuilder = new GsonBuilder()
                                        .setPrettyPrinting()
                                        .excludeFieldsWithoutExposeAnnotation();
                        configureGsonBuilder(gsonBuilder);
                        gson = gsonBuilder.create();
                }
                return gson;
        }

        protected abstract LXOutput[] createOutputs(LX lx);

        public LXOutput[] setupOutputs(LX lx) {
                outputs = createOutputs(lx);
                if (noMappingsFileFound) {
                        noMappingsFileFound = false;
                        saveMappings();
                }
                return outputs;
        }

        public LXOutput[] getOutputs() {
                if (outputs == null) {
                        throw new RuntimeException("Hardware: must run setupOutputs() before getOutputs()");
                }
                return outputs;
        }

        public Mappings getMappings() {
                if (mappings == null) {
                        mappings = loadMappingData(environment);
                }
                return mappings;
        }

        public void saveMappings() {
                saveMappingData(environment, mappings);
        }

        private Mappings loadMappingData(Environment environment) {
                try (FileReader reader = new FileReader(Utils.sketchFile(environment.getMappingsFilename()))) {
                        return getGson().fromJson(reader, Mappings.class);
                } catch (FileNotFoundException e) {
                        noMappingsFileFound = true;
                        return new Mappings();
                } catch (java.io.IOException e) {
                        System.out.println("Failed to load mapping data");
                        e.printStackTrace();
                        return null;
                }
        }

        private void saveMappingData(Environment environment, Mappings mappings) {
                try (FileWriter writer = new FileWriter(Utils.sketchFile(environment.getMappingsFilename()))) {
                        getGson().toJson(mappings, writer);
                } catch (java.io.IOException e) {
                        System.out.println("Failed to save mapping data");
                        e.printStackTrace();
                }
        }

}
