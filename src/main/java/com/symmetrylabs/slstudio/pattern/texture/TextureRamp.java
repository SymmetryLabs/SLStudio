package com.symmetrylabs.slstudio.pattern.texture;

import heronarts.lx.LX;


public class TextureRamp extends TextureSlideshow {
    public TextureRamp(LX lx) {
        super(lx);
    }

    public String[] getPaths() {
        return new String[]{
            "images/BW_Ramp.jpg",
        };
    }
}

