package com.symmetrylabs.layouts.cubes.topology;

import heronarts.lx.model.LXPoint;

import java.util.*;

public class EdgeAStar {
    public class NotConnectedException extends Exception {}

    private final CubeTopology topology;

    private final HashSet<CubeTopology.Bundle> closed = new HashSet<>();
    private final HashSet<CubeTopology.Bundle> open = new HashSet<>();
    private final HashMap<CubeTopology.Bundle, CubeTopology.Bundle> cameFrom = new HashMap<>();
    private final HashMap<CubeTopology.Bundle, Float> gScore = new HashMap<>();
    private final HashMap<CubeTopology.Bundle, Float> fScore = new HashMap<>();

    private CubeTopology.Bundle current;
    private CubeTopology.Bundle target;

    public EdgeAStar(CubeTopology topology) {
        this.topology = topology;
    }

    private float dist(CubeTopology.Bundle a, CubeTopology.Bundle b) {
        float ax = (a.endpoints().start.x + a.endpoints().end.x) / 2f;
        float ay = (a.endpoints().start.y + a.endpoints().end.y) / 2f;
        float az = (a.endpoints().start.z + a.endpoints().end.z) / 2f;
        float bx = (b.endpoints().start.x + b.endpoints().end.x) / 2f;
        float by = (b.endpoints().start.y + b.endpoints().end.y) / 2f;
        float bz = (b.endpoints().start.z + b.endpoints().end.z) / 2f;
        return (float) Math.sqrt(Math.pow(ax - bx, 2) + Math.pow(ay - by, 2) + Math.pow(az - bz, 2));
    }

    public List<CubeTopology.Bundle> findPath(CubeTopology.Bundle start, CubeTopology.Bundle end) throws NotConnectedException {
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
            for (CubeTopology.Bundle b : open) {
                Float bScore = fScore.get(b);
                if (bScore < currentFScore) {
                    current = b;
                    currentFScore = bScore;
                }
            }
            if (current == end)
                break;

            open.remove(current);
            closed.add(current);

            visitNeighbor(current.na);
            visitNeighbor(current.pa);
            visitNeighbor(current.nbn);
            visitNeighbor(current.nbp);
            visitNeighbor(current.pbn);
            visitNeighbor(current.pbp);
            visitNeighbor(current.ncn);
            visitNeighbor(current.ncp);
            visitNeighbor(current.pcn);
            visitNeighbor(current.pcp);
        }

        if (current != end)
            throw new NotConnectedException();

        LinkedList<CubeTopology.Bundle> res = new LinkedList<>();
        do {
            res.addFirst(current);
            current = cameFrom.get(current);
        } while (current != start);
        return res;
    }

    private void visitNeighbor(CubeTopology.Bundle neighbor) {
        if (neighbor == null)
            return;
        if (closed.contains(neighbor))
            return;

        // this node is now up for grabs
        open.add(neighbor);

        // the cost of getting to this neighbor from the current node
        float thisScore = gScore.get(current) + dist(current, neighbor);

        // if we already have a better path to this node, then there's nothing to do
        if (thisScore >= gScore.getOrDefault(neighbor, Float.MAX_VALUE))
            return;

        cameFrom.put(neighbor, current);
        gScore.put(neighbor, thisScore);
        fScore.put(neighbor, thisScore + dist(neighbor, target));
    }
}
