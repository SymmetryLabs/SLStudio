package com.symmetrylabs.slstudio.pattern.texture;

import heronarts.lx.LX;


public class TextureClouds extends TextureSlideshow {
    public TextureClouds(LX lx) {
        super(lx);
    }

    public String[] getPaths() {
        return new String[]{
            "images/clouds1.jpeg",
            "images/clouds2.jpeg",
            "images/clouds3.jpeg"

        };
    }
}
