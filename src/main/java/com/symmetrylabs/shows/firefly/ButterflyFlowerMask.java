package com.symmetrylabs.shows.firefly;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.BooleanParameter;
import com.symmetrylabs.slstudio.effect.SLEffect;
import art.lookingup.KaledoscopeModel;
import art.lookingup.LUButterfly;
import art.lookingup.LUFlower;

public class ButterflyFlowerMask extends SLEffect<KaledoscopeModel> {
    public final BooleanParameter enableButterfliesParam;
    public final BooleanParameter enableFlowersParam;

    public ButterflyFlowerMask(LX lx) {
        super(lx);

        addParameter(enableButterfliesParam = new BooleanParameter("EnableButterflies", true));
        addParameter(enableFlowersParam = new BooleanParameter("EnableFlowers", true));
    }

    @Override
    public void run(double deltaMs, double amount) {
        if (!enableButterfliesParam.isOn()) {
            for (int i = 0; i < KaledoscopeModel.allButterflies.size(); ++i) {
                LUButterfly butterfly = KaledoscopeModel.allButterflies.get(i);
                for (LXPoint p : butterfly.allPoints) {
                    colors[p.index] = LXColor.BLACK;
                }
            }
        }

        if (!enableFlowersParam.isOn()) {
            for (int i = 0; i < KaledoscopeModel.allFlowers.size(); ++i) {
                LUFlower flower = KaledoscopeModel.allFlowers.get(i);
                for (LXPoint p : flower.allPoints) {
                    colors[p.index] = LXColor.BLACK;
                }
            }
        }
    }
}
