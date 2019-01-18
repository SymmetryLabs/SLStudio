package heronarts.lx.ping_palettes;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

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
        String filepath = new File(baseDir, filename).toString();
        try {
            InputStream s = getClass().getClassLoader().getResourceAsStream(filepath);
            if (s != null)
                return ImageIO.read(s);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.err.println("Could not load image '" + filepath + "'.");

        return null;
    }
}
