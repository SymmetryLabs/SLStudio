package com.symmetrylabs.slstudio.pattern;

import com.symmetrylabs.color.Ops8;
import com.symmetrylabs.util.MathUtils;
import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.PolyBuffer;
import heronarts.lx.color.LXColor;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.CompoundParameter;

import static heronarts.lx.PolyBuffer.Space.SRGB8;

public class Spheres extends LXPattern {
    private CompoundParameter hueParameter = new CompoundParameter("Size", 1.0);
    private CompoundParameter periodParameter = new CompoundParameter("Period", 4000.0, 200.0, 10000.0);
    private CompoundParameter hueVariance = new CompoundParameter("HueVar", 50, 0, 180);
    private final SawLFO lfo = new SawLFO(0, 1, 10000);
    private final SinLFO sinLfo = new SinLFO(0, 1, periodParameter);
    private final float centerX, centerY, centerZ;

    class Sphere {
        float x, y, z;
        float radius;
        float hue;
    }

    private final Sphere[] spheres;

    public Spheres(LX lx) {
        super(lx);
        addParameter(hueParameter);
        addParameter(periodParameter);
        addParameter(hueVariance);
        addModulator(lfo).trigger();
        addModulator(sinLfo).trigger();
        centerX = (model.xMax + model.xMin) / 2;
        centerY = (model.yMax + model.yMin) / 2;
        centerZ = (model.zMax + model.zMin) / 2;

        spheres = new Sphere[2];

        spheres[0] = new Sphere();
        spheres[0].x = model.xMin;
        spheres[0].y = centerY;
        spheres[0].z = centerZ;
        spheres[0].hue = palette.getHuef() - hueVariance.getValuef() / 2;
        spheres[0].radius = 50;

        spheres[1] = new Sphere();
        spheres[1].x = model.xMax;
        spheres[1].y = centerY;
        spheres[1].z = centerZ;
        spheres[1].hue = palette.getHuef() + hueVariance.getValuef() / 2;
        spheres[1].radius = 50;
    }

    public void run(double deltaMs, PolyBuffer.Space space) {
        int[] colors = (int[]) getArray(SRGB8);

        // Access the core master hue via this method call
        float hv = hueParameter.getValuef();
        float lfoValue = lfo.getValuef();
        float sinLfoValue = sinLfo.getValuef();

        spheres[0].hue = palette.getHuef() - hueVariance.getValuef() / 2;
        spheres[1].hue = palette.getHuef() + hueVariance.getValuef() / 2;

        spheres[0].x = model.xMin + sinLfoValue * model.xMax;
        spheres[1].x = model.xMax - sinLfoValue * model.xMax;

        spheres[0].radius = 100 * hueParameter.getValuef();
        spheres[1].radius = 100 * hueParameter.getValuef();

        getVectorList().parallelStream().forEach(p -> {
            float value = 0;

            int c = Ops8.BLACK;
            for (Sphere s : spheres) {
                float d = MathUtils.dist(p.x, p.y, p.z, s.x, s.y, s.z);
                float r = (s.radius); // * (sinLfoValue + 0.5));
                value = MathUtils.max(0, 1 - MathUtils.max(0, d - r) / 10);

                c = Ops8.add(c, LXColor.hsb(s.hue, 100, MathUtils.min(1, value) * 100));
            }
            colors[p.index] = c;
        });
        markModified(SRGB8);
    }
}
