package com.symmetrylabs.slstudio.pattern;

import com.symmetrylabs.layouts.cubes.CubesModel;
import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.audio.GraphicMeter;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;

public class SoundHistogram extends SLPattern<CubesModel> {

    //private GraphicEQ eq = null;

    CompoundParameter hue = new CompoundParameter("hue", 0,0,360);
    CompoundParameter sprd = new CompoundParameter("sprd", 1,1,360);
    CompoundParameter rng = new CompoundParameter("rng", 1,1,20);
    public GraphicMeter eq = null;

    public SoundHistogram(LX lx) {
        super(lx);
        //eq = new GraphicEQ(lx.audioInput());
        eq = new GraphicMeter(lx.engine.audio.getInput());
        eq.range.setValue(48);
        eq.release.setValue(800);
        addModulator(eq).start();
//        addParameter(eq.gain);
//        addParameter(hue);
//        addParameter(sprd);
//        addParameter(rng);

    }

    public void run(double deltaMs) {
        int j=0;
        for (Strip s : model.getStrips())
        {
            int i=0;
            double val = eq.getBandf(j % 18)*18;
            for (LXPoint p : s.points)
            {
                val = i < val ? val : 0;
                colors[p.index] = LXColor.hsb(hue.getValuef()+(val % rng.getValuef())*sprd.getValuef(),val*100 < 100 ? val*100 : 100, val*10 < 100 ? val*10 : 10);
                i++;
            }
            j++;
        }
    }

}
