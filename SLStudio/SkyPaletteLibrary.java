package com.symmetrylabs.palette;

/**
 * Generates palettes based on images of the sky from webcams around the world.
 */

import java.util.Map;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Date;
import java.net.URL;
import java.io.IOException;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import heronarts.lx.color.LXColor;

/** A collection of named palettes drawn from various sky photo sources. */
public class SkyPaletteLibrary {
    private Map<String, SkyPhotoSource> sources;
    private Map<String, PaletteExtractor> extractors;
    private Map<String, Long> lastFetchMillis;
    private Map<String, ColorPalette> palettes;

    private final int CACHE_TTL_SEC = 60;  // length of time to cache retrieved photos

    private static SkyPaletteLibrary instance = null;
    public static synchronized SkyPaletteLibrary getInstance() {
        if (instance == null) {
            instance = new SkyPaletteLibrary();
        }

        return instance;
    }

    public SkyPaletteLibrary() {
        sources = new HashMap<String, SkyPhotoSource>();
        extractors = new HashMap<String, PaletteExtractor>();
        lastFetchMillis = new HashMap<String, Long>();
        palettes = new HashMap<String, ColorPalette>();
    }
    
    public String[] getNames() {
        SortedSet<String> names = new TreeSet<String>();
        for (String name : sources.keySet()) {
            names.add(name);
        }
        return names.toArray(new String[0]);
    }

    public void addSky(String name, SkyPhotoSource source, PaletteExtractor extractor) {
        sources.put(name, source);
        extractors.put(name, extractor);
    }

    public ColorPalette getPalette(String name) {
        long nowMillis = new Date().getTime();
        long cacheAge = (nowMillis - lastFetchMillis.getOrDefault(name, 0L))/1000;
        ColorPalette palette = palettes.get(name);
        if (palette == null || cacheAge >= CACHE_TTL_SEC) {
            new PaletteUpdateThread(name).start();
        }
        return palette != null ? palette : new ConstantPalette(0);
    }    

    private class PaletteUpdateThread extends Thread {
        String name;

        PaletteUpdateThread(String name) {
            this.name = name;
        }

        public void run() {
            SkyPhotoSource source = sources.get(name);
            PaletteExtractor extractor = extractors.get(name);
            if (source == null || extractor == null) return;
            synchronized (source) {
                long nowMillis = new Date().getTime();
                long cacheAge = (nowMillis - lastFetchMillis.getOrDefault(name, 0L))/1000;
                if (cacheAge < CACHE_TTL_SEC) {
                    return;
                }
                try {
                    System.out.println("Updating \"" + name + "\" sky palette.");
                    BufferedImage image = source.fetchImage();
                    System.out.println("Fetched " + image.getWidth() + " x " + image.getHeight() + " photo for \"" + name + "\" sky palette.");
                    palettes.put(name, extractor.getPalette(image));
                    lastFetchMillis.put(name, new Date().getTime());
                } catch (IOException e) {
                    System.out.println("Failed to fetch \"%s\" sky photo " + name);
                }
            }
        }
    }

    /** A source of sky photos from a public webcam. */
    public interface SkyPhotoSource {
        public BufferedImage fetchImage() throws IOException;
    }

    public static class DeckChairSource implements SkyPhotoSource {
        public final String id;

        /** Sets up a deckchair.com camera.  Good places to find camera IDs are
         * http://api.deckchair.com/v1/cameras and @webcam_sunsets.
         */
        public DeckChairSource(String id) {
            this.id = id;
        }

        public BufferedImage fetchImage() throws IOException {
            String url = "http://api.deckchair.com/v1/viewer/camera/" + id +
                "?width=960&height=540&format=png&panelMode=false";
            System.out.println("Fetching sky image from " + url);
            return ImageIO.read(new URL(url));
        }
    }

    public static class UrlImageSource implements SkyPhotoSource {
        public final String url;

        public UrlImageSource(String url) {
            this.url = url;
        }

        public BufferedImage fetchImage() throws IOException {
            System.out.println("Fetching sky image from " + url);
            return ImageIO.read(new URL(url));
        }
    }

    public interface ColorPalette {
        public int getColor(double p);  // p is a parameter between 0 and 1
    }

    public interface PaletteExtractor {
        public ColorPalette getPalette(BufferedImage image);
    }

    public static class ConstantPalette implements ColorPalette {
        private final int c;
        
        public ConstantPalette(int c) {
            this.c = c;
        }
        
        public int getColor(double p) {
            return c;
        }
    }

    /** A palette the samples from an array of color values. */
    public static class ArrayPalette implements ColorPalette {
        private int[] colors;

        public ArrayPalette(int[] colors) {
            this.colors = colors;
        }

        public int getColor(double p) {
            p = Math.abs(p);
            int floor = (int) Math.floor(p);
            p = p - floor;
            if (floor % 2 == 1) {
                p = 1 - p;
            }
            
            double index = p * (colors.length - 1);
            int low = (int) Math.floor(index);
            int high = (low + 1) < colors.length ? low + 1 : low;
            double fraction = index - low;    
            return LXColor.lerp(colors[low], colors[high], fraction);
        }
    }

    /** Extracts a palette from an image by sampling colours along an arc that
        * rises from a point on the left edge of the image, to the center of the
        * top edge, falling to a point on the right edge of the image.  The "height"
        * parameter specifies how low to start at the left edge and end at the right
        * edge, as a fraction (e.g. height = 0.25 means a quarter of the way down).
        */
    public static class ArcPaletteExtractor implements PaletteExtractor {
        private float height;
        private int numStops;

        public ArcPaletteExtractor(float height, int numStops) {
            this.height = height;  // a fraction of the image's height, from 0 to 1
            this.numStops = numStops;  // number of colour stops to sample from the image
        }

        public ColorPalette getPalette(BufferedImage image) {
            int[] colors = new int[numStops + 1];

            double xMax = image.getWidth() - 1;
            double yMax = image.getHeight() - 1;
            for (int i = 0; i <= numStops; i++) {
                double t = i / (double) numStops;
                double x = xMax * t;
                double y = yMax * height * (0.5 + Math.cos(2 * Math.PI * t) * 0.5);

                int xl = (int) Math.floor(x);
                int yl = (int) Math.floor(y);
                int xh = (xl + 1) <= xMax ? xl + 1 : xl;
                int yh = (yl + 1) <= yMax ? yl + 1 : yl;

                int nw = image.getRGB(xl, yl);
                int ne = image.getRGB(xh, yl);
                int sw = image.getRGB(xl, yh);
                int se = image.getRGB(xh, yh);

                int n = LXColor.lerp(nw, ne, x - xl);
                int s = LXColor.lerp(sw, se, x - xl);

                colors[i] = LXColor.lerp(n, s, y - yl);
            }

            return new ArrayPalette(colors);
        }
    }
}
