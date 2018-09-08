package com.symmetrylabs.shows.summerstage;

import com.symmetrylabs.color.Ops8;
import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;

import java.util.Arrays;

import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import heronarts.lx.PolyBuffer.Space;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;

public class RedRise extends SLPattern<StripsModel<? extends Strip>> {
    private CompoundParameter hueParam = new CompoundParameter("Hue", 0, -1, 1).setDescription("Hue adjustment");
    private CompoundParameter riseSpdParam = new CompoundParameter("RiseSpd", 10, 0, 100).setDescription("Infection growth speed (strip lengths per minute)");
    private CompoundParameter splitSpdParam = new CompoundParameter("SplitSpd", 50, 0, 100).setDescription("Infection growth speed (strip lengths per minute)");
    private BooleanParameter triggerParam = new BooleanParameter("Trigger", false).setMode(BooleanParameter.Mode.MOMENTARY);

    private double timeSec = 0;
    private double riseStartSec = Double.MAX_VALUE;
    private double splitStartSec = Double.MAX_VALUE;

    public RedRise(LX lx) {
        super(lx);

        addParameter(hueParam);
        addParameter(riseSpdParam);
        addParameter(splitSpdParam);
        addParameter(triggerParam);

        reset();
    }

    protected void reset() {
        int[] colors = (int[]) getArray(Space.RGB8);
        Arrays.fill(colors, Ops8.BLACK);
        markModified(Space.RGB8);

        riseStartSec = Double.MAX_VALUE;
        splitStartSec = Double.MAX_VALUE;
    }

    @Override
    public void onParameterChanged(LXParameter p) {
        if (p instanceof BooleanParameter) {
            BooleanParameter param = (BooleanParameter) p;
            if (param == triggerParam) {
                if (param.getValueb()) startRise();
                else startSplit();
            }
        }
    }

    protected void startRise() {
        riseStartSec = timeSec;
    }

    protected void startSplit() {
        splitStartSec = timeSec;
        riseStartSec = Double.MAX_VALUE;
    }

    @Override
    public void run(double deltaMs, PolyBuffer.Space preferredSpace) {
        double deltaSec = deltaMs/1000.0;
        timeSec += deltaSec;

        double duration;
        double elapsedSec;
        int[] colors = (int[]) getArray(Space.RGB8);

        if (timeSec > splitStartSec) {
            elapsedSec = timeSec - splitStartSec;
            duration = (60.0 / splitSpdParam.getValue());
            if (elapsedSec > duration) {
                reset();
            } else {
                double radiusX = (model.xRange / 2) * elapsedSec / duration;
                for (LXPoint p : model.points) {
                    if (Math.abs(p.x - model.cx) < radiusX) colors[p.index] = Ops8.BLACK;
                }
            }
        } else if (timeSec > riseStartSec) {
            elapsedSec = timeSec - riseStartSec;
            duration = (60.0 / riseSpdParam.getValue());
            double topY = model.yMin + (model.yRange * elapsedSec / duration);
            for (LXPoint p : model.points) {
                if (p.y < topY) colors[p.index] = LXColor.hsb(hueParam.getValuef(), 100, 100);
            }
        }

        markModified(Space.RGB8);
    }
}
