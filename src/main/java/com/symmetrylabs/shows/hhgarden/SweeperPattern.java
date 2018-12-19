package com.symmetrylabs.shows.hhgarden;

import com.symmetrylabs.color.Ops16;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import com.symmetrylabs.slstudio.pattern.instruments.MarkUtils;

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

    public SweeperPattern(LX lx) {
        super(lx);

        sweepers = new Sweeper[] {
          new Sweeper("A", 1000, 390, 400, 0, 0.5f),
            new Sweeper("B", 674, 616, 400, 0.5f, 1)
        };

        addParameter(hueParam);
        addParameter(hueVarParam);
        addParameter(satParam);

        addParameter(attackParam);
        addParameter(decayParam);
    };

    protected void run(double deltaMs, PolyBuffer.Space preferredSpace) {
        long[] colors = (long[]) getPolyBuffer().getArray(RGB16);
        Arrays.fill(colors, 0);
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
        public float stopAngle;

        private CompoundParameter param;
        private float lastParamValue;
        private int[] indexes;
        private boolean[] states;
        private float[] angles;
        private float[] amplitudes;

        public Sweeper(String name, float x, float y, float radius, float startAngle, float stopAngle) {
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.startAngle = startAngle;
            this.stopAngle = stopAngle;
            param = new CompoundParameter(name);
            SweeperPattern.this.addParameter(param);

            List<LXPoint> points = MarkUtils.getAllPointsWithin(model, new LXVector(x, y, model.cz), radius);
            indexes = new int[points.size()];
            states = new boolean[points.size()];
            angles = new float[points.size()];
            amplitudes = new float[points.size()];
            int i = 0;
            for (LXPoint point : points) {
                indexes[i] = point.index;
                angles[i] = (float) (Math.atan2(point.y - y, point.x - x) / (2 * Math.PI));
                amplitudes[i] = 0;
                i++;
            }
            lastParamValue = param.getValuef();
        }

        public void run(double deltaMs, PolyBuffer buffer) {
            float deltaSec = (float) deltaMs / 1000;
            float attackSec = attackParam.getValuef();
            float decaySec = decayParam.getValuef();

            float min = Math.min(lastParamValue, param.getValuef());
            float max = Math.max(lastParamValue, param.getValuef());
            lastParamValue = param.getValuef();

            for (int i = 0; i < indexes.length; i++) {
                float fraction = angles[i];
                if (min <= fraction && fraction < max) {
                    states[i] = true;
                }
                if (states[i]) {
                    amplitudes[i] += deltaSec / attackSec;
                    if (amplitudes[i] >= 1) {
                        states[i] = false;
                    }
                } else {
                    amplitudes[i] -= deltaSec / decaySec;
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
        }
    }
}
