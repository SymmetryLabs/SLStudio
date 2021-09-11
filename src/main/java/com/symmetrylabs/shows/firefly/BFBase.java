package com.symmetrylabs.shows.firefly;

import art.lookingup.KaledoscopeModel;
import art.lookingup.LUButterfly;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;

import java.util.*;

abstract public class BFBase extends SLPattern {

    protected static final Random random = new Random();
    protected Map<LUButterfly, Integer> randomInts;

    public BFBase(LX lx) {
        super(lx);

    }

    public void onActive() {
        randomInts = new HashMap<LUButterfly, Integer>();
        for (LUButterfly butterfly : KaledoscopeModel.allButterflies) {
            randomInts.put(butterfly, random.nextInt(1000));
        }
    }

    protected int getRandom(LUButterfly butterfly) {
        return randomInts.get(butterfly);
    }

    @Override
    protected void run(double deltaMs) {
        for (LXPoint p : model.points) {
            colors[p.index] = LXColor.rgb(0, 0, 0);
        }
        for (LUButterfly butterfly : KaledoscopeModel.allButterflies) {
            renderButterfly(deltaMs, butterfly, getRandom(butterfly));
        }
    }

    abstract void renderButterfly(double deltaMs, LUButterfly butterfly, int randomInt);
}
