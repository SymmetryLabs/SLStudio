package com.symmetrylabs.pattern.texture;

import heronarts.lx.LX;


public class TextureNebula1 extends TextureSlideshow {
    public TextureNebula1(LX lx) {
        super(lx);
    }

    public String[] getPaths() {
        return new String[]{
            "images/apod-ccbysa-171004-soul-herschel.jpg",
        };
    }
}
