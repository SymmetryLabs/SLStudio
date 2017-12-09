package com.symmetrylabs.pattern.texture;

import heronarts.lx.LX;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
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
