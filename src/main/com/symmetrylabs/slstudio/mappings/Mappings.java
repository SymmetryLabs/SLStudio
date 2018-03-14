package com.symmetrylabs.slstudio.mappings;

import com.google.gson.annotations.Expose;
import com.symmetrylabs.util.ClassUtils;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class Mappings extends MappingGroup {

        @Expose private Map<String, OutputMapping> outputs;

        private Map<String, OutputMapping> getOutputs() {
                if (outputs == null) {
                        outputs = new TreeMap<>();
                }
                return outputs;
        }

        public <OutputMappingType extends OutputMapping> OutputMappingType getOutputById(String id, Class<OutputMappingType> type) {
                OutputMapping output = getOutputs().get(id);
                if (output == null) {
                        output = ClassUtils.tryCreateObject(type);
                        if (output == null) return null;
                        getOutputs().put(id, output);
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
                return getOutputs().keySet();
        }

}
