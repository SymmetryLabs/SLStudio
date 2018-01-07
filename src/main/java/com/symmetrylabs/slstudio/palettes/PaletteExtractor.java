package com.symmetrylabs.slstudio.palettes;

import java.awt.image.BufferedImage;

/**
 * A thing that generates a palette based on an image.
 */
public interface PaletteExtractor {
    ColorPalette getPalette(BufferedImage image);
}
