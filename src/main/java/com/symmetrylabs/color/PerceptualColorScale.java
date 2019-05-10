package com.symmetrylabs.color;

import com.google.common.base.Preconditions;
import com.symmetrylabs.color.Spaces;
import org.apache.commons.math3.util.FastMath;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.Arrays;


/**
 * Scales linear colors to maintain relative perceptual color differences.
 */
public class PerceptualColorScale {
    protected final int[][] lut8 = new int[3][256];
    protected final int[][] lut16 = new int[3][65536];

    protected final ReentrantReadWriteLock lutLock = new ReentrantReadWriteLock();
    protected final double[] perChannelGamma;
    protected double targetLinearScale;
    protected double perceptualScale;

    public PerceptualColorScale(double[] perChannelGamma, double targetLinearScale) {
        this.perChannelGamma = perChannelGamma;
        setTargetLinearScale(targetLinearScale);
    }

    public void setTargetLinearScale(double targetLinearScale) {
        Preconditions.checkArgument(targetLinearScale >= 0 && targetLinearScale <= 1);
        this.targetLinearScale = targetLinearScale;
        buildLuts();
    }

    public double getTargetLinearScale() {
        return targetLinearScale;
    }

    protected void buildLuts() {
        lutLock.writeLock().lock();
        try {
            /* we treat the target scale as a target luminance value, and then
            determine the lightness value associated with that luminance. We take
            this as the "lightness scale": the maximum lightness that we're
            allowed to have in our output. We then map all input colors to output
            colors by applying that lightness scale to the perceptual color. */
            perceptualScale = Spaces.cie_luminance_to_lightness(targetLinearScale);

            for (int channel = 0; channel < 3; channel++) {
                final double gamma = perChannelGamma[channel];
                for (int linearI = 0; linearI < 256; linearI++) {
                    final double linearV = ((double) linearI) / 256.0;
                    final double perceptualV = FastMath.pow(linearV, 1.0 / gamma);
                    final double scaledPerceptualV = perceptualV * perceptualScale;
                    final double scaledLinearV = FastMath.pow(scaledPerceptualV, gamma);
                    lut8[channel][linearI] = (int) FastMath.round(255.0 * scaledLinearV);
                }
                for (int linearI = 0; linearI < 65536; linearI++) {
                    final double linearV = ((double) linearI) / 65536.0;
                    final double perceptualV = FastMath.pow(linearV, 1.0 / gamma);
                    final double scaledPerceptualV = perceptualV * perceptualScale;
                    final double scaledLinearV = FastMath.pow(scaledPerceptualV, gamma);
                    lut16[channel][linearI] = (int) FastMath.round(65535.0 * scaledLinearV);
                }
            }
        } finally {
            lutLock.writeLock().unlock();
        }
    }

    public void copyLut8(int[][] copyTo) {
        lutLock.readLock().lock();
        try {
            for (int i = 0; i < 3; i++) {
                for (int v = 0; v < 256; v++) {
                    copyTo[i][v] = lut8[i][v];
                }
            }
        } finally {
            lutLock.readLock().unlock();
        }
    }

    public void copyLut16(int[][] copyTo) {
        lutLock.readLock().lock();
        try {
            for (int i = 0; i < 3; i++) {
                for (int v = 0; v < 65536; v++) {
                    copyTo[i][v] = lut8[i][v];
                }
            }
        } finally {
            lutLock.readLock().unlock();
        }
    }

    public int apply8(int color) {
        lutLock.readLock().lock();
        try {
            int r = Ops8.red(color);
            int g = Ops8.green(color);
            int b = Ops8.blue(color);
            int a = Ops8.alpha(color);
            return Ops8.rgba(lut8[0][r], lut8[1][g], lut8[2][b], a);
        } finally {
            lutLock.readLock().unlock();
        }
    }

    public long apply16(long color) {
        lutLock.readLock().lock();
        try {
            int r = Ops16.red(color);
            int g = Ops16.green(color);
            int b = Ops16.blue(color);
            int a = Ops16.alpha(color);
            return Ops16.rgba(lut16[0][r], lut16[1][g], lut16[2][b], a);
        } finally {
            lutLock.readLock().unlock();
        }
    }
}
