import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class FixedWidthOctree<T> {
    private final int depth;
    private final float centerX, centerY, centerZ;
    private final float width;

    private final FixedWidthOctree[] children;
    private final List<T> points;

    public FixedWidthOctree(float centerX, float centerY, float centerZ, float width, int depth) {
        this.depth = depth;
        this.centerX = centerX;
        this.centerY = centerY;
        this.centerZ = centerZ;
        this.width = width;

        points = new ArrayList<T>();

        if (depth > 0) {
            children = new FixedWidthOctree[8];

            float step = width * 0.25f;
            for (int i = 0; i < 8; ++i) {
                float offsetX = (((i & 1) == 0) ? step : -step);
                float offsetY = (((i & 2) == 0) ? step : -step);
                float offsetZ = (((i & 4) == 0) ? step : -step);

                children[i] = new FixedWidthOctree(centerX + offsetX, centerY + offsetY, centerZ + offsetZ, step * 2, depth - 1);
            }
        }
        else {
            children = null;
        }
    }

    public void insert(float x, float y, float z, T obj) {
        if (depth > 0) {
            int index = 0;
            if (x < centerX) index |= 1;
            if (y < centerY) index |= 2;
            if (z < centerZ) index |= 4;

            children[index].insert(x, y, z, obj);
        }
        else {
            points.add(obj);
        }
    }

    public List<T> withinDistance(float x, float y, float z, float d) {
        List<T> childPoints = new ArrayList<>(points);

        if (depth > 0) {
            int cleanCutX = 0;
            int cleanCutY = 0;
            int cleanCutZ = 0;

            if (Math.abs(centerX - x) < d) {
                cleanCutX = -1;
            }
            else if (x < centerX) {
                cleanCutX = 1;
            }

            if (Math.abs(centerY - y) < d) {
                cleanCutY = -1;
            }
            else if (y < centerY) {
                cleanCutY = 1;
            }

            if (Math.abs(centerZ - z) < d) {
                cleanCutZ = -1;
            }
            else if (z < centerZ) {
                cleanCutZ = 1;
            }

            boolean[] eliminatedChildren = new boolean[8];

            if (cleanCutX == 1) {
                eliminatedChildren[0] = true;
                eliminatedChildren[2] = true;
                eliminatedChildren[4] = true;
                eliminatedChildren[6] = true;
            }
            else if (cleanCutX == 0) {
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
            }
            else if (cleanCutY == 0) {
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
            }
            else if (cleanCutZ == 0) {
                eliminatedChildren[4] = true;
                eliminatedChildren[5] = true;
                eliminatedChildren[6] = true;
                eliminatedChildren[7] = true;
            }

            for (int i = 0; i < 8; ++i) {
                if (!eliminatedChildren[i]) {
                    childPoints.addAll(children[i].withinDistance(x, y, z, d));
                }
            }
        }

        return childPoints;
    }

    public T nearest(float x, float y, float z) {
        return null;
    }
}
