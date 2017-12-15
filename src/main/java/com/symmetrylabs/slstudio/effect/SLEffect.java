package com.symmetrylabs.slstudio.effect;

import com.symmetrylabs.slstudio.model.SLModel;
import heronarts.lx.LX;
import heronarts.lx.LXEffect;

public abstract class SLEffect extends LXEffect {

        public final SLModel model;

        public SLEffect(LX lx) {
                super(lx);
                this.model = (SLModel) lx.model;
        }

}
