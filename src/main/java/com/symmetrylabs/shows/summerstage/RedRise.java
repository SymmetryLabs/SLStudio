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
    public static final String GROUP_NAME = SummerStageShow.SHOW_NAME;

    private CompoundParameter hueParam = new CompoundParameter("Hue", 0, -1, 1);
    private CompoundParameter riseSpdParam = new CompoundParameter("RiseSpd", 10, 0, 100);
    private CompoundParameter splitSpdParam = new CompoundParameter("SplitSpd", 50, 0, 100);
    private BooleanParameter triggerParam = new BooleanParameter("Trigger", false).setMode(BooleanParameter.Mode.MOMENTARY);
    private CompoundParameter softEdgeParam = new CompoundParameter("SoftEdge", 50, 0, 200);

    private double timeSec = 0;
    private double riseStartSec = Double.MAX_VALUE;
    private double splitStartSec = Double.MAX_VALUE;

    public RedRise(LX lx) {
        super(lx);

        addParameter(hueParam);
        addParameter(riseSpdParam);
        addParameter(splitSpdParam);
        addParameter(triggerParam);
        addParameter(softEdgeParam);

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
        splitStartSec = Double.MAX_VALUE;
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
        float softEdge = softEdgeParam.getValuef();

        float duration;
        float elapsedSec;
        int[] colors = (int[]) getArray(Space.RGB8);

        if (timeSec > splitStartSec) {
            elapsedSec = (float) (timeSec - splitStartSec);
            duration = (60f / splitSpdParam.getValuef());
            if (elapsedSec > duration) {
                reset();
            } else {
                float radiusX = (model.xRange / 2) * elapsedSec / duration;
                for (LXPoint p : model.points) {
                    if (Math.abs(p.x - model.cx) < radiusX) {
                        float b = 1 - (radiusX - Math.abs(p.x - model.cx)) / softEdge;
                        if (b <= 0) colors[p.index] = Ops8.BLACK;
                        else {
                            int c = colors[p.index];
                            colors[p.index] = Ops8.rgba(Ops8.red(c), Ops8.green(c), Ops8.blue(c), (int) (b * 255 + 0.5));
                        }
                    }
                }
            }
        } else if (timeSec > riseStartSec) {
            elapsedSec = (float) (timeSec - riseStartSec);
            duration = (60f / riseSpdParam.getValuef());
            float topY = model.yMin + (model.yRange * elapsedSec / duration);
            for (LXPoint p : model.points) {
                if (p.y < topY) {
                    float b = (topY - p.y) / softEdge;
                    if (b > 1) b = 1;
                    colors[p.index] = LXColor.hsb(hueParam.getValuef(), 100, b * 100);
                }
            }
        }

        markModified(Space.RGB8);
    }
}
