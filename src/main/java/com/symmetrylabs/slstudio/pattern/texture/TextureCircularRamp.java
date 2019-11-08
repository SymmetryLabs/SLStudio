package com.symmetrylabs.slstudio.pattern.texture;

import heronarts.lx.LX;


public class TextureCircularRamp extends TextureSlideshow {
    public TextureCircularRamp(LX lx) {
        super(lx);
    }

    public String[] getPaths() {
        return new String[]{
            "images/BW_Ramp_circular.jpg",
        };
    }
}