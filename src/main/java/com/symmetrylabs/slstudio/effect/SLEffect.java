package com.symmetrylabs.slstudio.effect;

import heronarts.lx.LX;
import heronarts.lx.LXEffect;

import com.symmetrylabs.slstudio.SLStudioLX;

public abstract class SLEffect extends LXEffect {
    protected final SLStudioLX lx;

    public SLEffect(LX lx) {
        super(lx);

        this.lx = (SLStudioLX)lx;
    }
}
