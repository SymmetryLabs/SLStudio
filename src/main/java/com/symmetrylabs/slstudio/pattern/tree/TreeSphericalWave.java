package com.symmetrylabs.slstudio.pattern.tree;

import static processing.core.PApplet.sq;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;

import com.symmetrylabs.shows.kalpa.TreeModel;
import static com.symmetrylabs.util.DistanceConstants.*;
import static com.symmetrylabs.util.MathUtils.*;


public class TreeSphericalWave extends TreePattern {

    public String getAuthor() {
        return "Aimone";
    }
    private Hist inputHist;

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
        new CompoundParameter("speed", 0.001f, 0.5f)
            .setDescription("Controls the speed");

    public final CompoundParameter falloff =
        new CompoundParameter("falloff", 0, 40*FEET)
            .setDescription("Controls the falloff over distance");

    public final CompoundParameter scale =
        new CompoundParameter("scale", 0.1f, 20)
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
        inputHist = new Hist(1000);
    }

    public void run(double deltaMs) {
        float inputVal = (float)input.getValue();
        inputHist.addValue(inputVal);

        float speed = (float)waveSpeed.getValue();
        int leafColor = 0;

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

        for (TreeModel.Leaf leaf : tree.getLeaves()) {
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

    private class Hist {
        float[] history;
        int index;

        public Hist(int w) {
            history = new float[w];
        }

        public void addValue(float newValue) {
            history[index] = newValue;
            index = (index+1) % history.length;
        }

        public int prevValue(int index) {
            int i = index - 1;
            if (i < 0) i = history.length - 1;
            return i;
        }

        public int lookupInd(int ind){
            if (ind > history.length){
                return -1;
            }
            int i = index - ind;
            if (i < 0) i = history.length + i;
            return i;
        }
        public float getValue(int ind) {
            return history[ind];
        }
    }
}
