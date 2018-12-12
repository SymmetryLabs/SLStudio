package com.symmetrylabs.slstudio.pattern;

import com.symmetrylabs.color.Ops16;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import heronarts.lx.color.LXColor;
import heronarts.lx.modulator.ADSREnvelope;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.EnumParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.transform.LXVector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Diamonds extends SLPattern<SLModel> {
    private class Diamond {
        float age;
        LXVector loc;
    }

    private List<Diamond> diamonds = new ArrayList<>();
    private final Random random = new Random();

    private CompoundParameter reupParam =
        new CompoundParameter("reup", 0, 0, 1000);
    private CompoundParameter releaseParam =
        new CompoundParameter("release", 300, 0, 2000);
    private CompoundParameter sizeParam =
        new CompoundParameter("size", 0.1 * model.rMax, 0, model.rMax);
    private CompoundParameter shrinkParam =
        new CompoundParameter("shrink", 0.1, 0, 1);
    private BooleanParameter trigger =
        new BooleanParameter("trigger", false)
        .setMode(BooleanParameter.Mode.MOMENTARY);

    double sinceLastReUp;

    public Diamonds(LX lx) {
        super(lx);
        addParameter(releaseParam);
        addParameter(sizeParam);
        addParameter(shrinkParam);
        addParameter(reupParam);
        addParameter(trigger);
    }

    @Override
    public void onParameterChanged(LXParameter p) {
        super.onParameterChanged(p);
        if (p == trigger) {
            if (trigger.getValueb()) {
                addDiamond();
            }
        }
    }

    private void addDiamond() {
        Diamond d = new Diamond();
        LXVector[] vs = getVectorArray();
        d.loc = vs[random.nextInt(vs.length)];
        d.age = 0;
        diamonds.add(d);
    }

    @Override
    public String getCaption() {
        return String.format("%d diamonds", diamonds.size());
    }

    @Override
    public void run(double elapsedMs, PolyBuffer.Space preferredSpace) {
        if (reupParam.getValuef() > 0) {
            float reup = reupParam.getValuef();
            sinceLastReUp += elapsedMs;
            while (sinceLastReUp > reup) {
                addDiamond();
                sinceLastReUp -= reup;
            }
        }
        for (Diamond d : diamonds) {
            d.age += (float) elapsedMs;
        }
        long[] colors = (long[]) getArray(PolyBuffer.Space.RGB16);
        float release = releaseParam.getValuef();
        float size = sizeParam.getValuef();
        float shrink = shrinkParam.getValuef();
        for (LXVector v : getVectors()) {
            float max = 0;
            float minAge = release;
            for (Diamond d : diamonds) {
                LXVector off = v.copy().mult(-1.f).add(d.loc);
                float dist = Math.abs(off.x) + Math.abs(off.y) + Math.abs(off.z);
                float lvl = Float.max(0, 1.f - (d.age / release));
                float dsize = ((1.f - shrink) + lvl * shrink) * size;
                if (dist < dsize) {
                    if (d.age < minAge) {
                        minAge = d.age;
                        max = lvl;
                    }
                }
            }
            int gray = (int) Math.round((float) 0xFFFF * max);
            colors[v.index] = Ops16.rgba(gray, gray, gray, 0xFFFF);
        }
        markModified(PolyBuffer.Space.RGB16);
        diamonds.removeIf(d -> d.age > release);
    }
}
