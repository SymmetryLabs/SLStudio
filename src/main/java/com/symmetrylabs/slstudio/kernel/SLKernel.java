package com.symmetrylabs.slstudio.kernel;

import com.aparapi.Kernel;
import com.aparapi.Range;
import com.aparapi.device.Device;
import com.aparapi.device.OpenCLDevice;
import org.apache.commons.math3.util.FastMath;

public abstract class SLKernel extends Kernel {

        // Generally, the recommended work-group size for kernels is 64-128 work-items.
        public static final int LOCAL_WORKGROUP_SIZE = 128;

        public static Device getGPU() {
                return OpenCLDevice.select((a, b) -> b, Device.TYPE.GPU);
        }

        public static Range getRangeForSize(int size) {
                int localSize = LOCAL_WORKGROUP_SIZE;
                int globalSize = (int) (FastMath.ceil(((float) size) / localSize) * localSize);
                return Range.create(getGPU(), globalSize, localSize);
        }

        public void executeForSize(int size) {
                execute(getRangeForSize(size));
        }


        public final float PI = (float) FastMath.PI;

        public final int RED_MASK = 0x00ff0000;
        public final int GREEN_MASK = 0x0000ff00;
        public final int BLUE_MASK = 0x000000ff;

        public final int RED_SHIFT = 16;
        public final int GREEN_SHIFT = 8;

        /**
         * Hue of a color from 0-360
         *
         * @param rgb Color value
         * @return Hue value from 0-360
         */
        public float h(int rgb) {
                int r = (rgb & RED_MASK) >> RED_SHIFT;
                int g = (rgb & GREEN_MASK) >> GREEN_SHIFT;
                int b = rgb & BLUE_MASK;
                int max = (r > g) ? r : g;
                if (b > max) {
                        max = b;
                }
                int min = (r < g) ? r : g;
                if (b < min) {
                        min = b;
                }
                if (max == 0) {
                        return 0;
                }
                float range = max - min;
                if (range == 0) {
                        return 0;
                }
                float h = 0;
                float rc = (max - r) / range;
                float gc = (max - g) / range;
                float bc = (max - b) / range;
                if (r == max) {
                        h = bc - gc;
                } else if (g == max) {
                        h = 2.f + rc - bc;
                } else {
                        h = 4.f + gc - rc;
                }
                h /= 6.f;
                if (h < 0) {
                        h += 1.f;
                }
                return 360.f * h;
        }

    /**
     * Hue of a color from 0-1
     *
     * @param rgb Color value
     * @return Hue value from 0-1
     */
    public float hn(int rgb) {
        int r = (rgb & RED_MASK) >> RED_SHIFT;
        int g = (rgb & GREEN_MASK) >> GREEN_SHIFT;
        int b = rgb & BLUE_MASK;
        int max = (r > g) ? r : g;
        if (b > max) {
            max = b;
        }
        int min = (r < g) ? r : g;
        if (b < min) {
            min = b;
        }
        if (max == 0) {
            return 0;
        }
        float range = max - min;
        if (range == 0) {
            return 0;
        }
        float h = 0;
        float rc = (max - r) / range;
        float gc = (max - g) / range;
        float bc = (max - b) / range;
        if (r == max) {
            h = bc - gc;
        } else if (g == max) {
            h = 2.f + rc - bc;
        } else {
            h = 4.f + gc - rc;
        }
        h /= 6.f;
        if (h < 0) {
            h += 1.f;
        }
        return h;
    }

        /**
         * Saturation from 0-100
         *
         * @param rgb Color value
         * @return Saturation value from 0-100
         */
        public float s(int rgb) {
                int r = (rgb & RED_MASK) >> RED_SHIFT;
                int g = (rgb & GREEN_MASK) >> GREEN_SHIFT;
                int b = rgb & BLUE_MASK;
                int max = (r > g) ? r : g;
                if (b > max) {
                        max = b;
                }
                int min = (r < g) ? r : g;
                if (b < min) {
                        min = b;
                }
                return (max == 0) ? 0 : (max - min) * 100.f / max;
        }

