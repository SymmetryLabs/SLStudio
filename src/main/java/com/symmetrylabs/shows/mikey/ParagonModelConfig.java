package com.symmetrylabs.shows.mikey;

import com.symmetrylabs.util.FileUtils;

public class ParagonModelConfig {
    public final ConfigModel model;

    public ParagonModelConfig(ConfigModel model) {
        this.model = model;
    }

    public static class ConfigModel {
        public final String type;
        public final String label;
        public final ConfigFixture[] fixtures;

        public ConfigModel(String type, String label, ConfigFixture[] fixtures) {
            this.type = type;
            this.label = label;
            this.fixtures = fixtures;
        }
    }

    public static class ConfigFixture {
        public final String id;
        public final String label;
        public final String fixtureType;
        public final boolean isNativeType;
        public final float[] origin;
        public final ConfigFixture[] fixtures;

        public ConfigFixture(String id, String label, String fixtureType, boolean isNativeType, float[] origin, ConfigFixture[] fixtures) {
            this.id = id;
            this.label = label;
            this.fixtureType = fixtureType;
            this.isNativeType = isNativeType;
            this.origin = origin;
            this.fixtures = fixtures;
        }
    }

    public static ParagonModelConfig load() {
        return FileUtils.readShowJson("formatted.pgm", ParagonModelConfig.class);
    }
}
