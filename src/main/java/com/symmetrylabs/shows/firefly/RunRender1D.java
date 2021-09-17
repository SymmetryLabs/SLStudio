package com.symmetrylabs.shows.firefly;

import art.lookingup.KaledoscopeModel;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

/**
 * LightBarRender1D implements a variety of 1D rendering functions that
 * are local to the specified LightBar.
 */
public class RunRender1D {
    private static final Logger logger = Logger.getLogger(RunRender1D.class.getName());

    static public void randomGray(int colors[], KaledoscopeModel.Run run, LXColor.Blend blend) {
        Random r = new Random();
        for (LXPoint pt : run.allPoints) {
            int randomValue = r.nextInt(256);
            colors[pt.index] = LXColor.blend(colors[pt.index], LXColor.rgba(randomValue, randomValue, randomValue, 255), blend);
        }
    }

    static public void randomGrayBaseDepth(int colors[], KaledoscopeModel.Run run, LXColor.Blend blend, int min, int depth) {
        for (LXPoint pt : run.allPoints) {
            if (depth < 0)
                depth = 0;
            int randomDepth = ThreadLocalRandom.current().nextInt(depth);
            int value = min + randomDepth;
            if (value > 255) {
                value = 255;
            }
            colors[pt.index] = LXColor.blend(colors[pt.index], LXColor.rgba(value, value, value, 255), blend);
        }
    }

    static public void sine(int colors[], KaledoscopeModel.Run run, float head, float freq, float phase, float min, float depth, LXColor.Blend blend) {
        for (LXPoint pt : run.allPoints) {
            float ptX = run.getRunDistance(pt) / run.getButterfliesRunDistance();
            float value = ((float)Math.sin((double)freq * (head - ptX) + phase) + 1.0f)/2.0f;
            value = min + depth * value;
            int color = (int)(value * 255f);
            colors[pt.index] = LXColor.blend(colors[pt.index], LXColor.rgba(color, color, color, 255), blend);
        }
    }

    static public void cosine(int colors[], KaledoscopeModel.Run run, float head, float freq, float phase, float min, float depth, LXColor.Blend blend) {
        for (LXPoint pt : run.allPoints) {
            float ptX = run.getRunDistance(pt) / run.getButterfliesRunDistance();
            float value = ((float)Math.cos((double)freq * (head - ptX) + phase) + 1.0f)/2.0f;
            value = min + depth * value;
            int color = (int)(value * 255f);
            colors[pt.index] = LXColor.blend(colors[pt.index], LXColor.rgba(color, color, color, 255), blend);
        }
    }

    /**
     * Render a triangle gradient in gray.  t is the 0 to 1 normalized x position.  Slope
     * is the slope of the gradient.
     * TODO(tracy): Slope normalization needs to account for led density? i.e. Max slope should include
     * only one led.  Minimum slope should include all leds.
     * @param colors LED colors array.
     * @param run The run to render on.
     * @param t Normalized (0.0-1.0) x position.
     * @param slope The slope of the gradient.  Not normalized currently.
     * @param maxValue Maximum value of the step function (0.0 - 1.0)
     * @param blend Blend mode for writing into the colors array.
     * @return A float array containing the minimum x intercept and maximum x intercept in that order.
     */
    static public float[] renderTriangle(int colors[], KaledoscopeModel.Run run, float t, float slope, float maxValue, LXColor.Blend blend) {
        return renderTriangle(colors, run, t, slope, maxValue, blend, LXColor.rgba(255, 255, 255, 255));
    }

    static public float[] renderTriangle(int colors[], KaledoscopeModel.Run run, float t, float slope, float maxValue, LXColor.Blend blend,
                                         int color) {
        float[] minMax = new float[2];
        minMax[0] = (float)zeroCrossingTriangleWave(t, slope);
        minMax[1] = (float)zeroCrossingTriangleWave(t, -slope);
        for (LXPoint pt : run.allPoints) {
            float ptX = run.getRunDistance(pt) / run.getButterfliesRunDistance();
            float val = (float)triangleWave(t, slope, ptX)*maxValue;
            //colors[pt.index] = LXColor.blend(colors[pt.index], LXColor.rgba(gray, gray, gray, 255), blend);
            colors[pt.index] = LXColor.blend(colors[pt.index], LXColor.rgba(
                (int)(Colors.red(color) * val), (int)(Colors.green(color) * val), (int)(Colors.blue(color) * val), 255),
                blend);
        }
        return minMax;
    }

    static public class Colors {
        /**
         * Returns the red part of a 32-bit RGBA color.
         */
        public static int red(int color) {
            return (color >> 16) & 0xff;
        }

        /**
         * Returns the green part of a 32-bit RGBA color.
         */
        public static int green(int color) {
            return (color >> 8) & 0xff;
        }

        /**
         * Returns the blue part of a 32-bit RGBA color.
         */
        public static int blue(int color) {
            return color & 0xff;
        }

        /**
         * Returns the alpha part of a 32-bit RGBA color.
         */
        public static int alpha(int color) {
            return (color >> 24) & 0xff;
        }
    }


    static public float[] renderSquare(int colors[], KaledoscopeModel.Run run, float t, float width, float maxValue, LXColor.Blend blend) {
        return renderSquare(colors, run, t, width, maxValue, blend, LXColor.rgba(255, 255, 255, 255));
    }

