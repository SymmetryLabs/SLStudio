package com.symmetrylabs.slstudio.pattern.texture;

import java.util.Map;
import java.util.HashMap;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import org.apache.commons.math3.util.FastMath;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;

import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;

public abstract class TextureSlideshow extends SLPattern {
    public final CompoundParameter rate = new CompoundParameter("rate", 3000, 10000, 250);
    public final CompoundParameter offsetX = new CompoundParameter("offsetX", 0, -1, 1);
    public final CompoundParameter offsetY = new CompoundParameter("offsetY", 0, -1, 1);
    public final CompoundParameter zoomX = new CompoundParameter("zoomX", 0, 0, 5);
    public final CompoundParameter zoomY = new CompoundParameter("zoomY", 0, 0, 5);
    public final BooleanParameter enableInterp = new BooleanParameter("interp", true);

    private final SawLFO lerp = (SawLFO) startModulator(new SawLFO(0, 1, rate));

    private static Map<String, WeakReference<BufferedImage>> imageCache = new HashMap<>();

    private int imageIndex = 0;
    private final BufferedImage[] images;
    private final int[][] imageLayers;

    private UpdateThread updateThread;

    public TextureSlideshow(LX lx) {
        super(lx);

        String[] paths = getPaths();
        images = new BufferedImage[paths.length];
        for (int i = 0; i < images.length; ++i) {
            String filePath = SLStudio.applet.dataPath(paths[i]);
            //System.out.println("Loading image: " + filePath);

            try {
                BufferedImage image = null;
                synchronized (imageCache) {
                    WeakReference<BufferedImage> imageWeakRef = imageCache.get(filePath);
                    if (imageWeakRef != null) {
                        image = imageWeakRef.get();
                    }
                }

                if (image == null) {
                    image = ImageIO.read(new File(filePath));

                    synchronized (imageCache) {
                        imageCache.put(filePath, new WeakReference(image));
                    }
                }

                images[i] = image;
            }
            catch (IOException e) {
                System.err.println("Error loading image from '" + filePath + "': " + e.getMessage());
            }
        }

        imageLayers = new int[images.length][colors.length];

        addParameter(rate);
        addParameter(offsetX);
        addParameter(offsetY);
        addParameter(zoomX);
        addParameter(zoomY);
        addParameter(enableInterp);

        LXParameterListener updateRastersListener = new LXParameterListener() {
            public void onParameterChanged(LXParameter ignore) {
                triggerUpdate();
            }
        };

        zoomX.addListener(updateRastersListener);
        zoomY.addListener(updateRastersListener);
        offsetX.addListener(updateRastersListener);
        offsetY.addListener(updateRastersListener);
        enableInterp.addListener(updateRastersListener);

        triggerUpdate();
    }

    abstract String[] getPaths();

    private int bilinearInterp(BufferedImage image, double px, double py) {
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();

        int imgOffsX = (int)(offsetX.getValue() * (imageWidth - 1) + imageWidth);
        int imgOffsY = (int)(offsetY.getValue() * (imageHeight - 1) + imageHeight);

        double zoomXValue = zoomX.getValue() + 1;
        double zoomYValue = zoomY.getValue() + 1;

        double imgX = px * (imageWidth - 1) / zoomXValue + imgOffsX;
        double imgY = py * (imageHeight - 1) / zoomYValue + imgOffsY;

        int imgXFloor = (int)FastMath.floor(imgX);
        int imgYFloor = (int)FastMath.floor(imgY);

        double xRem = imgX - imgXFloor;
        double yRem = imgY - imgYFloor;

        imgXFloor %= imageWidth;
        imgYFloor %= imageHeight;

        if (!enableInterp.isOn())
            return image.getRGB(imgXFloor, imgYFloor);

        int imgXCeil = (int)FastMath.ceil(imgX) % imageWidth;
        int imgYCeil = (int)FastMath.ceil(imgY) % imageHeight;

        int q11 = image.getRGB(imgXFloor, imgYFloor);
        int q12 = image.getRGB(imgXFloor, imgYCeil);
        int q21 = image.getRGB(imgXCeil, imgYFloor);
        int q22 = image.getRGB(imgXCeil, imgYCeil);

        int q1 = LXColor.lerp(q11, q21, xRem);
        int q2 = LXColor.lerp(q12, q22, xRem);

        return LXColor.lerp(q1, q2, yRem);
    }

    private class UpdateThread extends Thread {
        public boolean needsUpdate = true;

        @Override
        public void run() {
            while (true) {
                synchronized (TextureSlideshow.this) {
                    if (needsUpdate) {
                        //System.out.println("needsUpdate was reset, unsetting");
                        needsUpdate = false;
                    }
                    else {
                        //System.out.println("updateThread is done, setting to null");
                        updateThread = null;
                        return;
                    }
                }

                updateRasters();
            }
        }
    }

    private synchronized void triggerUpdate() {
        if (updateThread != null) {
            //System.out.println("updateThread found, setting needsUpdate");
            updateThread.needsUpdate = true;
        }
        else {
            //System.out.println("updateThread is null, creating new");
            updateThread = new UpdateThread();
            updateThread.start();
        }
    }

    private void updateRasters() {
        for (int i = 0; i < images.length; ++i) {
            BufferedImage image = images[i];
            if (image == null)
                continue;

            int[] layer = imageLayers[i];

            final BufferedImage imageFinal = image;
            model.getPoints().parallelStream().forEach(p -> {
                double px = (p.x - model.xMin) / model.xRange;
                double py = (p.y - model.yMin) / model.yRange;

                layer[p.index] = bilinearInterp(imageFinal, px, py);
            });
        }
    }

    public void run(double deltaMs) {
        if (images.length == 0)
            return;

        double lerpValue = lerp.getValue();
        if (lerp.loop()) {
            imageIndex = (imageIndex + 1) % images.length;
        }

        int image1Index = imageIndex;
        int image2Index = (imageIndex + 1) % images.length;

        model.getPoints().parallelStream().forEach(p -> {
            int c1 = imageLayers[image1Index][p.index];
            int c2 = imageLayers[image2Index][p.index];

            colors[p.index] = c1 == c2 ? c1 : LXColor.lerp(c1, c2, lerpValue);
        });
    }
}
