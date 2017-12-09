package com.symmetrylabs.palettes;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * An external source of photo images.
 */
interface ImageSource {
    public BufferedImage fetchImage() throws IOException;
}
