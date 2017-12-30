package com.symmetrylabs.slstudio.effect;

import com.symmetrylabs.slstudio.model.SunsModel;
import heronarts.lx.LX;
import heronarts.lx.LXEffect;

public abstract class SLEffect extends LXEffect {

        public final SunsModel model;

        public SLEffect(LX lx) {
                super(lx);
                this.model = (SunsModel) lx.model;
        }

}
