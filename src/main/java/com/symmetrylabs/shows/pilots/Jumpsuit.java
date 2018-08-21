package com.symmetrylabs.shows.pilots;

import com.symmetrylabs.color.Ops8;
import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.transform.LXVector;

public class Jumpsuit<T extends Strip> extends SLPattern<StripsModel<T>> {
    public static final String GROUP_NAME = PilotsShow.SHOW_NAME;

    public BooleanParameter yellowAttack = new BooleanParameter("y", false);
    public BooleanParameter redAttack = new BooleanParameter("r", false);

    public CompoundParameter yellowCenter = new CompoundParameter("yc", model.cx, model.xMin, model.xMax);
    public CompoundParameter redCenter = new CompoundParameter("rc", model.cx, model.xMin, model.xMax);

    public CompoundParameter hit = new CompoundParameter("hit", 30, 0, 120);
    public CompoundParameter decay = new CompoundParameter("decay", 30, 0, 120);

    public CompoundParameter fadeStart = new CompoundParameter("fstart", 750, 0, 1500);
    public CompoundParameter fadeFor = new CompoundParameter("fdecay", 2000, 0, 5000);

    double redLevel = 100;
    double yellowLevel = 100;

    double redAge = 0;
    double yellowAge = 0;

    boolean redLock = false;
    boolean yellowLock = false;

    public Jumpsuit(LX lx) {
        super(lx);

        yellowAttack.setMode(BooleanParameter.Mode.MOMENTARY);
        redAttack.setMode(BooleanParameter.Mode.MOMENTARY);

        addParameter(yellowAttack);
        addParameter(redAttack);
        addParameter(yellowCenter);
        addParameter(redCenter);

        addParameter(hit);
        addParameter(decay);

        addParameter(fadeStart);
        addParameter(fadeFor);
    }

    @Override
    public void onParameterChanged(LXParameter p) {
        if (p == redAttack) {
            if (redAttack.getValueb()) {
                redLevel += hit.getValue();
                redLock = true;
                redAge = 0;
            } else {
                redLock = false;
            }
        }
        if (p == yellowAttack) {
            if (yellowAttack.getValueb()) {
                yellowLevel += hit.getValue();
                yellowLock = true;
                yellowAge = 0;
            } else {
                yellowLock = false;
            }
        }
    }

    @Override
    public void run(double deltaMs) {
        double decrease = deltaMs / 1000 * decay.getValue();
        if (!yellowLock) {
            yellowLevel = Double.max(0, yellowLevel - decrease);
        }
        if (!redLock) {
            redLevel = Double.max(0, redLevel - decrease);
        }

        int black = LXColor.gray(0);
        for (int i = 0; i < colors.length; i++) {
            colors[i] = black;
        }

        redAge += deltaMs;
        yellowAge += deltaMs;

        double rAlpha, yAlpha;
        double fStart = fadeStart.getValue();
        double fFor = fadeFor.getValue();

        if (redAge < fStart) {
            rAlpha = 1;
        } else {
            rAlpha = Double.max(0, (fFor - (redAge - fStart)) / fFor);
        }
        if (yellowAge < fStart) {
            yAlpha = 1;
        } else {
            yAlpha = Double.max(0, (fFor - (yellowAge - fStart)) / fFor);
        }

        double rc = redCenter.getValue();
        double yc = yellowCenter.getValue();

        for (LXVector v : getVectors()) {
            double yz = v.y - model.yMin;
            double rd = Math.abs(v.x - rc) + yz;
            double yd = Math.abs(v.x - yc) + yz;

            colors[v.index] = 0;
            if (rd <= redLevel) {
                colors[v.index] = Ops8.add(colors[v.index], PilotsShow.RED, rAlpha);
            }
            if (yd <= yellowLevel) {
                colors[v.index] = Ops8.add(colors[v.index], PilotsShow.YELLOW, yAlpha);
            }
        }
    }
}
