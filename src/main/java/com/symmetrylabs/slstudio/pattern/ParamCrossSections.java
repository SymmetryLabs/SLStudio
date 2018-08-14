package com.symmetrylabs.slstudio.pattern;

import com.symmetrylabs.color.Ops8;
import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.PolyBuffer;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.transform.LXVector;

import static com.symmetrylabs.util.MathUtils.*;
import static heronarts.lx.PolyBuffer.Space.SRGB8;

public class ParamCrossSections extends LXPattern {

    final CompoundParameter x = new CompoundParameter("xPos", 0, model.xMin, model.xMax);
    final CompoundParameter y = new CompoundParameter("yPos", 0, model.yMin, model.yMax);
    final CompoundParameter z = new CompoundParameter("zPos", 0, model.zMin, model.zMax);

    final CompoundParameter xl = new CompoundParameter("xLvl", 1);
    final CompoundParameter yl = new CompoundParameter("yLvl", 1);
    final CompoundParameter zl = new CompoundParameter("zLvl", 0.5);

    final CompoundParameter xr = new CompoundParameter("xSpd", 0.7);
    final CompoundParameter yr = new CompoundParameter("ySpd", 0.6);
    final CompoundParameter zr = new CompoundParameter("zSpd", 0.5);

    final CompoundParameter xw = new CompoundParameter("xSize", 0.3);
    final CompoundParameter yw = new CompoundParameter("ySize", 0.3);
    final CompoundParameter zw = new CompoundParameter("zSize", 0.3);


    public ParamCrossSections(LX lx) {
        super(lx);
        addParameter(x);
        addParameter(y);
        addParameter(z);
        addParameter(xr);
        addParameter(yr);
        addParameter(zr);
        addParameter(xw);
        addParameter(xl);
        addParameter(yl);
        addParameter(zl);
        addParameter(yw);
        addParameter(zw);
    }

    float xv, yv, zv;

    protected void updateXYZVals() {
        xv = x.getValuef();
        yv = y.getValuef();
        zv = z.getValuef();
    }

    public void run(double deltaMs, PolyBuffer.Space space) {
        int[] colors = (int[]) getArray(SRGB8);

        updateXYZVals();

        float xlv = 100 * xl.getValuef();
        float ylv = 100 * yl.getValuef();
        float zlv = 100 * zl.getValuef();

        float xwv = 100f / (10 + 40 * xw.getValuef());
        float ywv = 100f / (10 + 40 * yw.getValuef());
        float zwv = 100f / (10 + 40 * zw.getValuef());

        for (LXVector p : getVectorList()) {
            int c = 0;
            c = Ops8.add(c, LXColor.hsb(
                palette.getHuef() + p.x / 10 + p.y / 3,
                constrain(140 - 1.1f * abs(p.x - model.xMax / 2f), 0, 100),
                max(0, xlv - xwv * abs(p.x - xv))
            ));
            c = Ops8.add(c, LXColor.hsb(
                palette.getHuef() + 80 + p.y / 10,
                constrain(140 - 2.2f * abs(p.y - model.yMax / 2f), 0, 100),
                max(0, ylv - ywv * abs(p.y - yv))
            ));
            c = Ops8.add(c, LXColor.hsb(
                palette.getHuef() + 160 + p.z / 10 + p.y / 2,
                constrain(140 - 2.2f * abs(p.z - model.zMax / 2f), 0, 100),
                max(0, zlv - zwv * abs(p.z - zv))
            ));
            colors[p.index] = c;
        }
        markModified(SRGB8);
    }
}
