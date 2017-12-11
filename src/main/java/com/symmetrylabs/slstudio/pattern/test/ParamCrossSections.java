package com.symmetrylabs.slstudio.pattern.test;

import com.symmetrylabs.slstudio.pattern.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import processing.core.PImage;

import static processing.core.PApplet.*;

/**
* @author Yona Appletree (yona@concentricsky.com)
*/
public class ParamCrossSections extends SLPattern implements SLTestPattern {

    final CompoundParameter x = new CompoundParameter("XPOS", 0.3);
    final CompoundParameter y = new CompoundParameter("YPOS", 0.3);
    final CompoundParameter z = new CompoundParameter("ZPOS", 0.3);
    final CompoundParameter xw = new CompoundParameter("XWID", 0.3);
    final CompoundParameter yw = new CompoundParameter("YWID", 0.3);
    final CompoundParameter zw = new CompoundParameter("ZWID", 0.3);
    final CompoundParameter xl = new CompoundParameter("XLEV", 1);
    final CompoundParameter yl = new CompoundParameter("YLEV", 1);
    final CompoundParameter zl = new CompoundParameter("ZLEV", 0.5);

    public ParamCrossSections(LX lx) {
        super(lx);
        addParams();
    }

    protected void addParams() {
        addParameter(x);
        addParameter(y);
        addParameter(z);
        addParameter(xl);
        addParameter(yl);
        addParameter(zl);
        addParameter(xw);
        addParameter(yw);
        addParameter(zw);
    }

    float xv, yv, zv;

    protected void updateXYZVals() {
        xv = model.xMin + (x.getValuef() * model.xRange);
        yv = model.yMin + (y.getValuef() * model.yRange);
        zv = model.zMin + (z.getValuef() * model.zRange);
    }

    public void run(double deltaMs) {
        updateXYZVals();

        float xlv = 100*xl.getValuef();
        float ylv = 100*yl.getValuef();
        float zlv = 100*zl.getValuef();

        float xwv = 100f / (1 + 1*xw.getValuef());
        float ywv = 100f / (1 + 1*yw.getValuef());
        float zwv = 100f / (1 + 1*zw.getValuef());

        for (LXPoint p : model.points) {
            int c = 0;
            c = PImage.blendColor(c, lx.hsb(
            (palette.getHuef() + p.x/10 + p.y/3) % 360,
            constrain(140 - 1.1f*abs(p.x - model.xMax/2f), 0, 100),
            max(0, xlv - xwv*abs(p.x - xv))
                ), ADD);
            c = PImage.blendColor(c, lx.hsb(
            (palette.getHuef() + 80 + p.y/10) % 360,
            constrain(140 - 2.2f*abs(p.y - model.yMax/2f), 0, 100),
            max(0, ylv - ywv*abs(p.y - yv))
                ), ADD);
            c = PImage.blendColor(c, lx.hsb(
            (palette.getHuef() + 160 + p.z / 10 + p.y/2) % 360,
            constrain(140 - 2.2f*abs(p.z - model.zMax/2f), 0, 100),
            max(0, zlv - zwv*abs(p.z - zv))
                ), ADD);
            colors[p.index] = c;
        }
    }
}
