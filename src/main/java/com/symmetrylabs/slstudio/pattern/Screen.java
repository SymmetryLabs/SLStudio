package com.symmetrylabs.slstudio.pattern;

import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.util.DrawHelper;
import com.symmetrylabs.util.MathUtils;
import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.p3lx.LXStudio;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.opengl.PGraphics2D;
import processing.opengl.PShader;

import java.util.concurrent.locks.ReentrantLock;

public abstract class Screen extends LXPattern {
    protected PGraphics2D pg;
    protected int pWidth;
    protected int pHeight;
    Runnable drawR;
    Runnable setupR;
    PShader blur;
    int i = 0;
    public CompoundParameter sigma = new CompoundParameter("sigma", 5, 1, 20);

    private final ReentrantLock lock = new ReentrantLock();

    public abstract void draw();
    public abstract void setup();

    public Screen(LX lx) {
        super(lx);

        addParameter(sigma);





        float yRatio = model.yRange / model.xRange;
        pWidth = 1000;
        pHeight = (int)Math.ceil(pWidth * yRatio);

        pg = (PGraphics2D)((SLStudioLX)lx).applet.createGraphics(pWidth, pHeight, PConstants.P2D);

        blur = ((SLStudioLX)lx).applet.loadShader("blur.glsl");


        drawR = new Runnable() {
            @Override
            public void run() {
                blur.set("blurSize", 40);
                blur.set("sigma", sigma.getValuef());
                pg.beginDraw();
                draw();
                pg.filter(blur);
                pg.endDraw();
                lock.lock();
                pg.loadPixels();
                lock.unlock();
                i++;
            }
        };

        setupR = new Runnable() {
            @Override
            public void run() {
                pg.beginDraw();
                setup();
                pg.endDraw();
            }
        };

        DrawHelper.queueJob("setup", setupR);
    }

    @Override
    protected void run(double deltaMs) {
        DrawHelper.queueJob("draw", drawR);


        if (pg.pixels == null || lock.isLocked()) {
            return;
        }

        lock.lock();

        getVectorList().parallelStream().forEach(v -> {
            int pX = (int)(MathUtils.map(v.x, model.xMin, model.xMax, 0, pWidth - 1));
            int pY = (int)(MathUtils.map(v.y, model.yMin, model.yMax, pHeight - 1, 0));
            int i = pY * pWidth + pX;
            try {
                int c = pg.pixels[i];
                colors[v.index] = c;

            } catch (Exception e) {
                e.printStackTrace();
                System.out.printf("X: %d Y: %d I: %d N: %d\n", pX, pY, i, pg.pixels.length);
            }


        });

        lock.unlock();
    }
}
