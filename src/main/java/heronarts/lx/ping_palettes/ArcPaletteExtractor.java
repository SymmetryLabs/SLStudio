package heronarts.lx.ping_palettes;// The com.symmetrylabs.slstudio.palettes.ColorPalette interface and various implementations that generate or
// extract palettes for use in generative patterns.  (It would have been
// named simply com.symmetrylabs.slstudio.pattern.Palette, but there is already a com.symmetrylabs.slstudio.pattern.Palette class in Patterns.pde.)

import java.awt.image.BufferedImage;

/**
 * Extracts a palette from an image by sampling colours along an arc that rises from a point on the left edge of the
 * image, to the center of the top edge, falling to a point on the right edge of the image.  The "height" parameter
 * specifies how low to start at the left edge and end at the right edge, as a fraction (e.g. height = 0.25 means a
 * quarter of the way down).
 */
public class ArcPaletteExtractor extends ImageUtils implements PaletteExtractor {
    float height;
    int numStops;

    ArcPaletteExtractor(float height, int numStops) {
        this.height = height;  // a fraction of the image's height, from 0 to 1
        this.numStops = numStops;  // number of stops to sample from the image
    }

    ArcPaletteExtractor(float height) {
        this(height, 100);
    }

    public ColorPalette getPalette(BufferedImage image) {
        if (image == null) return new ConstantPalette(0);

        int[] colors = new int[numStops + 1];
        for (int i = 0; i <= numStops; i++) {
            double t = i / (double) numStops;
            colors[i] = getImageColor(
                image, t, height * (0.5 + Math.cos(2 * Math.PI * t) * 0.5));
        }
        return new ArrayPalette(colors);
    }
}

