package com.symmetrylabs.pattern.texture;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
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
