package com.symmetrylabs.pattern.raven;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.symmetrylabs.pattern.SLPattern;
import com.symmetrylabs.util.DrawHelper;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.p3lx.P3LX;
import processing.core.PGraphics;
import processing.core.PVector;
import sun.security.provider.Sun;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static processing.core.PConstants.P3D;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public abstract class P3CubeMapPattern extends SLPattern {
    private PGraphics pg;
    protected PGraphics pgF, pgB, pgL, pgR, pgU, pgD;
    final PVector origin;
    final PVector bboxSize;
    protected int faceRes;

    private final String id = "" + Math.random();

    DiscreteParameter resParam = discreteParameter("RES", 200, 64, 512);
    DiscreteParameter kernelSize = discreteParameter("KER", 1, 1, 6);
    BooleanParameter allSunsParams = booleanParam("ALL", false);
    List<BooleanParameter> sunSwitchParams = Lists.newArrayList();

    /**
     * A pattern that projects a cubemap image onto all the LEDs inside a given bounding box in world space.  The cubemap
     * image should have resolution 4k x 3k, where each face of the cube takes up a k x k square, and the six faces are
     * arranged thus (where R/L are +x/-x of the origin, U/D are +y/-y, F/B are -z/+z): +---+ | U | +---+---+---+---+ | L
     * | F | R | B | +---+---+---+---+ | D | +---+ Note that the +z side is the back (B), not the front (F) as you might
     * expect, because Processing, insanely, uses a left-handed coordinate system.
     *
     * @param lx
     *     The global P3LX object.
     * @param origin
     *     The center of the bounding box in world space.
     * @param bboxSize
     *     The length, width, and height of the bounding box in world space.
     * @param defaultFaceRes
     *     The width and height, k, in pixels of one square face of the cubemap image, which will have total width 4k and
     *     total height 3k.
     */
    protected P3CubeMapPattern(P3LX lx, PVector origin, PVector bboxSize, int defaultFaceRes) {
        super(lx);

        resParam.setValue(defaultFaceRes);
        resParam.addListener(new LXParameterListener() {
            @Override
            public void onParameterChanged(final LXParameter lxParameter) {
                faceRes = (int) resParam.getValue();
                updateGraphics();
                invalidateProjectionCache();
            }
        });

        final LXParameterListener invalidateProjectionCache = new LXParameterListener() {
            @Override
            public void onParameterChanged(final LXParameter lxParameter) {
                invalidateProjectionCache();
            }
        };
        kernelSize.addListener(invalidateProjectionCache);
        allSunsParams.addListener(invalidateProjectionCache);

        this.faceRes = defaultFaceRes;
        this.updateGraphics();

        this.origin = origin;
        this.bboxSize = bboxSize;

        for (final Sun sun : model.suns) {
            sunSwitchParams.add(booleanParam("SUN" + (model.suns.indexOf(sun) + 1), true));
        }
    }

    private void updateGraphics() {
        P3LX lx = (P3LX) this.lx;
        this.pg = lx.applet.createGraphics(faceRes * 4, faceRes * 3, P3D);
        this.pgF = lx.applet.createGraphics(faceRes, faceRes, P3D);
        this.pgB = lx.applet.createGraphics(faceRes, faceRes, P3D);
        this.pgL = lx.applet.createGraphics(faceRes, faceRes, P3D);
        this.pgR = lx.applet.createGraphics(faceRes, faceRes, P3D);
        this.pgU = lx.applet.createGraphics(faceRes, faceRes, P3D);
        this.pgD = lx.applet.createGraphics(faceRes, faceRes, P3D);
    }

    PVector originForSun(final Sun sun) {
        return new PVector(
            sun.boundingBox.origin.x + sun.boundingBox.size.x * .5f,
            sun.boundingBox.origin.y + sun.boundingBox.size.y * .5f,
            sun.boundingBox.origin.z + sun.boundingBox.size.z * .5f
        );
    }

    PVector bboxForSun(final Sun sun) {
        return sun.boundingBox.size;
    }

    Map<Sun, int[][]> perSunProjectionCache;
    int[][] allProjectionCache;

    private void invalidateProjectionCache() {
        perSunProjectionCache = null;
        allProjectionCache = null;
    }

    private synchronized void ensureProjectionCache() {
        int inputPointCount = kernelSize.getValuei() * kernelSize.getValuei();

        if (allSunsParams.getValueb()) {
            if (allProjectionCache == null) {
                allProjectionCache = new int[model.points.length][inputPointCount];
                computeCache(allProjectionCache, origin, bboxSize, model.points);
            }
        } else {
            if (perSunProjectionCache == null) {
                perSunProjectionCache = Maps.newHashMapWithExpectedSize(model.suns.size());

                for (final Sun sun : model.suns) {
                    int[][] sunCache = new int[sun.points.length][inputPointCount];
                    perSunProjectionCache.put(sun, sunCache);

                    computeCache(sunCache, originForSun(sun), bboxForSun(sun), sun.points);
                }
            }
        }
    }

    private void computeCache(
        final int[][] cache,
        PVector origin,
        PVector bboxSize,
        final LXPoint[] points
    ) {
        int kernelSize = this.kernelSize.getValuei();

        for (int i = 0; i < points.length; i++) {
            LXPoint p = points[i];
            PVector v = new PVector(p.x, p.y, p.z).sub(origin);
            double ax = Math.abs(v.x);
            double ay = Math.abs(v.y);
            double az = Math.abs(v.z);

            // Ignore pixels outside the bounding box.
            if (ax > bboxSize.x / 2 || ay > bboxSize.y / 2 || az > bboxSize.z / 2) {
                Arrays.fill(cache[i], -1);
                continue;
            }

            // Avoid division by zero.
            if (ax == 0 && ay == 0 && az == 0) {
                Arrays.fill(cache[i], -1);
                continue;
            }

            // Select the face according to the component with the largest absolute value.
            if (ax > ay && ax > az) {
                if (v.x > 0) {  // Right face
                    computePointColorIndexes(2 * faceRes, faceRes, v.z / ax, -v.y / ax, kernelSize, cache[i]);
                } else {  // Left face
                    computePointColorIndexes(0, faceRes, -v.z / ax, -v.y / ax, kernelSize, cache[i]);
                }
            } else if (ay > ax && ay > az) {
                if (v.y > 0) {  // Up face
                    computePointColorIndexes(faceRes, 0, v.x / ay, -v.z / ay, kernelSize, cache[i]);
                } else {  // Down face
                    computePointColorIndexes(faceRes, 2 * faceRes, v.x / ay, v.z / ay, kernelSize, cache[i]);
                }
            } else {
                if (v.z > 0) {  // Back face
                    computePointColorIndexes(3 * faceRes, faceRes, -v.x / az, -v.y / az, kernelSize, cache[i]);
                } else {  // Front face
                    computePointColorIndexes(faceRes, faceRes, v.x / az, -v.y / az, kernelSize, cache[i]);
                }
            }
        }
    }

    private void projectToLeds() {
        ensureProjectionCache();

        if (allSunsParams.getValueb()) {
            projectToLeds(allProjectionCache, model.points);
        } else {
            model.suns.parallelStream().forEach(new Consumer<Sun>() {
                @Override
                public void accept(final Sun sun) {
                    final int sunIndex = model.suns.indexOf(sun);
                    PVector origin = originForSun(sun);
                    PVector bboxSize = bboxForSun(sun);

                    if (sunSwitchParams.get(sunIndex).getValueb()) {
                        projectToLeds(perSunProjectionCache.get(sun), sun.points);
                    } else {
                        for (final LXPoint point : sun.points) {
                            colors[point.index] = 0;
                        }
                    }
                }
            });
        }
    }

    private void projectToLeds(final int[][] cache, final LXPoint[] points) {
        for (int i = 0; i < cache.length; i++) {
            final int[] kernel = cache[i];
            final LXPoint pt = points[i];

            int count = 0;
            int aSum = 0;
            int rSum = 0;
            int gSum = 0;
            int bSum = 0;

            for (final int colorIndex : kernel) {
                if (colorIndex == -1) break;

                int colorVal = pg.pixels[colorIndex];
                rSum += colorVal & 0xFF;
                gSum += (colorVal >> 8) & 0xFF;
                bSum += (colorVal >> 16) & 0xFF;
                aSum += (colorVal >> 24) & 0xFF;
                count++;
            }

            if (count > 0) {
                colors[pt.index] = (rSum / count) | ((gSum / count) << 8) | ((bSum / count) << 16) | ((aSum / count) << 24);
            } else {
                colors[pt.index] = 0;
            }
        }
    }


    private Runnable run = new Runnable() {
        long lastRunAt = System.currentTimeMillis();

        @Override
        public void run() {
            double deltaMs = System.currentTimeMillis() - lastRunAt;
            lastRunAt = System.currentTimeMillis();

            //pg.beginDraw();
            P3CubeMapPattern.this.run(deltaMs, pg);
            //pg.endDraw();
            pg.loadPixels();

            projectToLeds();
        }
    };

    private double deltaMsAccumulator = 0;

    @Override
    final protected void run(final double deltaMs) {
        DrawHelper.queueJob(id, this.run);
    }

    // Implement this method; it should paint the cubemap image onto pg.
    public abstract void run(double deltaMs, PGraphics pg);

    private void computePointColorIndexes(
        int faceMinX,
        int faceMinY,
        double u,
        double v,
        int kernelSize,
        int[] output
    ) {
        if (u < -1 || u > 1 || v < -1 || v > 1 || pg == null || pg.pixels == null) {
            Arrays.fill(output, -1);
            return;
        }

        double offsetX = ((u + 1) / 2) * faceRes;
        double offsetY = ((v + 1) / 2) * faceRes;

        if (kernelSize < 1) kernelSize = 1;

        if (kernelSize == 1) {
            double x = faceMinX + offsetX;
            double y = faceMinY + offsetY;
            output[0] = (int) x + ((int) y) * pg.width;
        } else {
            int outputIndex = 0;
            for (int kx = 0; kx < kernelSize; kx++) {
                for (int ky = 0; ky < kernelSize; ky++) {
                    int x = (int) (faceMinX + offsetX + kx - kernelSize / 2);
                    int y = (int) (faceMinY + offsetY + ky - kernelSize / 2);

                    final int index = x + y * pg.width;
                    if (x >= 0 && x < pg.width && y >= 0 && y < pg.height && index < colors.length) {
                        output[outputIndex++] = index;
                    }
                }
            }
        }
    }
}
