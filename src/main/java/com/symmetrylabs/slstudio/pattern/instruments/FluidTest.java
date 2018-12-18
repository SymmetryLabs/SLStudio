package com.symmetrylabs.slstudio.pattern.instruments;

import com.symmetrylabs.color.Ops8;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import com.symmetrylabs.util.CubeMarker;
import com.symmetrylabs.util.Marker;
import com.symmetrylabs.util.Matrix;
import com.symmetrylabs.util.MatrixMarker;

import java.util.Arrays;
import java.util.List;

import heronarts.lx.LX;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import processing.core.PVector;

public class FluidTest extends SLPattern<SLModel> {
    private CompoundParameter addParam = new CompoundParameter("add", 0, -4, 4);
    private CompoundParameter diffParam = new CompoundParameter("diff", 0, -4, 4);
    private CompoundParameter retentParam = new CompoundParameter("retent", 1, 0, 1);
    private CompoundParameter vxParam = new CompoundParameter("vx", 0, -50, 50);
    private CompoundParameter vyParam = new CompoundParameter("vy", 0, -50, 50);
    private CompoundParameter brtParam = new CompoundParameter("brt", 0, -4, 4);
    private CompoundParameter itersParam = new CompoundParameter("iters", 10, 0, 100);
    private CompoundParameter speedParam = new CompoundParameter("speed", 0, -1, 1);
    private BooleanParameter resetParam = new BooleanParameter("reset")
        .setMode(BooleanParameter.Mode.MOMENTARY);

    private Fluid fluid;

    public FluidTest(LX lx) {
        super(lx);
        reset();

        addParameter(addParam);
        addParameter(diffParam);
        addParameter(retentParam);
        addParameter(vxParam);
        addParameter(vyParam);
        addParameter(brtParam);
        addParameter(itersParam);
        addParameter(speedParam);
        addParameter(resetParam);

        diffParam.addListener(param -> updateParams());
        retentParam.addListener(param -> updateParams());
        vxParam.addListener(param -> updateParams());
        vyParam.addListener(param -> updateParams());
        resetParam.addListener(param -> {
            if (resetParam.isOn()) reset();
        });
    }

    public void reset() {
        fluid = new Fluid(100, 11);
        updateParams();
    }

    public void updateParams() {
        fluid.setDiffusion(getDiffusion());
        fluid.setRetention(getRetention());
        fluid.setVelocity(getVelocity());
    }

    private float getAddRate() {
        return (float) Math.pow(10, addParam.getValuef());
    }

    private float getDiffusion() {
        return (float) Math.pow(10, diffParam.getValuef());
    }

    private float getRetention() {
        return retentParam.getValuef();
    }

    private PVector getVelocity() {
        return new PVector(vxParam.getValuef(), vyParam.getValuef(), 0);
    }

    private float getSpeedup() {
        return (float) Math.pow(10, speedParam.getValuef());
    }

    public void run(double deltaMs) {
        float deltaSec = (float) deltaMs/1000 * getSpeedup();
        fluid.addCell(5, 5, getAddRate() * deltaSec);
        fluid.advance(deltaSec, (1 / 60f) / itersParam.getValuef());
    }

    public List<Marker> getMarkers() {
        float scale = 10;
        PVector center = new PVector(fluid.width * scale / 2, fluid.height * scale / 2, 0);
        return Arrays.asList(
            new MatrixMarker(new PVector(0, 0, 0), scale, new FluidMatrix()),
            new CubeMarker(center, center, 0xff00c0c0)
        );
    }

    public String getCaption() {
        return String.format("iter %d: total = %.4f, add = %.4f, diff = %.4f, ret = %.4f, vel = (%.4f, %.4f), speedup = %.2f",
            fluid.getIterations(), fluid.getLastTotal(), getAddRate(), getDiffusion(), getRetention(), getVelocity().x, getVelocity().y, getSpeedup());
    }

    class FluidMatrix implements Matrix {
        public int getWidth() {
            return fluid.width;
        }

        public int getHeight() {
            return fluid.height;
        }

        public int getColor(int x, int y) {
            double value = fluid.getCell(x, y) * Math.pow(10, brtParam.getValue());
            value = Math.max(0, Math.min(1, value));
            return Ops8.hsb(0, 0, value);
        }
    }
}
