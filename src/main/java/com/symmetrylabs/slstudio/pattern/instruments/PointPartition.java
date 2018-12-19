package com.symmetrylabs.slstudio.pattern.instruments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXVector;

public class PointPartition {
    protected final List<List<LXVector>> clusters = new ArrayList<>();
    protected final Map<Integer, Integer> clusterNumbersByIndex = new HashMap<>();

    public PointPartition(Iterable<LXPoint> points, float maxSep) {
        SortedSet<LXVector> sortedVectors = new TreeSet<>((a, b) ->
            (a.x > b.x) ? 1 :
                (a.x < b.x) ? -1 : Integer.compare(a.index, b.index)
        );

        for (LXPoint point : points) {
            sortedVectors.add(new LXVector(point));
        }

        while (!sortedVectors.isEmpty()) {
            List<LXVector> cluster = new ArrayList<>();
            List<LXVector> pending = new ArrayList<>();
            LXVector first = sortedVectors.first();
            sortedVectors.remove(first);
            cluster.add(first);
            pending.add(first);

            while (!pending.isEmpty()) {
                LXVector vector = pending.remove(0);
                for (LXVector neighbor : getNeighbors(sortedVectors, vector, maxSep)) {
                    if (sortedVectors.contains(neighbor)) {
                        sortedVectors.remove(neighbor);
                        cluster.add(neighbor);
                        pending.add(neighbor);
                    }
                }
            }
            clusters.add(cluster);
        }

        for (int c = 0; c < clusters.size(); c++) {
            for (LXVector vector : clusters.get(c)) {
                clusterNumbersByIndex.put(vector.index, c);
            }
        }
    }

    public int getNumClusters() {
        return clusters.size();
    }

    public List<LXPoint> getCluster(int c) {
        List<LXPoint> points = new ArrayList<>();
        for (LXVector vector : clusters.get(c)) {
            points.add(vector.point);
        }
        return points;
    }

    public int getClusterNumber(LXPoint point) {
        return clusterNumbersByIndex.get(point.index);
    }

    public int getClusterNumber(int index) {
        return clusterNumbersByIndex.get(index);
    }

    protected List<LXVector> getNeighbors(SortedSet<LXVector> sortedVectors, LXVector center, float maxSep) {
        LXVector lowKey = new LXVector(center.x - maxSep, 0, 0);
        LXVector highKey = new LXVector(center.x + maxSep, 0, 0);
        List<LXVector> neighbors = new ArrayList<>();
        for (LXVector vector : sortedVectors.subSet(lowKey, highKey)) {
            if (vector.dist(center) < maxSep) {
                neighbors.add(vector);
            }
        }
        return neighbors;
    }
}
