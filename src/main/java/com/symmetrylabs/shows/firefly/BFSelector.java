package com.symmetrylabs.shows.firefly;

import art.lookingup.KaledoscopeModel;
import art.lookingup.LUButterfly;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.DiscreteParameter;

import java.util.logging.Logger;

public class BFSelector extends SLPattern {
    public static final String GROUP_NAME = FireflyShow.SHOW_NAME;
    private static final Logger logger = Logger.getLogger(BFSelector.class.getName());

    DiscreteParameter runNum = new DiscreteParameter("run", 0, 0, 100);
    BooleanParameter tracer = new BooleanParameter("tracer", false);
    DiscreteParameter index = new DiscreteParameter("index", -1, -1, 500);

    int currentIndex = 0;

    public BFSelector(LX lx) {
        super(lx);
        addParameter("run", runNum);
        addParameter("tracer", tracer);
        addParameter("index", index);
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
            for (int i = 0; i < run.butterflies.size(); i++) {
                if (currentIndex == i)
                    run.butterflies.get(i).setColor(colors, LXColor.rgb(255, 255, 255));
            }
            currentIndex = (currentIndex + 1) % run.allPoints.size();
        } else {
            int i = 0;
            for (LUButterfly butterfly : run.butterflies) {
                if (index.getValuei() == 0 && runNum.getValuei() == 1) {
                }
                if (index.getValuei() == i || index.getValuei() == -1)
                    butterfly.setColor(colors, LXColor.rgb(255, 255, 255));
                else
                    butterfly.setColor(colors, LXColor.rgb(0, 0, 0));
                i++;
            }
        }
    }
}
