package com.symmetrylabs.slstudio.palettes;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.File;
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
        File f = new File(baseDir, filename);
        String filepath = f.toString();
        try {
            InputStream s = getClass().getClassLoader().getResourceAsStream(filepath);
            if (s != null)
                return ImageIO.read(s);
        } catch (IOException e) {
            e.printStackTrace();
        }

        /* HACK: our sourceless windows build isn't loading image files from
           resources for some reason. This is a stopgap until I can fix
           resource loading. */
        f = new File("src/main/resources/" + baseDir, filename);
        if (f.exists()) {
            try {
                BufferedImage img = ImageIO.read(f);
                System.err.println("using windows resource hack to load image without resources");
                return img;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.err.println("Could not load image '" + filepath + "'.");

        return null;
    }
}
