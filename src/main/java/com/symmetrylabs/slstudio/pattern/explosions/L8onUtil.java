package com.symmetrylabs.slstudio.pattern.explosions;

import heronarts.lx.color.LXColor;

import static processing.core.PApplet.max;
import static processing.core.PApplet.min;

public class L8onUtil {
        L8onUtil() {
        }

        /*
         * Use this to decrease the brightness of a light over `delay` ms.
         * The current color is reduces by the appropriate proportion given
         * the deltaMs of the current run.
         */
        public static float decayed_brightness(int c, float delay,  double deltaMs) {
                float bright_prop = min(((float)deltaMs / delay), 1.0f);
                float bright_diff = max((LXColor.b(c) * bright_prop), 1);
                return max(LXColor.b(c) - bright_diff, 0.0f);
        }


        public static float natural_hue_blend(float hueBase, float hueNew) {
                return natural_hue_blend(hueBase, hueNew, 2);
        }

        /**
         * Use this to "naturally" blend colors.
         * Can be used iteratively on a point as more colors are "mixed" into it, or
         * used simply with 2 colors.
         *
         */
        public static float natural_hue_blend(float hueBase, float hueNew, int count) {
                // Return hueA if there is only one hue to mix
                if(count == 1) { return hueBase; }

                if(count > 2) {
                        // Jump color by 180 before blending again to avoid regression towards the mean (180)
                        hueBase = (hueBase + 180) % 360;
                }

                // Blend a with b
                float minHue = min(hueBase, hueNew);
                float maxHue = max(hueBase, hueNew);
                return (minHue * 2.0f + maxHue / 2.0f) / 2.0f;
        }
}
