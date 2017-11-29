package com.symmetrylabs.pattern;

import java.util.stream.StreamSupport;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.*;
import heronarts.lx.modulator.*;
import heronarts.lx.transform.*;

public class Swim extends LXPattern {

    // Projection stuff
    private final LXProjection projection;

    SinLFO rotationX = new SinLFO(-Math.PI/16, Math.PI/8, 9000);
    SinLFO rotationY = new SinLFO(-Math.PI/8, Math.PI/8, 7000);
    SinLFO rotationZ = new SinLFO(-Math.PI/8, Math.PI/16, 11000);
    SinLFO yPos = new SinLFO(-1, 1, 13234);
    SinLFO sineHeight = new SinLFO(1, 2.5, 13234);
    SawLFO phaseLFO = new SawLFO(0, 2 * Math.PI, 15000 - 13000 * 0.5);

    final CompoundParameter phaseParam = new CompoundParameter("Spd", 0.5);
    final CompoundParameter crazyParam = new CompoundParameter("Crzy", 0.5);

    final CompoundParameter hueScale = new CompoundParameter("HUE", 0.1, 0.0, 0.2);

    public Swim(LX lx) {
        super(lx);

        projection = new LXProjection(model);

        addParameter(hueScale);
        addParameter(crazyParam);
        addParameter(phaseParam);

        addModulator(rotationX).trigger();
        addModulator(rotationY).trigger();
        addModulator(rotationZ).trigger();
        addModulator(yPos).trigger();
        addModulator(phaseLFO).trigger();
    }

    public void onParameterChanged(LXParameter parameter) {
        if (parameter == phaseParam) {
            phaseLFO.setPeriod(5000 - 4500 * parameter.getValuef());
        }
    }

    @Override
    public void run(final double deltaMs) {

        final float phase = phaseLFO.getValuef();
        final float upDownRange = (model.yMax - model.yMin) / 4;

        // Swim around the world
        float crazyFactor = crazyParam.getValuef() / 0.2f;
        projection.reset()
            .rotate(rotationZ.getValuef() * crazyFactor, 0, 1, 0)
            .rotate(rotationX.getValuef() * crazyFactor, 0, 0, 1)
            .rotate(rotationY.getValuef() * crazyFactor, 0, 1, 0)
            .translate(0, upDownRange * yPos.getValuef(), 0);

        final float model_height = model.yMax - model.yMin;
        final float model_width  = model.xMax - model.xMin;

        StreamSupport.stream(
            Spliterators.spliteratorUnknownSize(projection.iterator(), Spliterator.CONCURRENT),
            true
        ).forEach(new Consumer<LXVector>() {
            @Override
            public void accept(final LXVector p) {
                float x_percentage = (p.x - model.xMin)/model_width;

                // Multiply by sineHeight to shrink the size of the sin wave to be less than the height of the cubes.
                float y_in_range = sineHeight.getValuef() * (2*p.y - model.yMax - model.yMin) / model_height;
                float sin_x = (float)Math.sin(phase + 2 * Math.PI * x_percentage);

                float size_of_sin_wave = 0.4f;

                float v1 = ((float)Math.abs(y_in_range - sin_x) > size_of_sin_wave)
                                ? 0 : (float)Math.abs((y_in_range - sin_x + size_of_sin_wave)
                                                                                / size_of_sin_wave / 2 * 100);

                float hue_color = palette.getHuef() + hueScale.getValuef()
                                * ((float)Math.abs(p.x-model.xMax/2.) * .01f
                                        + (float)Math.abs(p.y-model.yMax / 2) * .6f
                                        + (float)Math.abs(p.z - model.zMax));

                colors[p.index] = lx.hsb(hue_color, 100, v1);
            }
        });
    }
}
