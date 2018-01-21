package com.symmetrylabs.slstudio.palettes;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.IOException;

/**
 * Loads images from a directory.
 */
public class ImageLibrary {
    private final String baseDir;

    public ImageLibrary() {
        this("");
    }

    public ImageLibrary(String path) {
        baseDir = path;
    }

    public BufferedImage get(String filename) {
        try {
            InputStream s = getClass().getResourceAsStream(baseDir + filename);
            if (s != null)
                return ImageIO.read(s);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
