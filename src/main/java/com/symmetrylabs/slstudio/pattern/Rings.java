package com.symmetrylabs.slstudio.pattern;

import com.symmetrylabs.util.MathUtils;
import com.symmetrylabs.util.NoiseHelper;
import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.PolyBuffer;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;

import static heronarts.lx.PolyBuffer.Space.SRGB8;
import static processing.core.PApplet.map;

public class Rings extends LXPattern {
    float dx, dy, dz;
    float angleParam, spacingParam;
    float dzParam, centerParam;

    private NoiseHelper noiseHelper = new NoiseHelper();
    CompoundParameter paletteV = new CompoundParameter("Hue", 0, 0, 360);
    CompoundParameter pDepth = new CompoundParameter("Depth", 0.6);
    CompoundParameter pBright = new CompoundParameter("Bright", 0.75);
    CompoundParameter pSaturation = new CompoundParameter("Sat", 0.5);

    CompoundParameter pSpeed1 = new CompoundParameter("Speed1", 0.2);
    CompoundParameter pSpeed2 = new CompoundParameter("Speed2", 0.4);
    CompoundParameter pScale = new CompoundParameter("Scale", 0.65);

    public Rings(LX lx) {
        super(lx);
        addParameter(paletteV);
        addParameter(pDepth);
        addParameter(pBright);
        addParameter(pSaturation);

        addParameter(pSpeed1);
        addParameter(pSpeed2);
        addParameter(pScale);
    }

    public void run(double deltaMs, PolyBuffer.Space space) {
        int[] colors = (int[]) getArray(SRGB8);

        final float xyspeed = pSpeed1.getValuef() * 0.01f;
        final float zspeed = pSpeed1.getValuef() * 0.08f;
        final float scale = pScale.getValuef() * 20.0f;
        final float br = pBright.getValuef() * 3.0f;
        final float gamma = 3.0f;
        final float depth = 1.0f - pDepth.getValuef();
        final float saturation = pSaturation.getValuef() * 100.0f;

        final float angleSpeed = pSpeed1.getValuef() * 0.002f;
        angleParam = (float) ((angleParam + angleSpeed * deltaMs) % (2 * (float)Math.PI));
        final float angle = MathUtils.sin(angleParam);

        spacingParam += (float) deltaMs * pSpeed2.getValuef() * 0.001f;
        dzParam += (float) deltaMs * 0.000014f;
        centerParam += (float) deltaMs * pSpeed2.getValuef() * 0.001f;

        final float spacing = noiseHelper.noise(spacingParam) * 50.0f;

        dx += MathUtils.cos(angle) * xyspeed;
        dy += MathUtils.sin(angle) * xyspeed;
        dz += (MathUtils.pow(noiseHelper.noise(dzParam), 1.8f) - 0.5f) * zspeed;

        final float centerx = map(noiseHelper.noise(centerParam, 100.0f), 0.0f, 1.0f, -0.1f, 1.1f);
        final float centery = map(noiseHelper.noise(centerParam, 200.0f), 0.0f, 1.0f, -0.1f, 1.1f);
        final float centerz = map(noiseHelper.noise(centerParam, 300.0f), 0.0f, 1.0f, -0.1f, 1.1f);

        final float coordMin = MathUtils.min(model.xMin, MathUtils.min(model.yMin, model.zMin));
        final float coordMax = MathUtils.max(model.xMax, MathUtils.max(model.yMax, model.zMax));

        noiseHelper.noiseDetail(4);

        getVectorList().parallelStream().forEach(p -> {
            // Scale while preserving aspect ratio
            float x = map(p.x, coordMin, coordMax, 0.0f, 1.0f);
            float y = map(p.y, coordMin, coordMax, 0.0f, 1.0f);
            float z = map(p.z, coordMin, coordMax, 0.0f, 1.0f);

            float dist = MathUtils.dist(x, y, z, centerx, centery, centerz);
            float pulse = (MathUtils.sin(dz + dist * spacing) - 0.3f) * 0.6f;

            float n = map(noiseHelper.noise(
                    dx + (x - centerx) * scale + centerx + pulse,
                    dy + (y - centery) * scale + centery,
                    dz + (z - centerz) * scale + centerz
                ) - depth,
                0.0f, 1.0f, 0.0f, 2.0f
            );

            float brightness = 100.0f * MathUtils.constrain(MathUtils.pow(br * n, gamma), 0.0f, 1.0f);
            if (brightness == 0) {
                colors[p.index] = LXColor.BLACK;
                return;
            }

            float m = map(noiseHelper.noise(
                    dx + (x - centerx) * scale + centerx,
                    dy + (y - centery) * scale + centery,
                    dz + (z - centerz) * scale + centerz
                ),
                0.0f, 1.0f, 0.0f, 300.0f
            );

            colors[p.index] = LXColor.hsb(paletteV.getValuef() + m, saturation, brightness);
        });

        noiseHelper.noiseDetail(1);
        polyBuffer.markModified(SRGB8);
    }
}
