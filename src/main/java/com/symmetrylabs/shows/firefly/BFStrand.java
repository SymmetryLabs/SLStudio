package com.symmetrylabs.shows.firefly;

import art.lookingup.KaledoscopeModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.DiscreteParameter;

public class BFStrand extends SLPattern {
    public static final String GROUP_NAME = FireflyShow.SHOW_NAME;

    DiscreteParameter strandNum = new DiscreteParameter("strand", 0, 0, 100);
    BooleanParameter tracer = new BooleanParameter("tracer", false);
    public int currentIndex;

    public BFStrand(LX lx) {
        super(lx);
        addParameter("strand", strandNum);
        addParameter("tracer", tracer);
    }

    @Override
    public void onActive() {
        strandNum.setRange(0, KaledoscopeModel.allStrands.size());
    }

    @Override
    protected void run(double deltaMs) {
        for (LXPoint p : model.points) {
            colors[p.index] = LXColor.rgb(0, 0, 0);
        }
        KaledoscopeModel.Strand strand = KaledoscopeModel.allStrands.get(strandNum.getValuei());
        if (tracer.getValueb()) {
            for (int i = 0; i < strand.allPoints.size(); i++) {
                if (currentIndex == i)
                    colors[strand.allPoints.get(i).index] = LXColor.rgb(255, 255, 255);
            }
            currentIndex = (currentIndex + 1) % strand.allPoints.size();
        } else {
            for (LXPoint p : strand.allPoints) {
                colors[p.index] = LXColor.rgb(255, 255, 255);
            }
        }
    }
}
