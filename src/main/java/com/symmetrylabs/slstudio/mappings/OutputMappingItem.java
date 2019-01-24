package com.symmetrylabs.slstudio.mappings;

public interface OutputMappingItem {
    default void mappingItemWasAdded(MappingItem mappingItem) {}
    default void mappingItemWasRemoved(MappingItem mappingItem) {}
}
