package com.symmetrylabs.slstudio.pattern;

import java.util.List;
import java.util.ArrayList;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;

import com.symmetrylabs.slstudio.pattern.SLPattern;
import com.symmetrylabs.slstudio.model.CubesModel;


public class ManualTowerFlash extends SLPattern {

    public final CompoundParameter tower1 = new CompoundParameter("tow1", -1, 0, ((CubesModel)model).getTowers().size()-1);
    public final CompoundParameter tower1HueOffser = new CompoundParameter("tow1h", 0, 0, 180);
    public final CompoundParameter tower2 = new CompoundParameter("tow2", -1, 0, ((CubesModel)model).getTowers().size()-1);
    public final CompoundParameter tower2HueOffser = new CompoundParameter("tow2h", 0, 0, 180);


    public ManualTowerFlash(LX lx) {
        super(lx);
        addParameter(tower1);
        addParameter(tower2);
        addParameter(tower1HueOffser);
        addParameter(tower2HueOffser);
    }

    public void run(double deltaMs) {
        setColors(0);

        CubesModel.Tower t1 = null;
        CubesModel.Tower t2 = null;

        int t1Index = (int)tower1.getValuef();
        int t2Index = (int)tower2.getValuef();

        if (t1Index > 0) {
            t1 = ((CubesModel)model).getTowers().get(t1Index);
        }
        if (t2Index > 0) {
            t2 = ((CubesModel)model).getTowers().get(t2Index);
        }

        if (t1 != null) {
            for (LXPoint p : t1.points) {
                colors[p.index] = lx.hsb(tower1HueOffser.getValuef(), 100, 100);
            }
        }
        if (t2 != null) {
            for (LXPoint p : t2.points) {
                colors[p.index] = lx.hsb(tower2HueOffser.getValuef(), 100, 100);
            }
        }
    }

}