package com.symmetrylabs.shows.pilots;

import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import com.symmetrylabs.util.MathUtils;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.transform.LXVector;

import java.util.ArrayList;
import java.util.List;

import static com.symmetrylabs.util.MathConstants.PI;
import static java.lang.StrictMath.atan2;

public class PilotsPinwheels extends SLPattern<SLModel> {

    private CompoundParameter horizSpreadParameter = new CompoundParameter("HSpr", 0.75);
    private CompoundParameter vertSpreadParameter = new CompoundParameter("VSpr", 0.5);
    private CompoundParameter vertOffsetParameter = new CompoundParameter("VOff", 1.0);
    private CompoundParameter zSlopeParameter = new CompoundParameter("ZSlp", 0.6);
    private CompoundParameter sharpnessParameter = new CompoundParameter("Shrp", 0.25);
    private CompoundParameter derezParameter = new CompoundParameter("Drez", 0.25);
    private CompoundParameter clickinessParameter = new CompoundParameter("Clic", 0.5);
    private CompoundParameter hueParameter = new CompoundParameter("Hue", 0.667);
    private CompoundParameter hueSpreadParameter = new CompoundParameter("HSpd", 0.667);
    private BooleanParameter freezeParameter = new BooleanParameter("freeze", false);

    float phase = 0;
    private final int NUM_BLADES = 12;

    class Pinwheel {
        LXVector center;
        int numBlades;
        float realPhase;
        float phase;
        float speed;

        Pinwheel(float xCenter, float yCenter, int numBlades, float speed) {
            this.center = new LXVector(xCenter, yCenter, 0);
            this.numBlades = numBlades;
            this.speed = speed;
        }

        void age(float numBeats) {
            int numSteps = numBlades;

            realPhase = (realPhase + numBeats / numSteps) % 2.0f;

            float phaseStep = MathUtils.floor(realPhase * numSteps);
            float phaseRamp = (realPhase * numSteps) % 1.0f;
            phase = (phaseStep + MathUtils.pow(phaseRamp, (clickinessParameter.getValuef() * 10) + 1)) / (numSteps * 2);
//      phase = (phase + deltaMs / 1000.0 * speed) % 1.0;
        }

        boolean isOnBlade(float x, float y) {
            x = x - center.x;
            y = y - center.y;

            float normalizedAngle = (((float) atan2(x, y)) / (2f * PI) + 1f + phase) % 1f;
            float v;
            v = normalizedAngle * 4 * numBlades;
            int blade_num = MathUtils.floor((v + 2) / 4);
            return (blade_num % 2) == 0;
        }
    }

    private final List<Pinwheel> pinwheels;
    private final float[] values;

    public PilotsPinwheels(LX lx) {
        super(lx);

        addParameter(horizSpreadParameter);
//    addParameter(vertSpreadParameter);
        addParameter(vertOffsetParameter);
        addParameter(zSlopeParameter);
        addParameter(sharpnessParameter);
        addParameter(derezParameter);
        addParameter(clickinessParameter);
        addParameter(hueParameter);
        addParameter(hueSpreadParameter);
        addParameter(freezeParameter);

        pinwheels = new ArrayList();
        pinwheels.add(new Pinwheel(0, 0, NUM_BLADES, 0.1f));
        pinwheels.add(new Pinwheel(0, 0, NUM_BLADES, -0.1f));

        this.updateHorizSpread();
        this.updateVertPositions();

        values = new float[model.points.length]; // was -> model.points.size()
    }

    public void onParameterChanged(LXParameter parameter) {
        if (parameter == horizSpreadParameter) {
            updateHorizSpread();
        } else if (parameter == vertSpreadParameter || parameter == vertOffsetParameter) {
            updateVertPositions();
        }
    }

    private void updateHorizSpread() {
        float xDist = model.xMax - model.xMin;
        float xCenter = (model.xMin + model.xMax) / 2;

        float spread = horizSpreadParameter.getValuef() - 0.5f;
        pinwheels.get(0).center.x = xCenter - xDist * spread;
        pinwheels.get(1).center.x = xCenter + xDist * spread;
    }

    private void updateVertPositions() {
        float yDist = model.yMax - model.yMin;
        float yCenter = model.yMin + yDist * vertOffsetParameter.getValuef();

        float spread = vertSpreadParameter.getValuef() - 0.5f;
        pinwheels.get(0).center.y = yCenter - yDist * spread;
        pinwheels.get(1).center.y = yCenter + yDist * spread;
    }

    private float prevRamp = 0;

    public void run(double deltaMs) {
        for (int i = 0; i < colors.length; i++) {
            colors[i] = LXColor.BLACK;
        }

        float ramp = lx.tempo.rampf();
        float numBeats = (1 + ramp - prevRamp) % 1;
        prevRamp = ramp;

        float hue = hueParameter.getValuef() * 360;
        // 0 -> -180
        // 0.5 -> 0
        // 1 -> 180
        float hueSpread = (hueSpreadParameter.getValuef() - 0.5f) * 360;

        float fadeAmount = (float) (deltaMs / 1000.0) * MathUtils.pow(sharpnessParameter.getValuef() * 10, 1);

        if (!freezeParameter.getValueb()) {
            for (Pinwheel pw : pinwheels) {
                pw.age(numBeats);
            }
        }

        float derez = derezParameter.getValuef();

        float zSlope = (zSlopeParameter.getValuef() - 0.5f) * 2;

        int i = -1;
        for (LXVector p : getVectors()) {
            ++i;

            int value = 0;
            for (Pinwheel pw : pinwheels) {
                value += (pw.isOnBlade(p.x, p.y - p.z * zSlope) ? 1 : 0);
            }
            if (value == 1) {
                values[i] = 1;
//        colors[p.index] = lx.hsb(120, 0, 100);
            } else {
                values[i] = MathUtils.max(0, values[i] - fadeAmount);
                //color c = colors[p.index];
                //colors[p.index] = lx.hsb(max(0, lx.h(c) - 10), min(100, lx.s(c) + 10), lx.b(c) - 5 );
            }

            if (MathUtils.random(1.0f) >= derez) {
                float v = values[i];
                colors[p.index] = lx.hsb(360 + hue + MathUtils.pow(v, 2) * hueSpread, 30 + MathUtils.pow(1 - v, 0.25f) * 60, v * 100);
            }
        }
    }
}
