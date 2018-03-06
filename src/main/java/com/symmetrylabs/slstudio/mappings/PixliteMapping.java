package com.symmetrylabs.slstudio.mappings;

import com.google.gson.annotations.Expose;
import heronarts.lx.model.LXPoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class PixliteMapping implements OutputMapping {

        public static final int NUM_DATALINES = PixliteMapping.NUM_DATALINES;

        @Expose public String ipAddress;
        @Expose private DatalineMapping[] datalineMappings;

        public DatalineMapping[] getDatalineMappings() {
                if (datalineMappings == null) {
                        datalineMappings = new DatalineMapping[NUM_DATALINES];
                        for (int i = 0; i < datalineMappings.length; i++) {
                                datalineMappings[i] = new DatalineMapping();
                        }
                }
                return datalineMappings;
        }

        @Override
        public void collectRefs(Supplier<Stream<MappingItem>> mappingItems) {
                int datalineIndex = 0;
                for (DatalineMapping datalineMapping : getDatalineMappings()) {
                        final int i = datalineIndex;
                        datalineMapping.collectRefs(mappingItems.get()
                                .filter(item -> {
                                        PixliteDatalineRef ref = item.getOutputAs(PixliteDatalineRef.class);
                                        return ref != null && ref.datalineIndex == i;
                                }));
                        datalineIndex++;
                }
        }

        public static class DatalineMapping implements OutputMappingItem {

                @Expose public int numPoints;

                public List<MappingItem> mappingItems;
                public LXPoint[] points;

                void collectRefs(Stream<MappingItem> mappingItems) {
                        if (this.mappingItems != null) return;
                        this.mappingItems = new ArrayList<>();
                        mappingItems
                                .sorted(Comparator.comparingInt(a -> a.getOutputAs(PixliteDatalineRef.class).datalineOrderIndex))
                                .forEach(mappingItem -> mappingItem.assignOutputObj(this));
                        points = this.mappingItems.stream().flatMap(item -> Arrays.stream(item.points)).toArray(LXPoint[]::new);
                }

                public void moveMappingItemToIndex(MappingItem mappingItem, int index) {
                        mappingItems.remove(mappingItem);
                        mappingItems.add(index, mappingItem);
                        readjustIndices();
                }

                @Override
                public void mappingItemWasAdded(MappingItem mappingItem) {
                        mappingItems.add(mappingItem);
                        readjustIndices();
                }

                @Override
                public void mappingItemWasRemoved(MappingItem mappingItem) {
                        mappingItems.remove(mappingItem);
                        readjustIndices();
                }

                private void readjustIndices() {
                        for (int i = 0; i < mappingItems.size(); i++) {
                                mappingItems.get(i).getOutputAs(PixliteDatalineRef.class).datalineOrderIndex = i;
                        }
                }

        }

}
