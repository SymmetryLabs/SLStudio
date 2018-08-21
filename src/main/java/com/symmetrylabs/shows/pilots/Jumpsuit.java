package com.symmetrylabs.shows.pilots;

import com.symmetrylabs.color.Ops8;
import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.modulator.ADSREnvelope;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.transform.LXVector;

public class Jumpsuit<T extends Strip> extends SLPattern<StripsModel<T>> {
    public static final String GROUP_NAME = PilotsShow.SHOW_NAME;

    private BooleanParameter yellowAttack = new BooleanParameter("y", false);
    private BooleanParameter redAttack = new BooleanParameter("r", false);

    private CompoundParameter yellowCenter = new CompoundParameter("yc", model.cx, model.xMin, model.xMax);
    private CompoundParameter redCenter = new CompoundParameter("rc", model.cx, model.xMin, model.xMax);

    private CompoundParameter rHit = new CompoundParameter("rhit", 30, 0, 120);
    private CompoundParameter yHit = new CompoundParameter("yhit", 30, 0, 120);
    private CompoundParameter rDrop = new CompoundParameter("rdrop", 30, 0, 120);
    private CompoundParameter yDrop = new CompoundParameter("ydrop", 30, 0, 120);

    private CompoundParameter attack = new CompoundParameter("attack", 0, 100, 500);
    private CompoundParameter decay = new CompoundParameter("decay", 100, 0, 500);
    private CompoundParameter sustain = new CompoundParameter("sustain", 0.8, 0, 1);
    private CompoundParameter release = new CompoundParameter("release", 750, 0, 2000);

    private ADSREnvelope redADSR = new ADSREnvelope("redADSR", 0, 1, attack, decay, sustain, release);
    private ADSREnvelope yellowADSR = new ADSREnvelope("yellowADSR", 0, 1, attack, decay, sustain, release);

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

        addParameter(rHit);
        addParameter(rDrop);
        addParameter(yHit);
        addParameter(yDrop);

        addParameter(fadeStart);
        addParameter(fadeFor);

        addParameter(attack);
        addParameter(decay);
        addParameter(sustain);
        addParameter(release);

        addModulator(redADSR);
        addModulator(yellowADSR);
    }

    @Override
    public void onParameterChanged(LXParameter p) {
        if (p == redAttack) {
            if (redAttack.getValueb()) {
                redADSR.attack();
                redLevel += rHit.getValue();
            } else {
                redADSR.release();
            }
        }
        if (p == yellowAttack) {
            if (yellowAttack.getValueb()) {
                yellowADSR.attack();
                yellowLevel += yHit.getValue();
            } else {
                yellowADSR.release();
            }
        }
    }

    @Override
    public void run(double deltaMs) {
        yellowLevel = Double.max(0, yellowLevel - deltaMs / 1000 * yDrop.getValue());
        redLevel = Double.max(0, redLevel - deltaMs / 1000 * rDrop.getValue());

        yellowAge += deltaMs;
        redAge += deltaMs;

        int black = LXColor.gray(0);
        for (int i = 0; i < colors.length; i++) {
            colors[i] = black;
        }

        double rAlpha = redADSR.getValuef();
        double yAlpha = yellowADSR.getValuef();

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
