// The ColorPalette interface and various implementations that generate or
// extract palettes for use in generative patterns.  (It would have been
// named simply Palette, but there is already a Palette class in Patterns.pde.)

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

import heronarts.lx.color.LXColor;

/** A ColorPalette is a thing that assigns a color value to every real number.
    * Palettes are typically designed to generate all their interesting values
    * between 0 and 1; however, implementations must accept any value of p.
    * For example, in a typical linear gradient palette, the gradient would
    * run from p = 0 to p = 1, and then it might be a constant for all p <= 0
    * and a constant for all p >= 1, or it might repeat the gradient from
    * p = 1 to p = 2, from p = 2 to p = 3, and so on, or it might zigzag back
    * and forth to avoid creating sharp jumps at integer values of p.
    *
    * getColor returns an integer 0xAARRGGBB value, and is responsible for making
    * sure that the top byte is a reasonable alpha value (usually 0xff).
    */
interface ColorPalette {
    int getColor(double p);
}

/** An external source of photo images. */
interface ImageSource {
    public BufferedImage fetchImage() throws IOException;
}

/** A thing that generates a palette based on an image. */
interface PaletteExtractor {
    ColorPalette getPalette(BufferedImage image);
}

/** Loads images from a directory. */
class ImageLibrary {
    File dir;
    
