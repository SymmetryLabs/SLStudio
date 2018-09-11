package com.symmetrylabs.slstudio.effect;

import com.aparapi.Kernel;
import com.symmetrylabs.color.Ops16;
import com.symmetrylabs.color.Ops8;
import com.symmetrylabs.util.FixedWidthOctree;
import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.transform.LXVector;

import java.util.List;
import java.util.stream.IntStream;

public class GaussianBlur extends SLEffect {
    private int indexes[];
    private int neighbors[][];
    private float coeffs[][];
    private int maxNeighbors;

    private final CompoundParameter stdDevParam = new CompoundParameter("stdev", 4, 0.1, 8);
    private float stdDevForCurrentKernels;

    public GaussianBlur(LX lx) {
        super(lx);
        addParameter(stdDevParam);
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

        double stdev = stdDevParam.getValue();
        double Gscale = Math.pow(1 / (2 * Math.PI * stdev * stdev), 1.5);
        double expScale = 1 / (2 * stdev * stdev);

        stdDevForCurrentKernels = (float) stdev;

        final LXVector[] vs = getVectorArray();
        indexes = new int[vs.length];

        maxNeighbors = 0;
        for (LXVector v : vs) {
            List<LXVector> neighbors = octree.withinDistance(v.x, v.y, v.z, (float) (3 * stdev));
            maxNeighbors = Integer.max(neighbors.size(), maxNeighbors);
        }
        coeffs = new float[vs.length][maxNeighbors];
        neighbors = new int[vs.length][maxNeighbors];

        for (int i = 0; i < vs.length; i++) {
            LXVector v = vs[i];
            indexes[i] = v.index;
            List<LXVector> nslist = octree.withinDistance(v.x, v.y, v.z, (float) (3 * stdev));
            int N = nslist.size();
            float scale = 0;

            for (int j = 0; j < N; j++) {
                LXVector n = nslist.get(j).copy().mult(-1);
                float distSq = n.add(v).magSq();
                float coeff = (float) (Gscale * Math.exp(-distSq * expScale));
                neighbors[i][j] = n.index;
                coeffs[i][j] = coeff;
                scale += coeff;
            }
            for (int j = N; j < maxNeighbors; j++) {
                neighbors[i][j] = -1;
            }

            scale = 1f / (float) Math.pow(scale, 0.25);
            for (int j = 0; j < N; j++) {
                coeffs[i][j] *= scale;
            }
        }
    }

    @Override
    public void run(double deltaMs, double amount, PolyBuffer.Space preferredSpace) {
        if (Math.abs(stdDevForCurrentKernels - stdDevParam.getValuef()) > 1) {
            initKernels();
        }

        switch (preferredSpace) {
            case SRGB8:
            case RGB8: {
                int[] c = (int[]) getArray(preferredSpace);
                int[] res = new int[c.length];
                IntStream.range(0, indexes.length).parallel().forEach(gid -> {
                    int csum = 0;
                    int index = indexes[gid];
                    for (int i = 0; i < maxNeighbors && neighbors[gid][i] != -1; i++) {
                        csum = Ops8.add(csum, Ops8.multiply(c[neighbors[gid][i]], 100 * coeffs[gid][i]));
                    }
                    res[index] = csum;
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
                    long csum = 0;
                    int index = indexes[gid];
                    int[] ns = neighbors[gid];
                    float[] cs = coeffs[gid];
                    for (int i = 0; i < maxNeighbors && ns[i] != -1; i++) {
                        csum = Ops16.add(csum, Ops16.multiply(c[ns[i]], 100 * cs[i]));
                    }
                    res[index] = csum;
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
