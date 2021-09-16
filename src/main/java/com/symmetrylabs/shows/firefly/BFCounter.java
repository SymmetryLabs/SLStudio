package com.symmetrylabs.shows.firefly;

import art.lookingup.KaledoscopeModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.DiscreteParameter;

public class BFCounter extends SLPattern {
    public static final String GROUP_NAME = FireflyShow.SHOW_NAME;

    DiscreteParameter strandNum = new DiscreteParameter("strand", -1, 0, 100);
    DiscreteParameter bfIndex = new DiscreteParameter("butterfly", -1, 30);
    public BFCounter(LX lx) {
        super(lx);
        addParameter("strand", strandNum);
        addParameter("butterfly", bfIndex);
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
        if (strand.strandType == KaledoscopeModel.Strand.StrandType.BUTTERFLY) {
            if (bfIndex.getValuei() == -1) {
                for (int i = 0; i < strand.butterflies.size(); i++) {
                    for (LXPoint p : strand.butterflies.get(i).allPoints) {
                        if ((i + 1) % 10 == 0) {
                            colors[p.index] = LXColor.rgb(255, 0, 0);
                        } else if ((i + 1) % 5 == 0) {
                            colors[p.index] = LXColor.rgb(0, 255, 0);
                        } else {
                            colors[p.index] = LXColor.rgb(255, 255, 255);
                        }
                    }
                }
            } else {
                if (bfIndex.getValuei() < strand.butterflies.size()) {
                    for (LXPoint p : strand.butterflies.get(bfIndex.getValuei()).allPoints) {
                        colors[p.index] = LXColor.rgb(255, 255, 255);
                    }
                }
            }
        }
    }
}
