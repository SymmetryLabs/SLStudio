package com.symmetrylabs.slstudio.mappings;

import com.google.gson.annotations.Expose;
import com.symmetrylabs.util.ClassUtils;

import java.util.*;
import java.util.stream.Stream;

public class MappingGroup {

    @Expose private Map<String, MappingGroup> children;
        @Expose private List<MappingItem> items;

        public Map<String, MappingGroup> getChildren() {
        if (children == null) {
                        children = new TreeMap<>();
                }
                return children;
        }

        public MappingGroup getChildById(String childId) {
        MappingGroup child = getChildren().get(childId);
        if (child == null) {
            child = new MappingGroup();
                        getChildren().put(childId, child);
                }
                return child;
        }

        public MappingGroup getChildByIdIfExists(String childId) {
        return children != null ? children.get(childId) : null;
        }

        public List<MappingItem> getItems() {
                if (items == null) {
                        items = new ArrayList<>();
                }
        return items;
        }

        public MappingItem getItemByIndex(int i) {
        if (i >= getItems().size()) return null;
                return getItems().get(i);
        }

        public <MappingsItemType extends MappingItem> MappingsItemType getItemByIndex(int i, Class<MappingsItemType> type) {
                while (i >= getItems().size()) {
                        MappingItem item = ClassUtils.tryCreateObject(type);
                        if (item == null) return null;
                        getItems().add(item);
                }
                return ClassUtils.tryCast(getItems().get(i), type);
        }

        public Stream<MappingItem> getAllDescendantItems() {
                Stream<MappingItem> stream = items != null ? getItems().stream() : Stream.empty();
        if (children != null) {
                        stream = Stream.concat(stream,
                                getChildren().values().stream()
                                        .flatMap(MappingGroup::getAllDescendantItems));
                }
                return stream;
        }

}
