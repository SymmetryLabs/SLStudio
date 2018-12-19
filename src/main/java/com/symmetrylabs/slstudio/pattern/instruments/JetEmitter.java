package com.symmetrylabs.slstudio.pattern.instruments;

import com.symmetrylabs.color.Ops16;

import heronarts.lx.PolyBuffer;
import heronarts.lx.model.LXModel;
import heronarts.lx.transform.LXVector;
import processing.core.PVector;

import static com.symmetrylabs.slstudio.pattern.instruments.Instrument.ParameterSet;
import static heronarts.lx.PolyBuffer.Space.RGB16;

public class JetEmitter implements Emitter {
    private LXModel model;
    private Fluid[] fluids;
    private float periodSec = (1 / 60f) / 5;  // 5 iterations per frame
    private static final float GRID_RESOLUTION = 10;

    @Override
    public Jet emit(Instrument.ParameterSet paramSet, int pitch, double intensity) {
        if (model != null) {
            float rate = (float) paramSet.getRate();
            float speedUp = rate < 1 ? 1 : rate;
            return new Jet(
                model,
                new LXVector(paramSet.getPoint(pitch, MarkUtils.randomXyDisc())),
                paramSet.getSize(intensity),
                paramSet.getColor(MarkUtils.randomVariation()),
                speedUp
            );
        }
        return null;
    }

    public void initFluid(LXModel model) {
        if (model != this.model) {
            this.model = model;
            fluids = new Fluid[] {
                setupFluid(), setupFluid(), setupFluid()
            };
        }
    }

    private Fluid setupFluid() {
        int width = (int) Math.ceil(model.xRange / GRID_RESOLUTION);
        int height = (int) Math.ceil(model.yRange / GRID_RESOLUTION);
        Fluid fluid = new Fluid(width, height);
        fluid.setDiffusion(10);
        fluid.setRetention((float) Math.pow(0.01, 1/0.5));  // 99% decrease in 0.5 seconds
        return fluid;
    }

    public void run(double deltaSec, ParameterSet paramSet) {
        if (deltaSec > 0.2) deltaSec = 0.2;
        float rate = (float) paramSet.getRate();
        float speedUp = rate < 1 ? 1 : rate;
        float speed = rate < 1 ? 50 * rate : 50;
        float fluidSec = (float) (deltaSec * speedUp);
        LXVector dir = paramSet.getDirection();
        PVector velocity = new PVector(dir.x, dir.y, 0).mult(speed);
        if (fluids != null) {
            for (Fluid fluid : fluids) {
                if (fluid != null) {
                    fluid.setDiffusion((float) paramSet.getSize(0) / GRID_RESOLUTION);
                    fluid.setRetention((float) Math.pow(0.01, 1/(paramSet.getDecaySec() + 0.001)));
                    fluid.setVelocity(velocity);
                    fluid.advance(fluidSec, periodSec);
                }
            }
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
            float r = lerpCells(fluids[0], uLo, u - uLo, vLo, v - vLo);
            float g = lerpCells(fluids[1], uLo, u - uLo, vLo, v - vLo);
            float b = lerpCells(fluids[2], uLo, u - uLo, vLo, v - vLo);
            colors[i] = Ops16.rgba(
                (int) (r * Ops16.MAX),
                (int) (g * Ops16.MAX),
                (int) (b * Ops16.MAX),
                Ops16.MAX
            );
        }
        buffer.markModified(RGB16);
    }

    private float lerpCells(Fluid fluid, int uLo, float uFrac, int vLo, float vFrac) {
        return lerp(
            lerp(fluid.getCell(uLo, vLo), fluid.getCell(uLo + 1, vLo), uFrac),
            lerp(fluid.getCell(uLo, vLo + 1), fluid.getCell(uLo + 1, vLo + 1), uFrac),
            vFrac
        );
    }

    private float lerp(float start, float stop, float fraction) {
        return start + (stop - start) * fraction;
    }

    class Jet implements Mark {
        public LXVector center;
        public double size;
        public long color;
        public double speedUp;

        private float r;
        private float g;
        private float b;
        private int u;
        private int v;
        private float addRate = 2000;
        private boolean expired;

        public Jet(LXModel model, LXVector center, double size, long color, double speedUp) {
            this.center = center;
            this.size = size;
            this.color = color;
            r = (float) Ops16.red(color) / Ops16.MAX;
            g = (float) Ops16.green(color) / Ops16.MAX;
            b = (float) Ops16.blue(color) / Ops16.MAX;
            this.speedUp = speedUp;

            u = (int) Math.floor((center.x - model.xMin) / GRID_RESOLUTION);
            v = (int) Math.floor((center.y - model.yMin) / GRID_RESOLUTION);
        }

        public void advance(double deltaSec, double intensity, boolean sustain) {
            float fluidSec = (float) (deltaSec * speedUp);
            if (sustain) {
                float addQuantity = (float) (intensity * intensity * addRate * fluidSec);
                fluids[0].addCell(u, v, r * addQuantity);
                fluids[1].addCell(u, v, g * addQuantity);
                fluids[2].addCell(u, v, b * addQuantity);
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
