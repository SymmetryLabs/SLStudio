package com.symmetrylabs.util;

import java.util.List;
import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import java.util.function.Supplier;
import java.util.function.Function;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.apache.commons.math3.util.FastMath;

public class HashOctree<T> {
    private final float centerX, centerY, centerZ;
    private final float width;
    private final int depth;

    private SortedMap<String, List<Entry<T>>> buckets = new TreeMap<String, List<Entry<T>>>();

    public HashOctree(float centerX, float centerY, float centerZ, float width, int depth) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.centerZ = centerZ;
        this.width = width;
        this.depth = depth;
    }

    private String getBucketAddress(float x, float y, float z) {
        StringBuilder address = new StringBuilder("1");

        for (int i = 0; i < depth; ++i) {
            byte index = 0;
            if (x >= centerX) index |= 1;
            if (y >= centerY) index |= 2;
            if (z >= centerZ) index |= 4;
            address.append(index);
        }

        return address.toString();
    }

    public void insert(float x, float y, float z, T object) {
        String bucketAddress = getBucketAddress(x, y, z);
        if (!buckets.containsKey(bucketAddress)) {
            buckets.put(bucketAddress, new ArrayList<Entry<T>>());
        }

        buckets.get(bucketAddress).add(new Entry(x, y, z, object));
    }

    public List<T> withinDistance(final float x, final float y, final float z, final float d) {
        String bucketAddress = getBucketAddress(x, y, z);

        String startAddress = "1";
        String endAddress = "18";

        final float maxSqrDist = d * d;
        return buckets.subMap(startAddress, endAddress).values().parallelStream().flatMap(new Function<List<Entry<T>>, Stream<T>>() {
            public Stream<T> apply(List<Entry<T>> entries) {
                return entries.stream().filter(new Predicate<Entry<T>>() {
                    public boolean test(Entry<T> entry) {
                        float dx = entry.x - x;
                        float dy = entry.y - y;
                        float dz = entry.z - z;
                        float dSqr = dx * dx + dy * dy + dz * dz;

                        return dSqr <= maxSqrDist;
                    }
                }).map(new Function<Entry<T>, T>() {
                    public T apply(Entry<T> entry) {
                        return entry.object;
                    }
                });
            }
        }).collect(Collectors.toCollection(new Supplier<List<T>>() {
            public List<T> get() {
                return new ArrayList<>();
            }
        }));
    }

    public T nearest(float x, float y, float z) {
        String bucketAddress = getBucketAddress(x, y, z);

        for (int i = depth; i > 0; --i) {
            Entry<T> nearestEntry = null;
            float nearestDistSqr = Float.MAX_VALUE;
            String startAddress = bucketAddress.substring(0, i);
            String endAddress = startAddress + "8";
            for (List<Entry<T>> entries : buckets.subMap(startAddress, endAddress).values()) {
                for (Entry<T> entry : entries) {
                    float dx = entry.x - x;
                    float dy = entry.y - y;
                    float dz = entry.z - z;
                    float dSqr = dx * dx + dy * dy + dz * dz;

                    if (dSqr < nearestDistSqr) {
                        nearestDistSqr = dSqr;
                        nearestEntry = entry;
                    }
                }
            }

            if (nearestEntry != null)
                return nearestEntry.object;
        }

        return null;
    }

    private static class Entry<T> {
        public float x;
        public float y;
        public float z;
        public T object;

        Entry(float x, float y, float z, T object) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.object = object;
        }
    }
}
