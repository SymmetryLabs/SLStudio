package com.symmetrylabs.util;

import com.symmetrylabs.slstudio.model.StripsTopology;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class EdgeAStar {
    public class NotConnectedException extends Exception {
    }

    private final StripsTopology topology;

    private final HashSet<StripsTopology.Bundle> closed = new HashSet<>();
    private final HashSet<StripsTopology.Bundle> open = new HashSet<>();
    private final HashMap<StripsTopology.Bundle, StripsTopology.Bundle> cameFrom = new HashMap<>();
    private final HashMap<StripsTopology.Bundle, Float> gScore = new HashMap<>();
    private final HashMap<StripsTopology.Bundle, Float> fScore = new HashMap<>();

    private StripsTopology.Bundle current;
    private StripsTopology.Bundle target;

    public EdgeAStar(StripsTopology topology) {
        this.topology = topology;
    }

    private float dist(StripsTopology.Bundle a, StripsTopology.Bundle b) {
        float ax = (a.endpoints().negative.x + a.endpoints().positive.x) / 2f;
        float ay = (a.endpoints().negative.y + a.endpoints().positive.y) / 2f;
        float az = (a.endpoints().negative.z + a.endpoints().positive.z) / 2f;
        float bx = (b.endpoints().negative.x + b.endpoints().positive.x) / 2f;
        float by = (b.endpoints().negative.y + b.endpoints().positive.y) / 2f;
        float bz = (b.endpoints().negative.z + b.endpoints().positive.z) / 2f;
        return (float) Math.sqrt(Math.pow(ax - bx, 2) + Math.pow(ay - by, 2) + Math.pow(az - bz, 2));
    }

    public List<StripsTopology.Bundle> findPath(StripsTopology.Bundle start, StripsTopology.Bundle end) throws NotConnectedException {
        target = end;

        closed.clear();
        open.clear();
        cameFrom.clear();
        gScore.clear();
        fScore.clear();

        open.add(start);
        gScore.put(start, 0f);
        fScore.put(start, dist(start, end));

        while (!open.isEmpty()) {
            current = null;
            float currentFScore = Float.MAX_VALUE;
            for (StripsTopology.Bundle b : open) {
                Float bScore = fScore.get(b);
                if (bScore < currentFScore) {
                    current = b;
                    currentFScore = bScore;
                }
            }
            if (current == end) {
                break;
            }

            open.remove(current);
            closed.add(current);

            for (StripsTopology.Sign s1 : StripsTopology.Sign.values()) {
                for (StripsTopology.Dir d : StripsTopology.Dir.values()) {
                    for (StripsTopology.Sign s2 : StripsTopology.Sign.values()) {
                        visitNeighbor(current.get(s1).get(d, s2));
                    }
                }
            }
        }

        if (current != end) {
            throw new NotConnectedException();
        }

        LinkedList<StripsTopology.Bundle> res = new LinkedList<>();
        do {
            res.addFirst(current);
            current = cameFrom.get(current);
        }
        while (current != start && current != null); // current can be null if current == start == end on the first go-through
        res.addFirst(start);

        return res;
    }

    private void visitNeighbor(StripsTopology.Bundle neighbor) {
        if (neighbor == null) {
            return;
        }
        if (closed.contains(neighbor)) {
            return;
        }

        // this node is now up for grabs
        open.add(neighbor);

        // the cost of getting to this neighbor from the current node
        float thisScore = gScore.get(current) + dist(current, neighbor);

        // if we already have a better path to this node, then there's nothing to do
        if (thisScore >= gScore.getOrDefault(neighbor, Float.MAX_VALUE)) {
            return;
        }

        cameFrom.put(neighbor, current);
        gScore.put(neighbor, thisScore);
        fScore.put(neighbor, thisScore + dist(neighbor, target));
    }
}
