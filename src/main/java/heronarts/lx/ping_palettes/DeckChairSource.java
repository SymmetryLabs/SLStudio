package heronarts.lx.ping_palettes;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

/**
 * A source that fetches photos from a deckchair.com camera.  Good places to find camera IDs are
 * http://api.deckchair.com/v1/cameras and @webcam_sunsets.
 */
class DeckChairSource implements ImageSource {
	public final String id;

	DeckChairSource(String id) {
		this.id = id;
	}

	public BufferedImage fetchImage() throws IOException {
		String url = "http://api.deckchair.com/v1/viewer/camera/" + id +
			"?width=960&height=540&format=png&panelMode=false";
		System.out.println("Fetching image from " + url);
		return ImageIO.read(new URL(url));
	}
}
