package com.symmetrylabs.slstudio.mappings;

import java.util.function.Supplier;
import java.util.stream.Stream;

public interface OutputMapping {
    void collectRefs(Supplier<Stream<MappingItem>> mappingItems);
}
