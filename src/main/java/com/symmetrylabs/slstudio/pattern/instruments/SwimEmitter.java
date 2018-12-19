package com.symmetrylabs.slstudio.pattern.instruments;

import com.symmetrylabs.color.Ops16;

import java.util.Arrays;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

import heronarts.lx.PolyBuffer;
import heronarts.lx.model.LXModel;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.transform.LXProjection;

import static heronarts.lx.PolyBuffer.Space.RGB16;

public class SwimEmitter implements Emitter {
    @Override
    public Swimmer emit(Instrument.ParameterSet paramSet, int pitch, double intensity) {
        return new Swimmer(
            paramSet.getHue(),
            paramSet.getHueVar(),
            paramSet.getSat(),
            paramSet.getRate(),
            paramSet.getOrient(),
            paramSet.getPitchFraction(pitch),
            0.1 / (0.005 + intensity * intensity),  // intensity = 0, 0.5, 1 -> attack = 20, 0.4, 0.1
            paramSet.getDecaySec()
        );
    }

    class Swimmer extends AttackDecayMark {
        public double hue;
        public double hueVar;
        public double sat;
        public double rate;
        public double crazy;

        private LXProjection projection = null;

        private final SinLFO rotationX = new SinLFO(-Math.PI / 16, Math.PI / 8, 9000);
        private final SinLFO rotationY = new SinLFO(-Math.PI / 8, Math.PI / 8, 7000);
        private final SinLFO rotationZ = new SinLFO(-Math.PI / 8, Math.PI / 16, 11000);
        private final SinLFO yPos = new SinLFO(-1, 1, 13234);
        private final SinLFO sineHeight = new SinLFO(1, 2.5, 13234);
        private final SawLFO phaseLFO = new SawLFO(0, 2 * Math.PI, 15000 - 13000 * 0.5);

        public Swimmer(double hue, double hueVar, double sat, double rate, double crazy, double phase, double attackSec, double decaySec) {
            super(attackSec, decaySec);

            this.hue = hue;
            this.hueVar = hueVar;
            this.sat = sat;
            this.rate = rate;
            this.crazy = crazy;

            addModulator(rotationX).trigger();
            addModulator(rotationY).trigger();
            addModulator(rotationZ).trigger();
            addModulator(yPos).trigger();
            addModulator(phaseLFO).trigger();
            phaseLFO.setPeriod(5000 - 4500 * rate);
            phaseLFO.setValue(phase * 2 * Math.PI);
        }

        public void render(LXModel model, PolyBuffer buffer) {
            if (projection == null) {
                projection = new LXProjection(model);
            }

            long[] colors = (long[]) buffer.getArray(RGB16);

            final float phase = phaseLFO.getValuef();
            final float upDownRange = (model.yMax - model.yMin) / 4;

            // Swim around the world
            float crazyFactor = (float) crazy / 0.2f;
            projection.reset()
                .rotate(rotationZ.getValuef() * crazyFactor, 0, 1, 0)
                .rotate(rotationX.getValuef() * crazyFactor, 0, 0, 1)
                .rotate(rotationY.getValuef() * crazyFactor, 0, 1, 0)
                .translate(0, upDownRange * yPos.getValuef(), 0);

            final float model_height = model.yMax - model.yMin;
            final float model_width = model.xMax - model.xMin;

            StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(projection.iterator(), Spliterator.CONCURRENT),
                true
            ).forEach(p -> {
                float x_percentage = (p.x - model.xMin) / model_width;

                // Multiply by sineHeight to shrink the size of the sin wave to be less than the height of the cubes.
                float y_in_range = sineHeight.getValuef() * (2 * p.y - model.yMax - model.yMin) / model_height;
                float sin_x = (float) Math.sin(phase + 2 * Math.PI * x_percentage);

                float size_of_sin_wave = 0.4f;

                float v1 = (Math.abs(y_in_range - sin_x) > size_of_sin_wave)
                    ? 0 : Math.abs((y_in_range - sin_x + size_of_sin_wave) / size_of_sin_wave / 2 * 100);

                float pointHue = (float) hue * 360 +
                    (float) hueVar * (
                        (float) Math.abs(p.x - model.xMax / 2.) * .01f
                            + Math.abs(p.y - model.yMax / 2) * .6f
                            + Math.abs(p.z - model.zMax)
                    );
                long c = Ops16.hsb(pointHue / 360, sat, (v1/100) * amplitude);
                colors[p.index] = Ops16.add(colors[p.index], c);
            });

            buffer.markModified(RGB16);
        }
    }
}
