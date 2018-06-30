package com.symmetrylabs.slstudio.pattern;

//import com.symmetrylabs.util.MathUtils;
import com.symmetrylabs.util.MathUtils;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.transform.LXProjection;
import heronarts.lx.transform.LXVector;

import static com.symmetrylabs.util.MathConstants.PI;
//import static com.symmetrylabs.util.MathUtils.*;

public class TimMetronome extends LXPattern {
    private CompoundParameter clickyParameter = new CompoundParameter("CLICK", 0, 0, 10.0);
    private CompoundParameter derezParameter = new CompoundParameter("DREZ", 0.5, 0, 1.0);
    private CompoundParameter driftParameter = new CompoundParameter("DRIFT", 0, 0, 1.0);
    private CompoundParameter fadeParameter = new CompoundParameter("FADE", 0.05, 0, 0.2);
    private float modelWidth;
    private int beatNum;
    private float prevTempoRamp;
    private LXProjection projection;
    private float[] values;
    private float[] hues;

    public TimMetronome(LX lx) {
        super(lx);
        addParameter(clickyParameter);
        addParameter(derezParameter);
        addParameter(driftParameter);
        addParameter(fadeParameter);
        modelWidth = model.xMax - model.xMin;
        projection = new LXProjection(model);
        beatNum = 0;
        prevTempoRamp = 0;
        values = new float[model.points.length];
        hues = new float[model.points.length];
    }

    public void run(double deltaMs) {
        float tempoRamp = lx.tempo.rampf();
        if (tempoRamp < prevTempoRamp) {
            beatNum = (beatNum + 1) % 1000;
        }
        prevTempoRamp = tempoRamp;

        float phase = beatNum + MathUtils.pow(tempoRamp, 1.0f + clickyParameter.getValuef());

        projection.reset();
        projection.translateCenter(model.xMin, model.yMin, model.cz);
        projection.rotate(phase * 0.5f * PI, 0, 0, 1);

        projection.translate(driftParameter.getValuef() * tempoRamp * modelWidth * 0.5f, 0, 0);

        float derezCutoff = derezParameter.getValuef();

        float fadeMultiplier = (1.0f - fadeParameter.getValuef());

        float armRadius = modelWidth * 0.1f;
        for (LXVector p : projection) {
            boolean onArm = false;
            if (MathUtils.abs(p.x) < armRadius) {
                onArm = (p.y > 0) || (MathUtils.sqrt(MathUtils.pow(p.x, 2) + MathUtils.pow(p.y, 2)) < armRadius);
            }
            if (onArm) {
                values[p.index] = 1.0f;
                hues[p.index] = MathUtils.floor(phase / 4) * 90;
            } else {
                values[p.index] *= fadeMultiplier;
            }

            float saturation = MathUtils.pow(1 - values[p.index], 0.5f) * 0.7f + 0.3f;
            float brightness = values[p.index];

            if (MathUtils.random(1.0f) > derezCutoff) {
                colors[p.index] = lx.hsb(hues[p.index], saturation * 100, brightness * 100);
            }
        }
    }
}
