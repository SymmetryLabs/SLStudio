package com.symmetrylabs.slstudio.pattern;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.parameter.BooleanParameter;

import com.symmetrylabs.slstudio.model.Sun;
import com.symmetrylabs.slstudio.model.SunsModel;

public abstract class CopySunsPattern extends SLPattern {
    public BooleanParameter perSun;

    // we have to do this because we can't cast Sun into SunsModel if we derive
    // from SunsPattern instead
    private SunsModel sunsModel;

    public CopySunsPattern(LX lx) {
        super(lx);

        try {
            sunsModel = (SunsModel)model;
        } catch (ClassCastException e) {
            sunsModel = new SunsModel();
        }
    }

    @Override
    protected void createParameters() {
        perSun = new BooleanParameter("perSun");

        if (!(model instanceof SunsModel))
            return;

        addParameter(perSun);

        perSun.addListener(param -> {
            if (perSun.isOn() && !sunsModel.getSuns().isEmpty()) {
                setModel(sunsModel.getMasterSun());
            }
            else {
                setModel(sunsModel);
            }
        });
    }

    @Override
    public void loop(double deltaMs) {
        super.loop(deltaMs);

        if (perSun.isOn()) {
            for (Sun sun : sunsModel.getSuns()) {
                sun.copyFromMasterSun(colors);
            }
        }
    }
}
