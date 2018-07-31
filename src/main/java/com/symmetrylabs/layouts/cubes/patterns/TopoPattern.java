package com.symmetrylabs.layouts.cubes.patterns;

import com.jogamp.common.util.ArrayHashSet;
import com.symmetrylabs.layouts.cubes.CubesModel;
import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;

import java.util.*;

public abstract class TopoPattern extends SLPattern<CubesModel> {
    public static final int NO_EDGE = Integer.MAX_VALUE;
    public enum EdgeDirection {
        X, Y, Z, Other
    }

    public class TopoEdge {
        EdgeDirection dir = EdgeDirection.Other;
        int i   = NO_EDGE;
        TopoEdge pa  = null;
        TopoEdge pbp = null;
        TopoEdge pbn = null;
        TopoEdge pcp = null;
        TopoEdge pcn = null;
        TopoEdge na  = null;
        TopoEdge nbp = null;
        TopoEdge nbn = null;
        TopoEdge ncp = null;
        TopoEdge ncn = null;

        SortBucket sortBucket() {
            Strip s = model.getStripByIndex(i);
            switch (dir) {
                case X: return new SortBucket(dir, s.cy, s.cz);
                case Y: return new SortBucket(dir, s.cx, s.cz);
                case Z: return new SortBucket(dir, s.cx, s.cy);
                default: return null;
            }
        }

        /** An edge's "order" is the projection of it's centroid onto the axis it's aligned with */
        float order() {
            Strip s = model.getStripByIndex(i);
            switch (dir) {
                case X: return s.cx;
                case Y: return s.cy;
                case Z: return s.cz;
                default: return 0;
            }
        }

        /** Min-order is the minimum value of the projection of this edge onto its axis */
        float minOrder() {
            Strip s = model.getStripByIndex(i);
            switch (dir) {
                case X: return s.xMin;
                case Y: return s.yMin;
                case Z: return s.zMin;
                default: return 0;
            }
        }

        /** Max-order is the maximum value of the projection of this edge onto its axis */
        float maxOrder() {
            Strip s = model.getStripByIndex(i);
            switch (dir) {
                case X: return s.xMax;
                case Y: return s.yMax;
                case Z: return s.zMax;
                default: return 0;
            }
        }

        /** Checks whether this edge and the other edge are close enough to be considered connected.
         * @param other The other edge to check. This method assumes that the other edge is in the same sort bucket.
         */
        private boolean closeTo(TopoEdge other) {
            Strip a = model.getStripByIndex(i);
            Strip b = model.getStripByIndex(other.i);
            float a1, a2, b1, b2, d;

            switch (dir) {
                case X:
                    a1 = a.xMin; a2 = a.xMax;
                    b1 = b.xMin; b2 = b.xMax;
                    d = a.xRange / 2;
                    break;
                case Y:
                    a1 = a.yMin; a2 = a.yMax;
                    b1 = b.yMin; b2 = b.yMax;
                    d = a.yRange / 2;
                    break;
                case Z:
                    a1 = a.zMin; a2 = a.zMax;
                    b1 = b.zMin; b2 = b.zMax;
                    d = a.zRange / 2;
                    break;
                default:
                    throw new IllegalArgumentException("cannot compare non-aligned edges");
            }

            return Math.abs(a1 - b1) < d
                || Math.abs(a1 - b2) < d
                || Math.abs(a2 - b1) < d
                || Math.abs(a2 - b2) < d;
        }
    }

    protected static class SortBucket {
        EdgeDirection dir;
        float a;
        float b;

        public SortBucket(EdgeDirection dir, float a, float b) {
            this.dir = dir;
            this.a = a;
            this.b = b;
        }

        public boolean equivalent(SortBucket sb) {
            return dir == sb.dir && Math.abs(a - sb.a) < 1e-2 && Math.abs(b - sb.b) < 1e-2;
        }
    }

    public final List<TopoEdge> edges;

    public TopoPattern(LX lx) {
        super(lx);

        int N = model.getStrips().size();
        edges = new ArrayList<>(N);

        for (int i = 0; i < N; i++) {
            Strip s = model.getStripByIndex(i);

            TopoEdge e = new TopoEdge();
            e.i = i;
            if (s.xRange < 1e-3 && s.yRange < 1e-3) {
                e.dir = EdgeDirection.Z;
            } else if (s.xRange < 1e-3 && s.zRange < 1e-3) {
                e.dir = EdgeDirection.Y;
            } else if (s.yRange < 1e-3 && s.zRange < 1e-3) {
                e.dir = EdgeDirection.X;
            } else {
                System.out.println(String.format(
                    "model strip not axis-aligned: x=%f y=%f z=%f", s.xRange, s.yRange, s.zRange));
                e.dir = EdgeDirection.Other;
            }

            edges.add(e);
        }

        HashMap<SortBucket, ArrayList<TopoEdge>> sortmap = new HashMap<>();

        for (TopoEdge e : edges) {
            Strip s = model.getStripByIndex(e.i);

            SortBucket sb = e.sortBucket();
            for (SortBucket testsb : sortmap.keySet()) {
                if (testsb.equivalent(sb)) {
                    sb = testsb;
                    break;
                }
            }
            if (sortmap != null) {
                sortmap.putIfAbsent(sb, new ArrayList<>());
                sortmap.get(sb).add(e);
            }
        }

        sortmap.forEach((sb, edgeBucket) -> edgeBucket.sort(Comparator.comparingDouble(a -> a.order())));
        sortmap.forEach((sb, edgeBucket) -> {
            for (int i = 0; i < edgeBucket.size(); i++) {
                TopoEdge e = edgeBucket.get(i);
                if (i < edgeBucket.size() - 1) {
                    TopoEdge n = edgeBucket.get(i + 1);
                    if (e.closeTo(n))
                        e.na = n;
                }
                if (i > 0) {
                    TopoEdge p = edgeBucket.get(i - 1);
                    if (e.closeTo(p))
                        e.pa = p;
                }
            }
        });
    }
}
