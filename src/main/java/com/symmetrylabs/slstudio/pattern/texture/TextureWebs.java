package com.symmetrylabs.slstudio.pattern.texture;

import heronarts.lx.LX;

public class TextureWebs extends TextureSlideshow {
    public TextureWebs(LX lx) {
        super(lx);
    }

    public String[] getPaths() {
        return new String[]{
            "images/green_web.png",
        };
    }
}
