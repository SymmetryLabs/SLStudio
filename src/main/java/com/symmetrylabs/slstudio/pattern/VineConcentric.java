package com.symmetrylabs.slstudio.pattern;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.LXPattern;
import heronarts.lx.PolyBuffer;
import heronarts.lx.audio.GraphicMeter;
import heronarts.lx.audio.LXAudioInput;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.color.ColorParameter;
import java.util.ArrayList; 
import java.util.Collections;

import static com.symmetrylabs.util.MathUtils.*;
import static heronarts.lx.PolyBuffer.Space.SRGB8;

import com.symmetrylabs.slstudio.pattern.VineGeometry.Shape;



public class VineConcentric extends LXPattern {

    public ArrayList<VineGeometry.Shape> shapes = new ArrayList();

    public CompoundParameter smallScale = new CompoundParameter("smallScale", 0.1f);
    public CompoundParameter bigScale = new CompoundParameter("bigScale", 0.3f);

    private final ColorParameter smallColor = new ColorParameter("smallColor", LXColor.RED);
    private final ColorParameter bigColor = new ColorParameter("bigColor", LXColor.BLUE);

    VineGeometry.Shape small = new VineGeometry.Shape(VineGeometry.Shape.ShapeType.CIRCLE);
    VineGeometry.Shape big = new VineGeometry.Shape(VineGeometry.Shape.ShapeType.CIRCLE);

    public VineConcentric(LX lx) {
        super(lx);

        addParameter(smallScale);
        addParameter(bigScale);
        addParameter(smallColor);
        addParameter(bigColor);

        setupShapes();


        Collections.reverse(shapes);
    }


    void setupShapes() {
       shapes.add(big);
       shapes.add(small);
    }

    // @Override
    // public void onActive() {
    //     super.onActive();
    // }

    public static final float HUE_RATE = 100;
    public static final float BRTNESS_RATE = 800;
    public static final float DESAT_RATE = 50;

    @Override
    public void run(double deltaMs, PolyBuffer.Space space) {
        int[] colors = (int[]) getArray(SRGB8);
        float range = Math.max(model.xRange, model.yRange) / 2;

        small.scalex = smallScale.getValuef();
        small.scaley = smallScale.getValuef();
        big.scalex = bigScale.getValuef();
        big.scaley = bigScale.getValuef();


        // for (LXPoint p : model.points) {

               
        // }

        getVectorList().parallelStream().forEach(p -> {
             float x = (p.x - model.cx) / range;
                float y = (p.y - model.cy) / range;
                boolean found = false;

                if (small.contains(x, y)) {
                    colors[p.index] = smallColor.getColor();
                } else if (big.contains(x, y)) {
                    colors[p.index] = bigColor.getColor();
                } else {
                    colors[p.index] = LXColor.hsb(0, 0, 0);
                }
            
        });

        markModified(SRGB8);
    }
}
