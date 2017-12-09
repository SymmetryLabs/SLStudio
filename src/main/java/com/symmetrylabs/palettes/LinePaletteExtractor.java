package com.symmetrylabs.palettes;

import java.awt.image.BufferedImage;

/**
 * Extracts a palette from an image by sampling colours along a line.
 */
public class LinePaletteExtractor extends ImageUtils implements PaletteExtractor {
    double x0;
    double y0;
    double x1;
    double y1;
    int numStops;

    LinePaletteExtractor(double x0, double y0, double x1, double y1, int numStops) {
        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
        this.y1 = y1;
        this.numStops = numStops;  // number of stops to sample from the image
    }

    public LinePaletteExtractor(double x0, double y0, double x1, double y1) {
        this(x0, y0, x1, y1, 100);
    }

    public LinePaletteExtractor(double y) {
        this(0, y, 1, y);
    }

    public ColorPalette getPalette(BufferedImage image) {
        if (image == null) return new ConstantPalette(0);

        int[] colors = new int[numStops + 1];
        for (int i = 0; i <= numStops; i++) {
            double t = i / (double) numStops;
            colors[i] = getImageColor(image, x0 + (x1 - x0) * t, y0 + (y1 - y0) * t);
        }
        return new ArrayPalette(colors);
    }
}
