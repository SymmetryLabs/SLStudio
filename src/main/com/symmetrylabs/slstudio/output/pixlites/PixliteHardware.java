package com.symmetrylabs.slstudio.output.pixlites;

import com.google.gson.GsonBuilder;
import com.symmetrylabs.slstudio.mappings.MappingItem;
import com.symmetrylabs.slstudio.mappings.OutputMapping;
import com.symmetrylabs.slstudio.mappings.OutputMappingItemRef;
import com.symmetrylabs.slstudio.mappings.StripMapping;
import com.symmetrylabs.slstudio.mappings.pixlite.PixliteDatalineRef;
import com.symmetrylabs.slstudio.mappings.pixlite.PixliteMapping;
import com.symmetrylabs.slstudio.model.suns.SunsModel;
import com.symmetrylabs.slstudio.output.Hardware;
import com.symmetrylabs.util.RuntimeTypeAdapterFactory;
import heronarts.lx.LX;
import heronarts.lx.output.LXOutput;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PixliteHardware extends Hardware {

        public Map<String, int[]> mappingColorsPerPixlite = new HashMap<>();

        @Override
        protected void configureGsonBuilder(GsonBuilder gsonBuilder) {
                gsonBuilder
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
                                );

        }

        @Override
        public LXOutput[] createOutputs(LX lx) {
                List<Pixlite> pixlites = new ArrayList<>();

                for (String outputId : getMappings().getOutputIds()) {
                        PixliteMapping pixliteMapping = getMappings().getOutputById(outputId, PixliteMapping.class);
                        if (pixliteMapping != null) {
                                pixlites.add(createPixlite(lx, pixliteMapping, outputId));
                        }
                }

                for (Pixlite pixlite : pixlites) {
                        this.mappingColorsPerPixlite.put(pixlite.slice.id, pixlite.mappingColors);
                }

                return pixlites.toArray(new Pixlite[0]);
        }

        private Pixlite createPixlite(LX lx, PixliteMapping pixliteMapping, String sliceId) {
                SunsModel sunsModel = (SunsModel)lx.model;
                Pixlite pixlite = new Pixlite(getMappings(), pixliteMapping, lx, sunsModel.getSliceById(sliceId));
                lx.addOutput(pixlite);
                return pixlite;
        }

}
