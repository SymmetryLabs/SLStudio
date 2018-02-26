package com.symmetrylabs.slstudio.palettes;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * A collection of named palettes (either com.symmetrylabs.slstudio.palettes.ColorPalette instances or palettes extracted from
 * images by PaletteExtractors).
 */
public class PaletteLibrary {
    Map<String, ColorPalette> palettes;
    Map<String, ImageSource> sources;
    Map<String, PaletteExtractor> extractors;
    Map<String, Long> lastFetchMillis;
    Map<String, Thread> threads;

    final int CACHE_TTL_SEC = 60;  // length of time to cache retrieved photos
    final ColorPalette BLACK = new ConstantPalette(0);

    public PaletteLibrary() {
        palettes = new HashMap<String, ColorPalette>();
        sources = new HashMap<String, ImageSource>();
        extractors = new HashMap<String, PaletteExtractor>();
        lastFetchMillis = new HashMap<String, Long>();
        threads = new HashMap<String, Thread>();
    }

    public String[] getNames() {
        String[] names = palettes.keySet().toArray(new String[0]);
        Arrays.sort(names);
        return names;
    }

    public void put(String name, ColorPalette palette) {
        palettes.put(name, palette);
    }

    void put(String name, ImageSource source, PaletteExtractor extractor) {
        palettes.put(name, BLACK);
        sources.put(name, source);
        extractors.put(name, extractor);
        lastFetchMillis.put(name, 0L);
    }

    public ColorPalette get(String name) {
        if (sources.containsKey(name)) {
            long nowMillis = new Date().getTime();
            long cacheAge = (nowMillis - lastFetchMillis.get(name)) / 1000;
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
                    long cacheAge = (nowMillis - lastFetchMillis.get(name)) / 1000;
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
