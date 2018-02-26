package com.symmetrylabs.slstudio.pattern.texture;

import heronarts.lx.LX;


public class TextureCorals extends TextureSlideshow {
    public TextureCorals(LX lx) {
        super(lx);
    }

    public String[] getPaths() {
        return new String[]{
            "images/coral1.jpeg",
            "images/coral2.jpeg",
            "images/coral3.jpeg",
            "images/coral4.jpeg",
            "images/coral5.jpeg",
        };
    }
}
