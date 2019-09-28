package heronarts.lx.ping_palettes;

import heronarts.lx.color.LXColor;

import java.awt.image.BufferedImage;

/**
 * Helper methods for extracting colors from images.
 */
abstract class ImageUtils {
	/**
	 * Extracts a color from an image, given coordinates x and y from 0 to 1.
	 */
	int getImageColor(BufferedImage image, double x, double y) {
		int xMax = image.getWidth() - 1;
		int yMax = image.getHeight() - 1;
		double px = clamp(x, 0, 1) * xMax;
		double py = clamp(y, 0, 1) * yMax;
		int xl = (int) Math.floor(px);
		int yl = (int) Math.floor(py);
		int xh = (xl + 1) <= xMax ? (xl + 1) : xl;
		int yh = (yl + 1) <= yMax ? (yl + 1) : yl;
		int nw = image.getRGB(xl, yl);
		int ne = image.getRGB(xh, yl);
		int sw = image.getRGB(xl, yh);
		int se = image.getRGB(xh, yh);
		int n = LXColor.lerp(nw, ne, px - xl);
		int s = LXColor.lerp(sw, se, px - xl);
		return LXColor.lerp(n, s, py - yl);
	}

	double clamp(double value, double low, double high) {
		return Math.min(Math.max(value, low), high);
	}
}
