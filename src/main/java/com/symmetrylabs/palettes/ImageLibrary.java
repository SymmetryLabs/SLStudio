package com.symmetrylabs.palettes;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Loads images from a directory.
 */
public class ImageLibrary {
    File dir;

    public ImageLibrary(String path) {
        dir = new File(path);
    }

    public BufferedImage get(String filename) {
        try {
            return ImageIO.read(new File(dir, filename));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
