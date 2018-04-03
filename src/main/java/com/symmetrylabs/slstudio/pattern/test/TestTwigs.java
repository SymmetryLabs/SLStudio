package com.symmetrylabs.slstudio.pattern.test;

import com.symmetrylabs.layouts.officeTenere.OfficeCornerBranchModel;
import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;

public class TestTwigs extends LXPattern implements SLTestPattern{
    private int num_Twigs = 16;
    public BooleanParameter[] b = new BooleanParameter[num_Twigs];


    protected void createParameters() {
        for (int i = 0; i < num_Twigs; i ++){
            addParameter(b[i] = new BooleanParameter(Integer.toString(i), true));
        }
    }

    public TestTwigs(LX lx) {
        super(lx);
        createParameters();
    }

    public void run(double deltaMs) {
        OfficeCornerBranchModel branchModel = (OfficeCornerBranchModel) lx.model;

        int twigIndex = 0;
        float hue = 0;
        int color;
        for (OfficeCornerBranchModel.LeafAssemblage assemblage : branchModel.assemblages ) {
            if (twigIndex == num_Twigs){
                break;
            }
            if (b[twigIndex++].isOn()){
                color = lx.hsb(hue, 100, 100);
            }
            else {
                color = LX.rgb(0,0,0);
            }

            for (LXPoint p : assemblage.points) {
                colors[p.index] = color;
            }
            hue += 90;
        }
    }
}
