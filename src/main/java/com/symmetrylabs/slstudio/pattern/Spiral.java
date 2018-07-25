package com.symmetrylabs.slstudio.pattern;

import java.lang.Math;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.LXPattern;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.LXUtils;
import heronarts.lx.transform.LXProjection;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.transform.LXVector;
import heronarts.lx.LXDeviceComponent;
import heronarts.lx.LXLayer;
import heronarts.lx.color.LXColor;

import static com.symmetrylabs.util.MathUtils.*;
import static com.symmetrylabs.util.MathConstants.*;


public class Spiral extends LXPattern {

    final CompoundParameter speed = new CompoundParameter("speed", 4, -15, 15);
    final CompoundParameter xTrans = new CompoundParameter("xTran", model.cx, model.xMin, model.xMax);
    final CompoundParameter yTrans = new CompoundParameter("yTran", model.cy, model.yMin, model.yMax);
    final CompoundParameter scale = new CompoundParameter("scale", 0.5);
    final CompoundParameter thick = new CompoundParameter("thick", 3, 1, 20);
    final CompoundParameter blur = new CompoundParameter("blur", 0.5, 0.4, 0.9);

    final private BlurLayer blurLayer = new BlurLayer(lx, this, blur);
    private LXProjection spiral;
    private float rotated = 0;

    public Spiral(LX lx) {
        super(lx);

        onVectorsChanged();
        addParameter(speed);
        addParameter(xTrans);
        addParameter(yTrans);
        addParameter(scale);
        addParameter(thick);
        addParameter(blur);
        addLayer(blurLayer);
    }

    public void onVectorsChanged() {
        super.onVectorsChanged();
        this.spiral = new LXProjection(model, getVectors());
    }

    public void run(double deltaMs) {
        setColors(0);

        float scaleVal = lerp(2.5f, 0.2f, scale.getValuef());
        rotated += speed.getValuef() * PI / 180.f;

        spiral.reset()
                    .scale(scaleVal, scaleVal, 0)
                    .translate(xTrans.getValuef(), yTrans.getValuef(), 0)
                    .rotateZ(rotated);

        float thickV = thick.getValuef();

        float x = 0;
        float y = 0;
        float hue = palette.getHuef();
        float sat = palette.getSaturationf();

        for (float t = 0; t < 3 * TWO_PI; t += PI/35.f) {
            x = 8 * t * cos(t);
            y = 8 * t * sin(t);

            for (LXVector p : spiral) {
                if (dist(p.x, p.y, x, y) < thickV) {
                    colors[p.index] = lx.hsb(hue, sat, 100);
                }
            }
        }
    }
}

class BlurLayer extends LXLayer {
    final CompoundParameter amount;
    private final int[] blurBuffer;

    BlurLayer(LX lx, LXPattern pattern, CompoundParameter amount) {
        super(lx, pattern);
        this.amount = amount;
        this.blurBuffer = new int[lx.total];

        for (int i = 0; i < blurBuffer.length; ++i) {
            blurBuffer[i] = 0;
        }
    }

    public void run(double deltaMs) {
        float blurf = amount.getValuef();
        if (blurf > 0) {
            blurf = 1 - (1 - blurf) * (1 - blurf) * (1 - blurf);
            for (int i = 0; i < colors.length; ++i) {
                int blend = LXColor.screen(colors[i], blurBuffer[i]);
                colors[i] = LXColor.lerp(colors[i], blend, blurf);
            }
        }
        for (int i = 0; i < colors.length; ++i) {
            blurBuffer[i] = colors[i];
        }
    }
}
