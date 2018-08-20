package com.symmetrylabs.shows.pilots;

import com.symmetrylabs.color.Ops8;
import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.transform.LXVector;

public class Jumpsuit<T extends Strip> extends SLPattern<StripsModel<T>> {
    public static final String GROUP_NAME = PilotsShow.SHOW_NAME;

    public BooleanParameter yellowAttack = new BooleanParameter("Y", false);
    public BooleanParameter redAttack = new BooleanParameter("R", false);

    public CompoundParameter yellowCenter = new CompoundParameter("YC", model.cx, model.xMin, model.xMax);
    public CompoundParameter redCenter = new CompoundParameter("RC", model.cx, model.xMin, model.xMax);

    double redLevel = 100;
    double yellowLevel = 100;

    public Jumpsuit(LX lx) {
        super(lx);

        yellowAttack.setMode(BooleanParameter.Mode.MOMENTARY);
        redAttack.setMode(BooleanParameter.Mode.MOMENTARY);

        addParameter(yellowAttack);
        addParameter(redAttack);
        addParameter(yellowCenter);
        addParameter(redCenter);
    }

    @Override
    public void run(double deltaMs) {
        int black = LXColor.gray(0);
        for (int i = 0; i < colors.length; i++) {
            colors[i] = black;
        }

        double rc = redCenter.getValue();
        double yc = yellowCenter.getValue();

        for (LXVector v : getVectors()) {
            double rd = Math.abs(v.x - rc) + Math.abs(v.y - model.yMin);
            double yd = Math.abs(v.x - rc) + Math.abs(v.y - model.yMin);

            colors[v.index] = 0;
            if (rd <= redLevel) {
                colors[v.index] = Ops8.add(colors[v.index], PilotsShow.RED);
            }
            if (yd <= yellowLevel) {
                colors[v.index] = Ops8.add(colors[v.index], PilotsShow.YELLOW);
            }
        }
    }
}
