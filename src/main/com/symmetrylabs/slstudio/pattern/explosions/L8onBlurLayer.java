package com.symmetrylabs.slstudio.pattern.explosions;

import heronarts.lx.LX;
import heronarts.lx.LXDeviceComponent;
import heronarts.lx.LXLayer;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.BoundedParameter;

public class L8onBlurLayer extends LXLayer {
        public final BoundedParameter amount;
        private final int[] blurBuffer;

        public L8onBlurLayer(LX lx, LXDeviceComponent pattern) {
                this(lx, pattern, new BoundedParameter("BLUR", 0));
        }

        public L8onBlurLayer(LX lx, LXDeviceComponent pattern, BoundedParameter amount) {
                super(lx, pattern);
                this.amount = amount;
                this.blurBuffer = new int[lx.total];

                for (int i = 0; i < blurBuffer.length; ++i) {
                        this.blurBuffer[i] = 0xff000000;
                }
        }

        public void run(double deltaMs) {
                float blurf = this.amount.getValuef();
                if (blurf > 0) {
                        blurf = 1 - (1 - blurf) * (1 - blurf) * (1 - blurf);
                        for (int i = 0; i < this.colors.length; ++i) {
                                int blend = LXColor.screen(this.colors[i], this.blurBuffer[i]);
                                this.colors[i] = LXColor.lerp(this.colors[i], blend, blurf);
                        }
                }
                for (int i = 0; i < this.colors.length; ++i) {
                        this.blurBuffer[i] = this.colors[i];
                }
        }
}