    ImageLibrary(String path) {
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

/** A source that fetches photos from a deckchair.com camera.  Good places to
    * find camera IDs are http://api.deckchair.com/v1/cameras and @webcam_sunsets.
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

/** A source that fetches images from a public URL. */
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

/** A palette that returns a constant color. */
class ConstantPalette implements ColorPalette {
    final int c;

    ConstantPalette(int c) {
        this.c = 0xff000000 | c;
    }

    public int getColor(double p) {
        return c;
    }
}

/** A palette that samples from an array of color values, interpolating between
    * adjacent values in the array.  By default, the palette extends over the
    * entire array, but can be pointed at an adjustable subrange of the array.
    */
class ArrayPalette implements ColorPalette {
    int[] colors;
    double start;
    double stop;
    double scale;

    ArrayPalette(int[] colors) {
        this.colors = new int[colors.length];
        for (int i = 0; i < colors.length; i++) {
            this.colors[i] = 0xff000000 | colors[i];
        }
        this.start = 0;
        this.stop = 1;
        this.scale = colors.length - 1;
    }

    double clamp(double value, double low, double high) {
        return Math.min(Math.max(value, low), high);
    }

    public void setStart(double start) {
        this.start = clamp(start, 0, 1);
    }

    public void setStop(double stop) {
        this.stop = clamp(stop, 0, 1);
    }

    public int getColor(double p) {
        double index = (start + clamp(p, 0, 1) * (stop - start)) * (colors.length - 1);
        int low = (int) Math.floor(index);
        int high = (low + 1) < colors.length ? low + 1 : low;
        return LXColor.lerp(colors[low], colors[high], index - low);
    }
}

/** A palette that repeats the colors from the [0, 1] range of another palette
    * in a zigzag pattern over the number line: in forward order from 0 to 1,
    * then reverse order from 1 to 2, then forward order from 2 to 3, etc.
    * A bias factor can be applied, which shifts the output more toward the 0 end
    * or the 1 end of the palette, while keeping p = 0 at 0 and p = 1 at 1.
    * A bias of -5 causes the output to spend most of its time near 0 (from p = 0
    * to about p = 0.95), and then shoot quickly up to 1.  A bias of +5 causes the
    * output to shoot quickly up to 0.95 and then spend most of its time near 1.
    */
class ZigzagPalette implements ColorPalette {
    ColorPalette palette;
    double exponent;

    ZigzagPalette(ColorPalette palette) {
        this.palette = palette;
        this.exponent = 1;
    }

    ZigzagPalette(int[] colors) {
        this(new ArrayPalette(colors));
    }

    public void setStart(double start) {
        if (palette instanceof ArrayPalette) {
            ((ArrayPalette) palette).setStart(start);
        }
    }

    public void setStop(double stop) {
        if (palette instanceof ArrayPalette) {
            ((ArrayPalette) palette).setStop(stop);
        }
    }

    // A negative bias shifts the output toward 0; positive bias shifts toward 1.
    public void setBias(double bias) {
        exponent = Math.exp(bias);
    }

    public int getColor(double p) {
        int floor = (int) Math.floor(p);
        p -= floor;
        if (floor % 2 != 0) {
            p = 1 - p;
        }
        return palette.getColor(Math.pow(p, exponent));
    }
}

/** Helper methods for extracting colors from images. */
abstract class ImageUtils {
    /** Extracts a color from an image, given coordinates x and y from 0 to 1. */
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

/** Extracts a palette from an image by sampling colours along a line. */
class LinePaletteExtractor extends ImageUtils implements PaletteExtractor {
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

    LinePaletteExtractor(double x0, double y0, double x1, double y1) {
        this(x0, y0, x1, y1, 100);
    }

    LinePaletteExtractor(double y) {
        this(0, y, 1, y);
    }

    public ColorPalette getPalette(BufferedImage image) {
        if (image == null) return new ConstantPalette(0);

        int[] colors = new int[numStops + 1];
        for (int i = 0; i <= numStops; i++) {
            double t = i / (double) numStops;
            colors[i] = getImageColor(image, x0 + (x1 - x0)*t, y0 + (y1 - y0)*t);
        }
        return new ZigzagPalette(colors);
    }
}

/** Extracts a palette from an image by sampling colours along an arc that
    * rises from a point on the left edge of the image, to the center of the
    * top edge, falling to a point on the right edge of the image.  The "height"
    * parameter specifies how low to start at the left edge and end at the right
    * edge, as a fraction (e.g. height = 0.25 means a quarter of the way down).
    */
class ArcPaletteExtractor extends ImageUtils implements PaletteExtractor {
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
        return new ZigzagPalette(colors);
    }
}

/** A collection of named palettes (either ColorPalette instances or palettes
    * extracted from images by PaletteExtractors).
    */
class PaletteLibrary {
    Map<String, ColorPalette> palettes;
    Map<String, ImageSource> sources;
    Map<String, PaletteExtractor> extractors;
    Map<String, Long> lastFetchMillis;
    Map<String, Thread> threads;

    final int CACHE_TTL_SEC = 60;  // length of time to cache retrieved photos
    final ColorPalette BLACK = new ConstantPalette(0);

    PaletteLibrary() {
        palettes = new HashMap<String, ColorPalette>();
        sources = new HashMap<String, ImageSource>();
        extractors = new HashMap<String, PaletteExtractor>();
        lastFetchMillis = new HashMap<String, Long>();
        threads = new HashMap<String, Thread>();
    }

    String[] getNames() {
        String[] names = palettes.keySet().toArray(new String[0]);
        Arrays.sort(names);
        return names;
    }

    void put(String name, ColorPalette palette) {
        palettes.put(name, palette);
    }

    void put(String name, ImageSource source, PaletteExtractor extractor) {
        palettes.put(name, BLACK);
        sources.put(name, source);
        extractors.put(name, extractor);
        lastFetchMillis.put(name, 0L);
    }

    ColorPalette get(String name) {
        if (sources.containsKey(name)) {
            long nowMillis = new Date().getTime();
            long cacheAge = (nowMillis - lastFetchMillis.get(name))/1000;
            if (cacheAge >= CACHE_TTL_SEC) {
                Thread thread = new PaletteRefreshThread(name);
                if (!threads.containsKey(name)) {
                    threads.put(name, thread);
                    thread.start();
                }
            }
        }
        return palettes.get(name);
    }

    class PaletteRefreshThread extends Thread {
        String name;

        PaletteRefreshThread(String name) {
            this.name = name;
        }

        public void run() {  // fetches a source image and updates a palette, once
            ImageSource source = sources.get(name);
            PaletteExtractor extractor = extractors.get(name);
            if (source != null && extractor != null) {
                synchronized (source) {
                    long nowMillis = new Date().getTime();
                    long cacheAge = (nowMillis - lastFetchMillis.get(name))/1000;
                    if (cacheAge >= CACHE_TTL_SEC) {
                        try {
                            System.out.println("Updating \"" + name + "\" palette.");
                            BufferedImage image = source.fetchImage();
                            System.out.println(
                                    "Fetched " + image.getWidth() + " x " + image.getHeight() +
                                    " image for \"" + name + "\" palette.");
                            palettes.put(name, extractor.getPalette(image));
                            lastFetchMillis.put(name, new Date().getTime());
                        } catch (IOException e) {
                            System.out.println("Failed to fetch image for \"" + name + "\" palette");
                        }
                    }
                }
            }
            threads.remove(name);
        }
    }
}