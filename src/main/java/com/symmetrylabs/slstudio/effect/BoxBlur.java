package com.symmetrylabs.slstudio.effect;

import com.symmetrylabs.color.Ops16;
import com.symmetrylabs.color.Ops8;
import com.symmetrylabs.util.FixedWidthOctree;
import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.transform.LXVector;
import com.symmetrylabs.slstudio.component.HiddenComponent;

import java.util.List;
import java.util.stream.IntStream;

@HiddenComponent
public class BoxBlur extends SLEffect {
    private int indexes[];
    private int neighbors[][];
    private int maxNeighbors;

    private final CompoundParameter radiusParam
        = new CompoundParameter("radius", 5, 0.1, 50);
    private float radiusForCurrentKernel;

    public BoxBlur(LX lx) {
        super(lx);
        addParameter(radiusParam);
        initKernels();
    }

    private void initKernels() {
        final FixedWidthOctree<LXVector> octree = new FixedWidthOctree<>(
            model.cx, model.cy, model.cz, 2 * model.rMax,
            /* use the log-base-8 of the number of points we have as the
             * depth; for perfectly-distributed endpoints this would put
             * four points in each partition. */
            (int) Math.ceil(Math.log(model.size) / Math.log(8) / 4));

        if (octree.totalPointCount() == 0) {
            for (LXVector v : getVectors()) {
                octree.insert(v.x, v.y, v.z, v);
            }
        }

        float radius = radiusParam.getValuef();

        radiusForCurrentKernel = (float) radius;

        /* This is calculated in the loop to take into account null vectors */
        int nVectors = 0;
        maxNeighbors = 0;
        for (LXVector v : getVectors()) {
            nVectors++;
            List<LXVector> neighbors = octree.withinDistance(v.x, v.y, v.z, radius);
            maxNeighbors = Integer.max(neighbors.size(), maxNeighbors);
        }
        indexes = new int[nVectors];
        neighbors = new int[nVectors][maxNeighbors];

        int i = 0;
        for (LXVector v : getVectors()) {
            indexes[i] = v.index;
            List<LXVector> nslist = octree.withinDistance(v.x, v.y, v.z, radius);
            int N = nslist.size();

            for (int j = 0; j < N; j++) {
                neighbors[i][j] = nslist.get(j).index;
            }
            for (int j = N; j < maxNeighbors; j++) {
                neighbors[i][j] = -1;
            }
            i++;
        }
    }

    @Override
    public void run(double deltaMs, double amount, PolyBuffer.Space preferredSpace) {
        if (Math.abs(radiusForCurrentKernel - radiusParam.getValuef()) > 1) {
            initKernels();
        }

        switch (preferredSpace) {
            case SRGB8:
            case RGB8: {
                int[] c = (int[]) getArray(preferredSpace);
                int[] res = new int[c.length];
                IntStream.range(0, indexes.length).parallel().forEach(gid -> {
                    int r = 0;
                    int g = 0;
                    int b = 0;
                    int a = 0;
                    int index = indexes[gid];
                    int N = 0;
                    for (int i = 0; i < maxNeighbors && neighbors[gid][i] != -1; i++) {
                        int nc = c[neighbors[gid][i]];
                        r += Ops8.red(nc);
                        g += Ops8.green(nc);
                        b += Ops8.blue(nc);
                        a += Ops8.alpha(nc);
                        N++;
                    }
                    r /= N;
                    g /= N;
                    b /= N;
                    a /= N;
                    res[index] = Ops8.rgba(r, g, b, a);
                });

                for (int i = 0; i < c.length; i++) {
                    c[i] = res[i];
                }
                break;
            }

            case RGB16: {
                long[] c = (long[]) getArray(preferredSpace);
                long[] res = new long[c.length];
                IntStream.range(0, indexes.length).parallel().forEach(gid -> {
                    long r = 0;
                    long g = 0;
                    long b = 0;
                    long a = 0;
                    int index = indexes[gid];
                    int N = 0;
                    for (int i = 0; i < maxNeighbors && neighbors[gid][i] != -1; i++) {
                        long nc = c[neighbors[gid][i]];
                        r += Ops16.red(nc);
                        g += Ops16.green(nc);
                        b += Ops16.blue(nc);
                        a += Ops16.alpha(nc);
                        N++;
                    }
                    r /= N;
                    g /= N;
                    b /= N;
                    a /= N;
                    res[index] = Ops16.rgba((int) r, (int) g, (int) b, (int) a);
                });

                for (int i = 0; i < c.length; i++) {
                    c[i] = res[i];
                }
                break;
            }
        }

        markModified(preferredSpace);
    }
}
