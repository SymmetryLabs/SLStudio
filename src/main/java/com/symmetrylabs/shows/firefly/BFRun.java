package com.symmetrylabs.shows.firefly;

import art.lookingup.KaledoscopeModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.DiscreteParameter;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class BFRun extends SLPattern {
    public static final String GROUP_NAME = FireflyShow.SHOW_NAME;
    private static final Logger logger = Logger.getLogger(BFRun.class.getName());

    DiscreteParameter runNum = new DiscreteParameter("run", -1, -1, 100);
    BooleanParameter tracer = new BooleanParameter("tracer", false);
    DiscreteParameter index = new DiscreteParameter("index", -1, -1, 500);

    int currentIndex = 0;

    public BFRun(LX lx) {
        super(lx);
        addParameter("run", runNum);
        addParameter("tracer", tracer);
        addParameter("index", index);
    }

    @Override
    public void onActive() {
        runNum.setRange(-1, KaledoscopeModel.allRuns.size());
    }

    @Override
    protected void run(double deltaMs) {
        for (LXPoint p : model.points) {
            colors[p.index] = LXColor.rgb(0, 0, 0);
        }
        List<KaledoscopeModel.Run> runs = new ArrayList<KaledoscopeModel.Run>();
        runs.addAll(KaledoscopeModel.allRuns);
        if (runNum.getValuei() != -1) {
            runs.clear();
            runs.add(KaledoscopeModel.allRuns.get(runNum.getValuei()));
        }
        for (KaledoscopeModel.Run run : runs) {
            if (tracer.getValueb()) {
                for (int i = 0; i < run.allPoints.size(); i++) {
                    if (currentIndex == i)
                        colors[run.allPoints.get(i).index] = LXColor.rgb(255, 255, 255);
                }
                currentIndex = (currentIndex + 1) % run.allPoints.size();
            } else {
                int i = 0;
                for (LXPoint p : run.allPoints) {
                    if (index.getValuei() == i || index.getValuei() == -1)
                        colors[p.index] = LXColor.rgb(255, 255, 255);
                    else
                        colors[p.index] = LXColor.rgb(0, 0, 0);
                    i++;
                }
            }
        }
    }
}