        /**
         * Saturation from 0-1
         *
         * @param rgb Color value
         * @return Saturation value from 0-1
         */
        public float sn(int rgb) {
                int r = (rgb & RED_MASK) >> RED_SHIFT;
                int g = (rgb & GREEN_MASK) >> GREEN_SHIFT;
                int b = rgb & BLUE_MASK;
                int max = (r > g) ? r : g;
                if (b > max) {
                        max = b;
                }
                int min = (r < g) ? r : g;
                if (b < min) {
                        min = b;
                }
                return (max == 0) ? 0 : ((float) (max - min)) / max;
        }

        /**
         * Brightness from 0-100
         *
         * @param rgb Color value
         * @return Brightness from 0-100
         */
        public float b(int rgb) {
                int r = (rgb & RED_MASK) >> RED_SHIFT;
                int g = (rgb & GREEN_MASK) >> GREEN_SHIFT;
                int b = rgb & BLUE_MASK;
                int max = (r > g) ? r : g;
                if (b > max) {
                        max = b;
                }
                return 100.f * max / 255.f;
        }

        /**
         * Brightness from 0-1
         *
         * @param rgb Color value
         * @return Brightness from 0-1
         */
        public float bn(int rgb) {
                int r = (rgb & RED_MASK) >> RED_SHIFT;
                int g = (rgb & GREEN_MASK) >> GREEN_SHIFT;
                int b = rgb & BLUE_MASK;
                int max = (r > g) ? r : g;
                if (b > max) {
                        max = b;
                }
                return max / 255.f;
        }

        private final float H_COEFF = 1 / 360.f;
        private final float S_COEFF = 1 / 100.f;
        private final float B_COEFF = 1 / 100.f;

        /**
         * Create a color from HSB
         *
         * @param h Hue from 0-360
         * @param s Saturation from 0-100
         * @param b Brightness from 0-100
         * @return rgb color value
         */
        public int hsb(float h, float s, float b) {
                return _hsbImpl(h * H_COEFF, s * S_COEFF, b * B_COEFF);
        }

        /**
         * Create a color from HSB
         *
         * @param h Hue from 0-1
         * @param s Saturation from 0-1
         * @param b Brightness from 0-1
         * @return rgb color value
         */
        public int hsbn(float h, float s, float b) {
                return _hsbImpl(h, s, b);
        }

        private int _hsbImpl(float hue, float saturation, float brightness) {
                int r = 0, g = 0, b = 0;
                if (saturation == 0) {
                        r = g = b = (int) (brightness * 255.f + 0.5f);
                } else {
                        float h = (hue - floor(hue)) * 6.0f;
                        float f = h - floor(h);
                        float p = brightness * (1.0f - saturation);
                        float q = brightness * (1.0f - saturation * f);
                        float t = brightness * (1.0f - (saturation * (1.0f - f)));
                        if (((int) h) == 0) {
                                r = (int) (brightness * 255.0f + 0.5f);
                                g = (int) (t * 255.0f + 0.5f);
                                b = (int) (p * 255.0f + 0.5f);
                        } else if (((int) h) == 1) {
                                r = (int) (q * 255.0f + 0.5f);
                                g = (int) (brightness * 255.0f + 0.5f);
                                b = (int) (p * 255.0f + 0.5f);
                        } else if (((int) h) == 2) {
                                r = (int) (p * 255.0f + 0.5f);
                                g = (int) (brightness * 255.0f + 0.5f);
                                b = (int) (t * 255.0f + 0.5f);
                        } else if (((int) h) == 3) {
                                r = (int) (p * 255.0f + 0.5f);
                                g = (int) (q * 255.0f + 0.5f);
                                b = (int) (brightness * 255.0f + 0.5f);
                        } else if (((int) h) == 4) {
                                r = (int) (t * 255.0f + 0.5f);
                                g = (int) (p * 255.0f + 0.5f);
                                b = (int) (brightness * 255.0f + 0.5f);
                        } else if (((int) h) == 5) {
                                r = (int) (brightness * 255.0f + 0.5f);
                                g = (int) (p * 255.0f + 0.5f);
                                b = (int) (q * 255.0f + 0.5f);
                        }
                }
                return 0xff000000 | (r << 16) | (g << 8) | (b << 0);
        }

        /**
         * Create a color from HSB
         *
         * @param color rgb color value
         * @param shiftAmt hue shift amount from 0-360
         * @return rgb color value
         */
        public int shiftHue(int color, float shiftAmt) {
                return hsbn(hn(color) + shiftAmt / 360.f, sn(color), bn(color));
        }

}
