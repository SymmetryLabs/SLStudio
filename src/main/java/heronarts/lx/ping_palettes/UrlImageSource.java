package heronarts.lx.ping_palettes;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

/**
 * A source that fetches images from a public URL.
 */
class UrlImageSource implements ImageSource {
    public final String url;

    UrlImageSource(String url) {
        this.url = url;
    }

    public BufferedImage fetchImage() throws IOException {
        System.out.println("Fetching image from " + url);
        return ImageIO.read(new URL(url));
    }
}
