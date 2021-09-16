package com.symmetrylabs.shows.firefly;

import art.lookingup.KaledoscopeModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.DiscreteParameter;

public class BFRun extends SLPattern {
    public static final String GROUP_NAME = FireflyShow.SHOW_NAME;

    DiscreteParameter runNum = new DiscreteParameter("run", 0, 0, 100);
    BooleanParameter tracer = new BooleanParameter("tracer", false);

    int currentIndex = 0;

    public BFRun(LX lx) {
        super(lx);
        addParameter("run", runNum);
        addParameter("tracer", tracer);
    }

    @Override
    public void onActive() {
        runNum.setRange(0, KaledoscopeModel.allRuns.size());
    }

    @Override
    protected void run(double deltaMs) {
        for (LXPoint p : model.points) {
            colors[p.index] = LXColor.rgb(0, 0, 0);
        }
        KaledoscopeModel.Run run = KaledoscopeModel.allRuns.get(runNum.getValuei());
        if (tracer.getValueb()) {
            for (int i = 0; i < run.allPoints.size(); i++) {
                if (currentIndex == i)
                    colors[run.allPoints.get(i).index] = LXColor.rgb(255, 255, 255);
            }
            currentIndex = (currentIndex + 1) % run.allPoints.size();
        } else {
            for (LXPoint p : run.allPoints) {
                colors[p.index] = LXColor.rgb(255, 255, 255);
            }
        }
    }
}
