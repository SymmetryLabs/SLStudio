package com.symmetrylabs.slstudio.pattern.tree;

import static processing.core.PApplet.sq;

import com.symmetrylabs.layouts.oslo.TreeModel;
import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;

import static com.symmetrylabs.util.DistanceConstants.FEET;
import static com.symmetrylabs.util.MathUtils.max;
import static com.symmetrylabs.util.MathUtils.min;
import static com.symmetrylabs.util.MathUtils.sqrt;

public class TreeSphericalWave extends LXPattern {

    public String getAuthor() {
        return "Aimone";
    }
    hist inputHist;

    public final CompoundParameter input =
        new CompoundParameter("input", 0, 1)
            .setDescription("Input (0-1)");

    public final CompoundParameter yPos =
        new CompoundParameter("yPos", model.cy, model.yMin, model.yMax)
            .setDescription("Controls Y");

    public final CompoundParameter xPos =
        new CompoundParameter("xPos", model.cx, model.xMin, model.xMax)
            .setDescription("Controls X");

    public final CompoundParameter zPos =
        new CompoundParameter("zPos", model.cz, model.zMin, model.zMax)
            .setDescription("Controls Z");

    public final CompoundParameter waveSpeed =
        new CompoundParameter("speed", 0.001, 0.5)
            .setDescription("Controls the speed");

    public final CompoundParameter falloff =
        new CompoundParameter("falloff", 0, 40*FEET)
            .setDescription("Controls the falloff over distance");

    public final CompoundParameter scale =
        new CompoundParameter("scale", 0.1, 20)
            .setDescription("Scale the input (after offset)");

    public final CompoundParameter offset =
        new CompoundParameter("offset", 0, 2)
            .setDescription("Offset the input (-1, 1)");

    public final CompoundParameter sourceColor =
        new CompoundParameter("Color", 0, 360)
            .setDescription("Controls the falloff");

    public final DiscreteParameter clamp =
        new DiscreteParameter("clamp", 0, 2 )
            .setDescription("clamp the input signal to be positive ");

    public TreeSphericalWave(LX lx) {
        super(lx);
        addParameter(input);
        addParameter(yPos);
        addParameter(xPos);
        addParameter(zPos);
        addParameter(waveSpeed);
        addParameter(falloff);
        addParameter(offset);
        addParameter(scale);
        addParameter(sourceColor);
        addParameter(clamp);
        inputHist = new hist(1000);
    }

    public void run(double deltaMs) {
        float inputVal = (float)input.getValue();
        inputHist.addValue(inputVal);

        float speed = (float)waveSpeed.getValue();
        color leafColor = LX.rgb(0, 0,0);

//    println("input val is "+inputVal);
        float offsetVal = (float)offset.getValue();
        offsetVal = offsetVal-1;

        float scaleVal = (float)scale.getValue();
        float dist=0;
        float sourceAdd = 0;
        int histIdx=0;
        float histVal=0;
        float sourceBaseColor = (float)sourceColor.getValue();
        float clampInput = (int)clamp.getValue();

        for (TreeModel.Leaf leaf : tree.leaves) {
            dist = sqrt(sq((float)leaf.x - (float)xPos.getValue())
                + sq((float)leaf.y - (float)yPos.getValue())
                + sq((float)leaf.z - (float)zPos.getValue()));
            sourceAdd = 0;
            histIdx = inputHist.lookupInd((int)(dist*speed));

            if (histIdx != -1){
                if (clampInput == 0){
                    histVal= min(1,inputHist.getValue(histIdx)+offsetVal)*scaleVal*max(0, 100-min(1, dist/(float)falloff.getValue())*100 );
                }else{
                    histVal= min(1,max(0,inputHist.getValue(histIdx)+offsetVal)*scaleVal)*max(0, 100-min(1, dist/(float)falloff.getValue())*100 );
                }
                leafColor = LX.hsb(sourceBaseColor, 100, histVal);
            }
            setColor(leaf, leafColor);

        }
    }
}
