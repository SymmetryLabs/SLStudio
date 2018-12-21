package com.symmetrylabs.slstudio.pattern;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.PolyBuffer;
import heronarts.lx.color.LXColor;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.transform.LXProjection;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

import static heronarts.lx.PolyBuffer.Space.SRGB8;

public class Swim extends LXPattern {

    // Projection stuff
    private LXProjection projection;

    private final SinLFO rotationX = new SinLFO(-Math.PI / 16, Math.PI / 8, 9000);
    private final SinLFO rotationY = new SinLFO(-Math.PI / 8, Math.PI / 8, 7000);
    private final SinLFO rotationZ = new SinLFO(-Math.PI / 8, Math.PI / 16, 11000);
    private final SinLFO yPos = new SinLFO(-1, 1, 13234);
    private final SinLFO sineHeight = new SinLFO(1, 2.5, 13234);
    private final SawLFO phaseLFO = new SawLFO(0, 2 * Math.PI, 15000 - 13000 * 0.5);

    private final CompoundParameter crazyParam = new CompoundParameter("Crazy", 0.5);
    private final CompoundParameter hueScale = new CompoundParameter("HueVar", 0.1, 0.0, 0.2);
    private final CompoundParameter phaseParam = new CompoundParameter("Speed", 0.5);
    private final CompoundParameter hueParam = new CompoundParameter("Hue", 0, 0, 360);
    private final BooleanParameter monochromeParam = new BooleanParameter("Mono", false);

    public Swim(LX lx) {
        super(lx);
        onVectorsChanged();

        addParameter(hueScale);
        addParameter(crazyParam);
        addParameter(phaseParam);
        addParameter(hueParam);
        addParameter(monochromeParam);

        addModulator(rotationX).trigger();
        addModulator(rotationY).trigger();
        addModulator(rotationZ).trigger();
        addModulator(yPos).trigger();
        addModulator(phaseLFO).trigger();
    }

    public void onParameterChanged(LXParameter parameter) {
        if (parameter == phaseParam) {
            phaseLFO.setPeriod(10000 - 9000 * parameter.getValuef());
        }
    }

    public void onVectorsChanged() {
        super.onVectorsChanged();
        projection = new LXProjection(model, getVectorArray());
    }

    @Override
    public void run(double deltaMs, PolyBuffer.Space space) {
        int[] colors = (int[]) getArray(SRGB8);
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
        final float model_width = model.xMax - model.xMin;
        final boolean monochrome = monochromeParam.getValueb();
        final float monohue = hueParam.getValuef();

        StreamSupport.stream(
            Spliterators.spliteratorUnknownSize(projection.iterator(), Spliterator.CONCURRENT),
            true
        ).forEach(p -> {
            float x_percentage = (p.x - model.xMin) / model_width;

            // Multiply by sineHeight to shrink the size of the sin wave to be less than the height of the cubes.
            float y_in_range = sineHeight.getValuef() * (2 * p.y - model.yMax - model.yMin) / model_height;
            float sin_x = (float) Math.sin(phase + 2 * Math.PI * x_percentage);

            float size_of_sin_wave = 0.4f;

            float v1 = ((float) Math.abs(y_in_range - sin_x) > size_of_sin_wave)
                ? 0 : (float) Math.abs((y_in_range - sin_x + size_of_sin_wave)
                / size_of_sin_wave / 2 * 100);

            float hue_color = monochrome ? monohue :
                palette.getHuef() + hueScale.getValuef()
                * ((float) Math.abs(p.x - model.xMax / 2.) * .01f
                + (float) Math.abs(p.y - model.yMax / 2) * .6f
                + (float) Math.abs(p.z - model.zMax));

            colors[p.index] = LXColor.hsb(hue_color, palette.getSaturationf(), v1);
        });
        markModified(SRGB8);
    }
}
