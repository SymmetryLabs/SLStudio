package com.symmetrylabs.slstudio.util;

import org.apache.commons.math3.util.FastMath;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class FixedWidthOctree<T> {
    private final int depth;
    private final float centerX, centerY, centerZ;
    private final float width;

    private final List<Entry<T>> points;
    private final FixedWidthOctree[] children;

    public FixedWidthOctree(float centerX, float centerY, float centerZ, float width, int depth) {
        this.depth = depth;
        this.centerX = centerX;
        this.centerY = centerY;
        this.centerZ = centerZ;
        this.width = width;

        points = new ArrayList<Entry<T>>();
        children = new FixedWidthOctree[8];
    }

    private FixedWidthOctree ensureChild(int i) {
        if (children[i] == null) {
            float step = width * 0.25f;
            float offsetX = (((i & 1) == 0) ? step : -step);
            float offsetY = (((i & 2) == 0) ? step : -step);
            float offsetZ = (((i & 4) == 0) ? step : -step);

            children[i] = new FixedWidthOctree(
                centerX + offsetX, centerY + offsetY, centerZ + offsetZ,
                step * 2, depth - 1
            );
        }

        return children[i];
    }

    public void insert(float x, float y, float z, T object) {
        if (depth > 0) {
            int index = 0;
            if (x < centerX) index |= 1;
            if (y < centerY) index |= 2;
            if (z < centerZ) index |= 4;

            ensureChild(index).insert(x, y, z, object);
        } else {
            points.add(new Entry(x, y, z, object));
        }
    }

    // NOTE: we're using Manhattan distance here
    public List<T> withinDistance(final float x, final float y, final float z, final float d) {
        final List<T> objectsWithin = new ArrayList<>(points.size());

        if (!points.isEmpty()) {


            points.parallelStream().filter(new Predicate<Entry<T>>() {
                public boolean test(Entry<T> p) {
                    return FastMath.abs(p.x - x) < d && FastMath.abs(p.y - y) < d && FastMath.abs(p.z - z) < d;
                }
            }).sequential().forEach(new Consumer<Entry<T>>() {
                public void accept(Entry<T> entry) {
                    objectsWithin.add(entry.object);
                }
            });
        }

        if (depth > 0) {
            int cleanCutX = 0;
            int cleanCutY = 0;
            int cleanCutZ = 0;

            if (FastMath.abs(centerX - x) < d) {
                cleanCutX = -1;
            } else if (x < centerX) {
                cleanCutX = 1;
            }

            if (FastMath.abs(centerY - y) < d) {
                cleanCutY = -1;
            } else if (y < centerY) {
                cleanCutY = 1;
            }

            if (FastMath.abs(centerZ - z) < d) {
                cleanCutZ = -1;
            } else if (z < centerZ) {
                cleanCutZ = 1;
            }

            boolean[] eliminatedChildren = new boolean[8];

            if (cleanCutX == 1) {
                eliminatedChildren[0] = true;
                eliminatedChildren[2] = true;
                eliminatedChildren[4] = true;
                eliminatedChildren[6] = true;
            } else if (cleanCutX == 0) {
                eliminatedChildren[1] = true;
                eliminatedChildren[3] = true;
                eliminatedChildren[5] = true;
                eliminatedChildren[7] = true;
            }

            if (cleanCutY == 1) {
                eliminatedChildren[0] = true;
                eliminatedChildren[1] = true;
                eliminatedChildren[4] = true;
                eliminatedChildren[5] = true;
            } else if (cleanCutY == 0) {
                eliminatedChildren[2] = true;
                eliminatedChildren[3] = true;
                eliminatedChildren[6] = true;
                eliminatedChildren[7] = true;
            }

            if (cleanCutZ == 1) {
                eliminatedChildren[0] = true;
                eliminatedChildren[1] = true;
                eliminatedChildren[2] = true;
                eliminatedChildren[3] = true;
            } else if (cleanCutZ == 0) {
                eliminatedChildren[4] = true;
                eliminatedChildren[5] = true;
                eliminatedChildren[6] = true;
                eliminatedChildren[7] = true;
            }

            List<FixedWidthOctree> survivingChildren = new ArrayList<>();
            for (int i = 0; i < 8; ++i) {
                if (!eliminatedChildren[i] && children[i] != null) {
                    survivingChildren.add(children[i]);
                }
            }

            survivingChildren.parallelStream().map(new Function<FixedWidthOctree, List<T>>() {
                public List<T> apply(FixedWidthOctree child) {
                    return child.withinDistance(x, y, z, d);
                }
            }).sequential().forEach(new Consumer<List<T>>() {
                public void accept(List<T> childPoints) {
                    objectsWithin.addAll(childPoints);
                }
            });
        }

        return objectsWithin;
    }

    public T nearest(float x, float y, float z) {
        int index = 0;
        if (x < centerX) index |= 1;
        if (y < centerY) index |= 2;
        if (z < centerZ) index |= 4;

        FixedWidthOctree<T> child = children[index];
        if (child == null || child.totalPointCount() == 0) {
            Entry<T> nearestEntry = null;
            float nearestDistSqr = Float.MAX_VALUE;
            for (Entry<T> p : points) {
                float dx = p.x - x;
                float dy = p.y - y;
                float dz = p.z - z;
                float dSqr = dx * dx + dy * dy + dz * dz;

                if (dSqr < nearestDistSqr) {
                    nearestDistSqr = dSqr;
                    nearestEntry = p;
                }
            }

            if (nearestEntry == null)
                return null;

            return nearestEntry.object;
        }

        return child.nearest(x, y, z);
    }

    public String dump() {
        return dump(0);
    }

    public int totalPointCount() {
        int total = points.size();

        for (FixedWidthOctree c : children) {
            if (c == null)
                continue;

            total += c.totalPointCount();
        }

        return total;
    }

    public int childCount() {
        int total = 0;
        for (FixedWidthOctree c : children) {
            if (c == null)
                continue;

            ++total;
        }

        return total;
    }

    private String dump(int level) {
        StringBuilder s = new StringBuilder();

        String firstPrefix = "";
        String prefix = level == 0 ? "" : "  ";
        for (int i = 0; i < level - 1; ++i) {
            firstPrefix += "  ";
            prefix += "  ";
        }

        if (level > 0) {
            firstPrefix += "- ";
        }

        s.append(firstPrefix).append("Depth: ").append(depth).append('\n');
        s.append(prefix).append("Width: ").append(width).append('\n');
        s.append(prefix).append("Center: (").append(centerX).append(", ")
            .append(centerY).append(", ").append(centerZ).append(")").append('\n');

        if (points.isEmpty()) {
            s.append(prefix).append("Total Points: ").append(totalPointCount()).append('\n');
        } else {
            s.append(prefix).append("Own Points: ").append(points.size()).append('\n');
        }

        if (childCount() > 0) {
            s.append(prefix).append("Children (").append(childCount()).append("):\n");

            for (FixedWidthOctree c : children) {
                if (c == null)
                    continue;

                s.append(c.dump(level + 1));
            }
        }

        return s.toString();
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
