package com.symmetrylabs.shows.firefly;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.EnumParameter;
import com.symmetrylabs.slstudio.effect.SLEffect;
import art.lookingup.KaledoscopeModel;
import art.lookingup.LUButterfly;
import art.lookingup.LUFlower;

public class ButterflyFlowerMaskEffect extends SLEffect<KaledoscopeModel> {
    public static enum MaskMode { PASS, OFF, ON };

    public final EnumParameter<MaskMode> butterflyMaskParam;
    public final EnumParameter<MaskMode> flowerMaskParam;

    public ButterflyFlowerMaskEffect(LX lx) {
        super(lx);

        addParameter(butterflyMaskParam = new EnumParameter<>("ButterflyMask", MaskMode.PASS));
        addParameter(flowerMaskParam = new EnumParameter<>("FlowerMask", MaskMode.PASS));
    }

    @Override
    public void run(double deltaMs, double amount) {
        if (butterflyMaskParam.getEnum() != MaskMode.PASS) {
            int c = LXColor.BLACK;
            if (butterflyMaskParam.getEnum() == MaskMode.ON) {
                c = LXColor.WHITE;
            }

            for (int i = 0; i < KaledoscopeModel.allButterflies.size(); ++i) {
                LUButterfly butterfly = KaledoscopeModel.allButterflies.get(i);
                setColor(butterfly, c);
            }
        }

        if (flowerMaskParam.getEnum() != MaskMode.PASS) {
            int c = LXColor.BLACK;
            if (flowerMaskParam.getEnum() == MaskMode.ON) {
                c = LXColor.WHITE;
            }

            for (int i = 0; i < KaledoscopeModel.allFlowers.size(); ++i) {
                LUFlower flower = KaledoscopeModel.allFlowers.get(i);
                setColor(flower, c);
            }
        }
    }
}
