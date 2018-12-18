package com.symmetrylabs.slstudio.pattern.instruments;

import com.symmetrylabs.color.Ops16;
import com.symmetrylabs.slstudio.pattern.instruments.RgbFluid.FloatRgb;

import java.util.ArrayList;
import java.util.List;

import heronarts.lx.PolyBuffer;
import heronarts.lx.model.LXModel;
import heronarts.lx.transform.LXVector;
import processing.core.PVector;

import static com.symmetrylabs.slstudio.pattern.instruments.EmitterInstrument.*;
import static heronarts.lx.PolyBuffer.Space.RGB16;

public class FluidEmitter extends AbstractEmitter implements Emitter {
    private LXModel model;
    private RgbFluid fluid;
    private float periodSec = (1 / 60f) / 10;  // 10 iterations per frame
    private static final float GRID_RESOLUTION = 10;

    public void initFluid(LXModel model) {
        if (model != this.model) {
            fluid = new RgbFluid(
                (int) Math.ceil(model.xRange / GRID_RESOLUTION),
                (int) Math.ceil(model.yRange / GRID_RESOLUTION)
            );
            fluid.setDiffusion(2);
            fluid.setRetention(0.8f);  // 20% decrease every second
            this.model = model;
        }
    }

    public void run(double deltaSec, ParameterSet paramSet) {
        float fluidSec = (float) (deltaSec * paramSet.getRate());
        if (fluid != null) {
            LXVector dir = paramSet.getDirection();
            fluid.setVelocity(new PVector(dir.x, dir.y, 0).mult(15));
            fluid.advance(fluidSec, periodSec);
        }
    }

    public void render(LXModel model, PolyBuffer buffer) {
        initFluid(model);
        long[] colors = (long[]) buffer.getArray(RGB16);
        for (int i = 0; i < model.points.length; i++) {
            float u = (model.points[i].x - model.xMin) / GRID_RESOLUTION;
            float v = (model.points[i].y - model.yMin) / GRID_RESOLUTION;
            int uLo = (int) Math.floor(u);
            int vLo = (int) Math.floor(v);
            FloatRgb sw = fluid.getCell(uLo, vLo);
            FloatRgb se = fluid.getCell(uLo + 1, vLo);
            FloatRgb nw = fluid.getCell(uLo, vLo + 1);
            FloatRgb ne = fluid.getCell(uLo + 1, vLo + 1);
            float r = lerp(lerp(sw.r, se.r, u - uLo), lerp(nw.r, ne.r, u - uLo), v - vLo);
            float g = lerp(lerp(sw.g, se.g, u - uLo), lerp(nw.g, ne.g, u - uLo), v - vLo);
            float b = lerp(lerp(sw.b, se.b, u - uLo), lerp(nw.b, ne.b, u - uLo), v - vLo);
            colors[i] = Ops16.rgba(
                (int) (r * Ops16.MAX),
                (int) (g * Ops16.MAX),
                (int) (b * Ops16.MAX),
                Ops16.MAX
            );
        }
        buffer.markModified(RGB16);
    }

    private float lerp(float start, float stop, float fraction) {
        return start + (stop - start) * fraction;
    }

    @Override
    public Jet emit(Instrument.ParameterSet paramSet, int pitch, double intensity) {
        if (model != null) {
            return new Jet(
                model,
                paramSet.getPosition(randomXyDisc()),
                paramSet.getSize(intensity),
                paramSet.getColor(randomVariation()),
                paramSet.getRate()
            );
        }
        return null;
    }

    class Jet implements Mark {
        public LXVector center;
        public double size;
        public long color;
        public double rate;

        private FloatRgb floatColor;
        private int u;
        private int v;
        private float addRate = 50;
        private boolean expired;

        public Jet(LXModel model, LXVector center, double size, long color, double rate) {
            this.center = center;
            this.size = size;
            this.color = color;
            floatColor = new FloatRgb(
                (float) Ops16.red(color) / Ops16.MAX,
                (float) Ops16.green(color) / Ops16.MAX,
                (float) Ops16.blue(color) / Ops16.MAX
            );
            this.rate = rate;

            u = (int) Math.floor((center.x - model.xMin) / GRID_RESOLUTION);
            v = (int) Math.floor((center.y - model.yMin) / GRID_RESOLUTION);
        }

        public void advance(double deltaSec, double intensity, boolean sustain) {
            float fluidSec = (float) (deltaSec * rate);
            if (sustain) {
                fluid.addCell(u, v, new FloatRgb(floatColor).scale(
                    (float) (intensity * addRate * fluidSec)));
            } else {
                expired = true;
            }
        }

        public boolean isExpired() {
            return expired;
        }

        public void render(LXModel model, PolyBuffer buffer) { }
    }
}
