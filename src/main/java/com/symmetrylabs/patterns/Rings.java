package com.symmetrylabs.patterns;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.parameter.*;
import heronarts.lx.modulator.*;
import heronarts.lx.model.LXPoint;
import heronarts.lx.color.LXColor;

import com.symmetrylabs.util.MathUtils;
import com.symmetrylabs.util.NoiseUtils;

public class Rings extends LXPattern {
    float dx, dy, dz;
    float angleParam, spacingParam;
    float dzParam, centerParam;

    CompoundParameter pDepth = new CompoundParameter("DEPTH", 0.6);
    CompoundParameter pBright = new CompoundParameter("BRT", 0.75);
    CompoundParameter pSaturation = new CompoundParameter("SAT", 0.5);

    CompoundParameter pSpeed1 = new CompoundParameter("SPD1", 0.2);
    CompoundParameter pSpeed2 = new CompoundParameter("SPD2", 0.4);
    CompoundParameter pScale = new CompoundParameter("SCALE", 0.15);

    public Rings(LX lx) {
        super(lx);
        addParameter(pDepth);
        addParameter(pBright);
        addParameter(pSaturation);

        addParameter(pSpeed1);
        addParameter(pSpeed2);
        addParameter(pScale);
    }

    public void run(double deltaMs) {

        float xyspeed = pSpeed1.getValuef() * 0.01f;
        float zspeed = pSpeed1.getValuef() * 0.08f;
        float scale = pScale.getValuef() * 20.0f;
        float br = pBright.getValuef() * 3.0f;
        float gamma = 3.0f;
        float depth = 1.0f - pDepth.getValuef();
        float saturation = pSaturation.getValuef() * 100.0f;

        float angleSpeed = pSpeed1.getValuef() * 0.002f;
        angleParam = (float)((angleParam + angleSpeed * deltaMs) % (2*(float)Math.PI));
        float angle = (float)Math.sin(angleParam);

        spacingParam += (float)deltaMs * pSpeed2.getValuef() * 0.001f;
        dzParam += (float)deltaMs * 0.000014f;
        centerParam += (float)deltaMs * pSpeed2.getValuef() * 0.001f;

        float spacing = NoiseUtils.noise(spacingParam) * 50.0f;

        dx += (float)Math.cos(angle) * xyspeed;
        dy += (float)Math.sin(angle) * xyspeed;
        dz += (float)(Math.pow(NoiseUtils.noise(dzParam), 1.8f) - 0.5f) * zspeed;

        float centerx = MathUtils.map(NoiseUtils.noise(centerParam, 100.0f), 0.0f, 1.0f, -0.1f, 1.1f);
        float centery = MathUtils.map(NoiseUtils.noise(centerParam, 200.0f), 0.0f, 1.0f, -0.1f, 1.1f);
        float centerz = MathUtils.map(NoiseUtils.noise(centerParam, 300.0f), 0.0f, 1.0f, -0.1f, 1.1f);

        float coordMin = (float)Math.min(model.xMin, (float)Math.min(model.yMin, model.zMin));
        float coordMax = (float)Math.max(model.xMax, (float)Math.max(model.yMax, model.zMax));

        NoiseUtils.noiseDetail(4);
        for (LXPoint p : model.points) {
            // Scale while preserving aspect ratio
            float x = MathUtils.map(p.x, coordMin, coordMax, 0.0f, 1.0f);
            float y = MathUtils.map(p.y, coordMin, coordMax, 0.0f, 1.0f);
            float z = MathUtils.map(p.z, coordMin, coordMax, 0.0f, 1.0f);

            float dist = (float)Math.sqrt(Math.pow((x - centerx),2) + Math.pow((y - centery),2) + Math.pow((z - centerz),2));
            float pulse = (float)(Math.sin(dz + dist * spacing) - 0.3f) * 0.6f;

            float n = MathUtils.map(NoiseUtils.noise(dx + (x - centerx) * scale + centerx + pulse,
                                                    dy + (y - centery) * scale + centery,
                                                    dz + (z - centerz) * scale + centerz)
                                        - depth, 0.0f, 1.0f, 0.0f, 2.0f);

            float brightness = 100.0f * MathUtils.constrain((float)Math.pow(br * n, gamma), 0.0f, 1.0f);
            if (brightness == 0) {
                colors[p.index] = LXColor.BLACK;
                continue;
            }

            float m = MathUtils.map(NoiseUtils.noise(dx + (x - centerx) * scale + centerx,
                                                    dy + (y - centery) * scale + centery,
                                                    dz + (z - centerz) * scale + centerz),
                                        0.0f, 1.0f, 0.0f, 300.0f);

            colors[p.index] = lx.hsb(
                palette.getHuef() + m,
                saturation,
                brightness
                );
        }
        NoiseUtils.noiseDetail(1);
    }
};
