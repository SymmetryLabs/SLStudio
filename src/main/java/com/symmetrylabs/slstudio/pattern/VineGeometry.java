package com.symmetrylabs.slstudio.pattern;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.LXPattern;
import heronarts.lx.PolyBuffer;
import heronarts.lx.audio.GraphicMeter;
import heronarts.lx.audio.LXAudioInput;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;
import java.util.ArrayList; 
import java.util.Collections;

import static com.symmetrylabs.util.MathUtils.*;
import static heronarts.lx.PolyBuffer.Space.SRGB8;



public class VineGeometry extends LXPattern {

    static public class Shape {
        float cx = 0;
        float cy = 0;
        float scalex = 0;
        float scaley = 0;
        float rot = 0;
        float hue = 0;
        float topHue = 0;
        float hueCutoff = 0.7f;

        enum ShapeType {
            RECT, CIRCLE;
        }

        ShapeType shapeType;

        public Shape(ShapeType shapeType_) {
            this(shapeType_, 0.1f);
        }

         public Shape(ShapeType shapeType_, float scale) {
            this(shapeType_, scale, 0);
        }

        public Shape(ShapeType shapeType_, float scale, float hue_) {
            shapeType = shapeType_;
            scalex = scale;
            scaley = scale;
            hue = hue_;
            topHue = hue_;
        }

        public boolean contains(float x, float y) {
            // float xd = (x - cx) / scalex;
            // float yd = (y - cy) / scaley;

            float cosx = (float)Math.cos(-rot);
            float sinx = (float)Math.sin(-rot);

            float xd = cx + ( cosx * (x-cx) + sinx * (y -cy));
            float yd = cy + ( -sinx * (x-cx) + cosx * (y -cy));

            xd = (xd - cx) / scalex;
            yd = (yd - cy) / scaley;


            if (shapeType == ShapeType.CIRCLE) {
                return Math.sqrt(xd * xd + yd * yd) < 1;
            }

            if (shapeType == ShapeType.RECT) {
                return xd > -1 && xd < 1 && yd > -1 && yd < 1;
            }

            return false;
        }


    }

    public ArrayList<Shape> shapes = new ArrayList();

    public CompoundParameter scale_x = new CompoundParameter("scale_x", 0.1f);
    public CompoundParameter scale_y = new CompoundParameter("scale_y", 0.1f);
    public CompoundParameter rot = new CompoundParameter("rot", 0, 0, 2 * Math.PI);

    public CompoundParameter top = new CompoundParameter("top", 240, 0, 360);
    public CompoundParameter bot = new CompoundParameter("bot", 330, 0, 360);
    public CompoundParameter cut = new CompoundParameter("cut", 0.7f);

    public VineGeometry(LX lx) {
        super(lx);

        addParameter(scale_x);
        addParameter(scale_y);
        addParameter(rot);

        addParameter(top);
        addParameter(bot);
        addParameter(cut);

        setupShapes();


        Collections.reverse(shapes);
    }


    void setupShapes() {
        Shape rect = new Shape(Shape.ShapeType.RECT);
        rect.cx = -0.5f;
        
        // shapes.add(rect);

        Shape circ = new Shape(Shape.ShapeType.CIRCLE, 0.2f, 240.0f    );
        // circ.hue = 
        // circ.cx = 0.5f;
        shapes.add(circ);
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

        for (Shape s : shapes) {
            s.scalex = scale_x.getValuef();
            s.scaley = scale_y.getValuef();
            s.rot = rot.getValuef();

            s.hue = bot.getValuef();
            s.topHue = top.getValuef();
            s.hueCutoff = cut.getValuef();
        }

        // for (LXPoint p : model.points) {

               
        // }

        getVectorList().parallelStream().forEach(p -> {
             float x = (p.x - model.cx) / range;
                float y = (p.y - model.cy) / range;
                boolean found = false;
                for (Shape s : shapes) {
                    if (s.contains(x, y)) {
                        found = true;
                        float lerp = (float)(s.hue + ((s.topHue - s.hue) * (((float)Math.abs(y) - s.hueCutoff) / (1.0 - s.hueCutoff))));
                        float h = Math.abs(y) > s.hueCutoff ? s.topHue : s.hue;
                        colors[p.index] = LXColor.hsb(h, 100, 100);
                        break;
                    }
                }
                
                if (!found) {
                    colors[p.index] = LXColor.hsb(0, 0, 0);
                }
            
        });

        markModified(SRGB8);
    }
}
