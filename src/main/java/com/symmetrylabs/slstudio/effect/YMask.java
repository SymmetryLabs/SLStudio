package com.symmetrylabs.slstudio.effect;

import com.symmetrylabs.util.MathUtils;
import com.symmetrylabs.util.NoiseUtils;
import heronarts.lx.LX;
import heronarts.lx.LXEffect;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.transform.LXVector;

public class YMask extends LXEffect {
    private CompoundParameter minY = new CompoundParameter("minY",  model.yMin, model.yMin, model.yMax);
    private CompoundParameter maxY = new CompoundParameter("maxY",  model.yMax, model.yMin, model.yMax);
    private CompoundParameter noiseTop = new CompoundParameter("noiseTop", 60, 0, 100);
    private CompoundParameter noiseScale = new CompoundParameter("noiseScale", 0.03, 0, 0.1);
    private CompoundParameter noiseSpeed = new CompoundParameter("noiseSpeed", 0.62, 0, 1.5);
    private CompoundParameter fadeDist = new CompoundParameter("fadeDist", 50.19, 1, 100);




    float yAcc = 0.0f;


    public YMask(LX lx) {
        super(lx);
        addParameter(minY);
        addParameter(maxY);
        addParameter(noiseTop);
        addParameter(noiseScale);
        addParameter(noiseSpeed);
        addParameter(fadeDist);

    }

    @Override
    public void run(double deltaMs, double amount) {
        for (LXVector v : getVectors()) {
            float newMax = maxY.getValuef() + noiseTop.getValuef() * NoiseUtils.noise(v.x * noiseScale.getValuef(), yAcc);
            float newMin = minY.getValuef();
            if (v.y < newMax && v.y > newMin) {
                continue;
            }
            float dist = 0;
            if (v.y > newMax) {
                dist = v.y - newMax;
            }
            if (v.y < newMin) {
                dist = newMin - v.y;
            }
            float rat = MathUtils.constrain(dist / fadeDist.getValuef(), 0, 1);
            int c = colors[v.index];
            float b = LXColor.b(c) * (1.0f - rat);
            colors[v.index] = LXColor.hsb(LXColor.h(c), LXColor.s(c), b);
        }
        yAcc += (noiseSpeed.getValuef()) * (deltaMs / 1000.0f);
    }
}
