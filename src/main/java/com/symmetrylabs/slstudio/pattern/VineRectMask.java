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



public class VineRectMask extends LXPattern {

    public ArrayList<VineGeometry.Shape> shapes = new ArrayList();

    public CompoundParameter scale = new CompoundParameter("scale", 0.1f);

    VineGeometry.Shape rect = new VineGeometry.Shape(VineGeometry.Shape.ShapeType.RECT);

    public VineRectMask(LX lx) {
        super(lx);

        addParameter(scale);

        setupShapes();


        Collections.reverse(shapes);
    }


    void setupShapes() {
       shapes.add(rect);
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

        rect.scalex = scale.getValuef();
        rect.scaley = 1.0f;


        // for (LXPoint p : model.points) {

               
        // }

        getVectorList().parallelStream().forEach(p -> {
             float x = (p.x - model.cx) / range;
                float y = (p.y - model.cy) / range;
                boolean found = false;

                if (rect.contains(x, y)) {
                    colors[p.index] = LXColor.hsb(0, 0, 0);
                } else {
                   
                    colors[p.index] = LXColor.hsb(0, 0, 100);
                }
            
        });

        markModified(SRGB8);
    }
}
