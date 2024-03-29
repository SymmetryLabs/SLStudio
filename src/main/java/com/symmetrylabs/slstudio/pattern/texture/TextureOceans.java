package com.symmetrylabs.slstudio.pattern.texture;

import heronarts.lx.LX;


public class TextureOceans extends TextureSlideshow {
    public TextureOceans(LX lx) {
        super(lx);
    }

    public String[] getPaths() {
        return new String[]{
            "images/ocean1.jpeg",
            "images/ocean2.jpeg",
            "images/ocean3.jpeg",
            "images/ocean4.jpeg"
        };
    }
}
