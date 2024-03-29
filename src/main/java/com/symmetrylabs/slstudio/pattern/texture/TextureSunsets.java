package com.symmetrylabs.slstudio.pattern.texture;

import heronarts.lx.LX;


public class TextureSunsets extends TextureSlideshow {
    public TextureSunsets(LX lx) {
        super(lx);
    }

    public String[] getPaths() {
        return new String[]{
            "images/sunset1.jpeg",
            "images/sunset2.jpeg",
            "images/sunset3.jpeg",
            "images/sunset4.jpeg",
            "images/sunset5.jpeg",
            "images/sunset6.jpeg"
        };
    }
}
