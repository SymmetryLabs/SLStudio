package com.symmetrylabs.slstudio.pattern;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.LXUtils;
import heronarts.lx.color.LXColor;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.transform.LXProjection;
import heronarts.lx.transform.LXVector;

public class Balance extends LXPattern {



    class Sphere {
        float x, y, z;
    }

    // Projection stuff
    private final LXProjection projection;

    SinLFO sphere1Z = new SinLFO(0, 0, 15323);
    SinLFO sphere2Z = new SinLFO(0, 0, 8323);
    SinLFO rotationX = new SinLFO(-Math.PI / 32, Math.PI / 32, 9000);
    SinLFO rotationY = new SinLFO(-Math.PI / 16, Math.PI / 16, 7000);
    SinLFO rotationZ = new SinLFO(-Math.PI / 16, Math.PI / 16, 11000);
    SawLFO phaseLFO = new SawLFO(0, 2 * Math.PI, 5000 - 4500 * 0.5f);
    
    
        final CompoundParameter crazyParam = new CompoundParameter("Crazy", 0.2f);
        final CompoundParameter hueScale = new CompoundParameter("Hue", 0.4);
        final CompoundParameter phaseParam = new CompoundParameter("Speed", 0.5f);
    private final Sphere[] spheres;
    private final float centerX, centerY, centerZ, modelHeight, modelWidth, modelDepth;
    SinLFO heightMod = new SinLFO(0.8, 1.9, 17298);

    public Balance(LX lx) {
        super(lx);

        projection = new LXProjection(model);

        addParameter(hueScale);
        addParameter(phaseParam);
        addParameter(crazyParam);

        spheres = new Sphere[2];
        centerX = (model.xMax + model.xMin) / 2;
        centerY = (model.yMax + model.yMin) / 2;
        centerZ = (model.zMax + model.zMin) / 2;
        modelHeight = model.yMax - model.yMin;
        modelWidth = model.xMax - model.xMin;
        modelDepth = model.zMax - model.zMin;

        spheres[0] = new Sphere();
        spheres[0].x = 1 * modelWidth / 2 + model.xMin;
        spheres[0].y = centerY + 20;
        spheres[0].z = centerZ;

        spheres[1] = new Sphere();
        spheres[1].x = model.xMin;
        spheres[1].y = centerY - 20;
        spheres[1].z = centerZ;

        addModulator(rotationX).trigger();
        addModulator(rotationY).trigger();
        addModulator(rotationZ).trigger();


        addModulator(sphere1Z).trigger();
        addModulator(sphere2Z).trigger();
        addModulator(phaseLFO).trigger();

        addModulator(heightMod).trigger();
    }

    public void onParameterChanged(LXParameter parameter) {
        if (parameter == phaseParam) {
            phaseLFO.setPeriod(5000 - 4500 * parameter.getValuef());
        }
    }

    int beat = 0;
    float prevRamp = 0;

    @Override
    public void run(double deltaMs) {

        // Sync to the beat
        float ramp = (float) lx.tempo.ramp();
        if (ramp < prevRamp) {
            beat = (beat + 1) % 4;
        }
        prevRamp = ramp;
        float phase = phaseLFO.getValuef();

        float crazy_factor = crazyParam.getValuef() / 0.2f;
        projection.reset()
            .rotate(rotationZ.getValuef() * crazy_factor, 0, 1, 0)
            .rotate(rotationX.getValuef() * crazy_factor, 0, 0, 1)
            .rotate(rotationY.getValuef() * crazy_factor, 0, 1, 0);

        StreamSupport.stream(
            Spliterators.spliteratorUnknownSize(projection.iterator(), Spliterator.CONCURRENT),
            true
        ).forEach(p -> {
            float x_percentage = (p.x - model.xMin) / modelWidth;

            float y_in_range = heightMod.getValuef() * (2 * p.y - model.yMax - model.yMin) / modelHeight;
            float sin_x = (float) Math.sin(Math.PI / 2 + phase + 2 * Math.PI * x_percentage);

            // Color fade near the top of the sin wave
            float v1 = Math.max(0, 100 * (1 - 4 * (float) Math.abs(sin_x - y_in_range)));

            float hue_color =
                palette.getHuef() + hueScale.getValuef() * ((float) Math.abs(p.x - model.xMax / 2f) + (float) Math.abs(p.y - model.yMax / 2f) * .2f + (float) Math
                    .abs(p.z - model.zMax / 2f) * .5f);
            int c = lx.hsb(hue_color, 80, v1);

            // Now draw the spheres
            for (Sphere s : spheres) {
                float phase_x = (float) ((s.x - phase / (2 * Math.PI) * modelWidth) % modelWidth);
                float x_dist = LXUtils.wrapdistf(p.x, phase_x, modelWidth);

                float sphere_z = (s == spheres[0]) ? (s.z + sphere1Z.getValuef()) : (s.z - sphere2Z.getValuef());


                float d = (float) Math.sqrt((float) Math.pow(x_dist, 2) + (float) Math.pow(
                    p.y - s.y,
                    2
                ) + (float) Math.pow(p.z - sphere_z, 2));

                float distance_from_beat = (beat % 2 == 1) ? 1 - ramp : ramp;

                float r = 40 - (float) Math.pow(distance_from_beat, 0.75) * 20;

                float distance_value = Math.max(0, 1 - Math.max(0, d - r) / 10);
                float beat_value = 1;

                float value = Math.min(beat_value, distance_value);

                float sphere_color = palette.getHuef() - (1 - hueScale.getValuef()) * d / r * 45;

                c = LXColor.blend(c, lx.hsb(sphere_color + 270, 60, Math.min(1, value) * 100), LXColor.Blend.ADD);
            }

            colors[p.index] = c;
        });
    }
}
