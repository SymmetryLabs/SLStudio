package com.symmetrylabs.shows.hhgarden;

import com.symmetrylabs.color.Ops16;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import com.symmetrylabs.slstudio.pattern.instruments.MarkUtils;
import com.symmetrylabs.slstudio.pattern.instruments.PointPartition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.transform.LXVector;

import static heronarts.lx.PolyBuffer.Space.RGB16;

public class SweeperPattern extends SLPattern<SLModel> {
    private final CompoundParameter hueParam = new CompoundParameter("Hue", 0, -1, 1);
    private final CompoundParameter hueVarParam = new CompoundParameter("HueVar", 0, 0, 1);
    private final CompoundParameter satParam = new CompoundParameter("Sat", 0, 0, 1);

    private final CompoundParameter attackParam = new CompoundParameter("Attack", 0.5, 0, 1);
    private final CompoundParameter decayParam = new CompoundParameter("Decay", 0.5, 0, 2);

    protected Sweeper[] sweepers;
    protected PointPartition partition;

    public SweeperPattern(LX lx) {
        super(lx);

        partition = new PointPartition(model.getPoints(), 30);

        sweepers = new Sweeper[] {
            new Sweeper("A", 1068, 713, true, 450, 0f, 0.55f),
            new Sweeper("B", 416, 653, true, 450, -0.04f, 0.7f),
            new Sweeper("C", 674, 616, true, 400, 0.4f, 0.67f),
          new Sweeper("D", 1062, 330, false, 418, 0.51f, -0.51f),
        };

        addParameter(hueParam);
        addParameter(hueVarParam);
        addParameter(satParam);

        addParameter(attackParam);
        addParameter(decayParam);
    };

    protected void run(double deltaMs, PolyBuffer.Space preferredSpace) {
        long[] colors = (long[]) getPolyBuffer().getArray(RGB16);
        Arrays.fill(colors, Ops16.BLACK);
        getPolyBuffer().markModified(RGB16);
        for (Sweeper sweeper : sweepers) {
            sweeper.run(deltaMs, getPolyBuffer());
        }
    }

    class Sweeper {
        public float x;
        public float y;
        public float radius;
        public float startAngle;
        public float sweepAngle;

        private CompoundParameter param;
        private float lastParamValue;
        private int[] indexes;
        private boolean[] states;
        private float[] positions;
        private float[] amplitudes;

        public Sweeper(String name, float x, float y, boolean useArcCluster, float radius, float startAngle, float sweepAngle) {
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.startAngle = floorMod(startAngle, 1);
            this.sweepAngle = sweepAngle;
            param = new CompoundParameter(name);
            SweeperPattern.this.addParameter(param);

            LXVector center = new LXVector(x, y, model.cz);
            LXVector clusterCenter = new LXVector(center);
            if (useArcCluster) {
                double radians = (startAngle + sweepAngle/2) * (2 * Math.PI);
                clusterCenter.x += Math.cos(radians) * radius;
                clusterCenter.y += Math.sin(radians) * radius;
            }
            int cluster = partition.getClusterNumber(MarkUtils.getNearestPoint(model.getPoints(), clusterCenter));
            List<LXPoint> points = new ArrayList<>();
            for (LXPoint point : MarkUtils.getAllPointsWithin(model, center, radius)) {
              if (partition.getClusterNumber(point) == cluster) {
                  points.add(point);
                }
            }
            indexes = new int[points.size()];
            states = new boolean[points.size()];
            positions = new float[points.size()];
            amplitudes = new float[points.size()];
            int i = 0;
            for (LXPoint point : points) {
                indexes[i] = point.index;
                float bearing = floorMod((float) (Math.atan2(point.y - y, point.x - x) / (2 * Math.PI)), 1);
                if (sweepAngle > 0) {
                    positions[i] = floorMod(bearing - startAngle, 1) / sweepAngle;
                } else {
                    positions[i] = floorMod(startAngle - bearing, 1) / -sweepAngle;
                }
                amplitudes[i] = 0;
                i++;
            }
            lastParamValue = param.getValuef();
        }

        protected float floorMod(float num, float den) {
            float quo = (float) Math.floor(num / den);
            return num - (quo * den);
        }

        public void run(double deltaMs, PolyBuffer buffer) {
            float deltaSec = (float) deltaMs / 1000;
            float attackSec = attackParam.getValuef();
            float decaySec = decayParam.getValuef();

            float nextParamValue = param.getValuef();
            float min = Math.min(lastParamValue, nextParamValue);
            float max = Math.max(lastParamValue, nextParamValue);

            for (int i = 0; i < indexes.length; i++) {
                float position = positions[i];
                if (min <= position && position < max) {
                    states[i] = true;
                    float progress = (nextParamValue - position) / (nextParamValue - lastParamValue);
                    amplitudes[i] += deltaSec * progress / attackSec;
                } else if (states[i]) {
                    amplitudes[i] += deltaSec / attackSec;
                } else {
                    amplitudes[i] -= deltaSec / decaySec;
                }
                if (amplitudes[i] >= 1) {
                    states[i] = false;
                }
                amplitudes[i] = Math.max(0, Math.min(1, amplitudes[i]));
            }

            double hue = hueParam.getValue() + hueVarParam.getValue() * param.getValuef();
            double sat = satParam.getValue();
            long[] colors = (long[]) buffer.getArray(RGB16);
            for (int i = 0; i < indexes.length; i++) {
                if (amplitudes[i] > 0) {
                    MarkUtils.addColor(colors, indexes[i], Ops16.hsb(hue, sat, amplitudes[i]));
                }
            }
            buffer.markModified(RGB16);

            lastParamValue = nextParamValue;
        }
    }
}