    static public float[] renderSquare(int colors[], KaledoscopeModel.Run run, float t, float width, float maxValue, LXColor.Blend blend,
                                       int color) {
        double barPos = t * run.getButterfliesRunDistance();
        float[] minMax = new float[2];
        minMax[0] = t - width/2.0f;
        minMax[1] = t + width/2.0f;
        for (LXPoint pt: run.allPoints) {
            float ptX = run.getRunDistance(pt);
            float totalRunDistance = run.getButterfliesRunDistance();
            //int gray = (int) ((((pt.lbx > minMax[0]*lightBar.length) && (pt.lbx < minMax[1]*lightBar.length))?maxValue:0f)*255.0f);
            float val = (((ptX > minMax[0]*totalRunDistance) && (ptX < minMax[1]*totalRunDistance))?maxValue:0f);
            int newColor = LXColor.blend(colors[pt.index], LXColor.rgba(
                (int)(Colors.red(color) * val), (int)(Colors.green(color) * val), (int)(Colors.blue(color) * val), 255),
                blend);
            colors[pt.index] = newColor;
        }
        return minMax;
    }

    /**
     * Render a step function at the given position with the given slope.
     * @param colors Points color array to write into.
     * @param run The run to render on.
     * @param t Normalized (0.0-1.0) x position of the step function on the run.
     * @param slope The slope of edge of the step function.
     * @param maxValue Maximum value of the step function (0.0 - 1.0)
     * @param forward Direction of the step function.
     * @param blend Blend mode for writing into the colors array.
     */
    static public float[] renderStepDecay(int colors[], KaledoscopeModel.Run run, float t, float width, float slope,
                                          float maxValue, boolean forward, LXColor.Blend blend) {
        return renderStepDecay(colors, run, t, width, slope, maxValue, forward, blend, LXColor.rgba(255, 255, 255, 255));
    }

    static public float[] renderStepDecay(int colors[], KaledoscopeModel.Run run, float t, float width, float slope,
                                          float maxValue, boolean forward, LXColor.Blend blend, int color) {
        float[] minMax = stepDecayZeroCrossing(t, width, slope, forward);
        for (LXPoint pt : run.allPoints) {
            //int gray = (int) (stepDecayWave(t, width, slope, pt.lbx/lightBar.length, forward)*255.0*maxValue);
            float ptX = run.getRunDistance(pt);
            float totalRunDistance = run.getButterfliesRunDistance();
            float val = stepDecayWave(t, width, slope, ptX/totalRunDistance, forward)*maxValue;
            //colors[pt.index] = LXColor.blend(colors[pt.index], LXColor.rgba(gray, gray, gray, 255), blend);
            colors[pt.index] = LXColor.blend(colors[pt.index], LXColor.rgba(
                (int)(Colors.red(color) * val), (int)(Colors.green(color) * val), (int)(Colors.blue(color) * val), 255),
                blend);
        }

        return minMax;
    }

    static public float triWave(float t, float p)  {
        return 2.0f * (float)Math.abs(t / p - Math.floor(t / p + 0.5f));
    }

    static public float[] stepDecayZeroCrossing(float stepPos, float width, float slope, boolean forward) {
        float[] minMax = new float[2];
        float max = stepPos + width/2.0f;
        float min = stepPos - width/2.0f - 1.0f/slope;
        // If our orientation traveling along the bar is backwards, swap our min/max computations.

        float tail = 0f;
        if (forward) {
            tail  = - 1.0f/slope + stepPos - width/2.0f;
        } else {
            tail = 1.0f/slope + stepPos + width/2.0f;
        }

        float head = 0;
        if (forward) {
            head = stepPos + width/2.0f;
        } else {
            head = stepPos - width/2.0f;
        }

        if (forward) {
            minMax[0] = tail;
            minMax[1] = head;
        } else {
            minMax[1] = tail;
            minMax[0] = head;
        }
        return minMax;
    }

    /**
     * Step wave with attack slope.
     * Returns value from 0.0f to 1.0f
     */
    static public float stepDecayWave(float stepPos, float width, float slope, float x, boolean forward) {
        float value;
        if ((x > stepPos - width/2.0f) && (x < stepPos + width/2.0f))
            return 1.0f;

        if ((x > stepPos + width/2.0f) && forward)
            return 0f;
        else if ((x < stepPos - width/2.0f && !forward))
            return 0f;

        if (forward) {
            value = 1.0f + slope * (x - (stepPos - width/2.0f));
            if (value < 0f) value = 0f;
        } else {
            value = 1.0f - slope * (x - (stepPos + width/2.0f));
            if (value < 0f) value = 0f;
        }
        return value;
    }

    static public double zeroCrossingTriangleWave(double peakX, double slope) {
        return peakX - 1.0/slope;
    }

    /**
     * Normalized triangle wave function.  Given position of triangle peak and the
     * slope, return value of function at evalAtX.  If less than 0, clip to zero.
     */
    static public double triangleWave(double peakX, double slope, double evalAtX)
    {
        // If we are to the right of the triangle, the slope is negative
        if (evalAtX > peakX) slope = -slope;
        double y = slope * (evalAtX - peakX) + 1.0f;
        if (y < 0f) y = 0f;
        return y;
    }

    static public void renderColor(int[] colors, KaledoscopeModel.Run run, int red, int green, int blue, int alpha) {
        renderColor(colors, run, LXColor.rgba(red, green, blue, alpha));
    }

    static public void renderColor(int[] colors, KaledoscopeModel.Run run, int color) {
        renderColor(colors, run, color, 1.0f);
    }

    static public void renderColor(int[] colors, KaledoscopeModel.Run run, int color, float maxValue) {
        for (LXPoint point: run.allPoints) {
            colors[point.index] = LXColor.rgba(
                (int)(Colors.red(color) * maxValue), (int)(Colors.green(color) * maxValue), (int)(Colors.blue(color) * maxValue), 255);
        }
    }
}
