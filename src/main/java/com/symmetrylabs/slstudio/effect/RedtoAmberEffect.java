package com.symmetrylabs.slstudio.effect;

import heronarts.lx.LX;
import heronarts.lx.LXEffect;
import heronarts.lx.model.LXPoint;
import heronarts.lx.color.LXColor;
import com.symmetrylabs.color.Ops8;
import com.symmetrylabs.slstudio.component.GammaExpander;

public class RedtoAmberEffect extends LXEffect {
    private static final int BYTES_PER_PIXEL = 6; // should be multiple of 3

    private final GammaExpander gammaExpander;

    public RedtoAmberEffect(LX lx) {
        super(lx);

        gammaExpander = GammaExpander.getInstance(lx);
    }

    @Override
    public void run(double deltaMs, double amount) {
        for (LXPoint point : model.points) {
            int index = point.index * BYTES_PER_PIXEL / 3; // Calculate the starting index for this point's RGB data

            // Ensure we do not exceed the bounds of the colors array
            if (index + BYTES_PER_PIXEL / 3 > colors.length) {
                continue; // Skip to the next point if out of bounds
            }

            // Retrieve the current color
            int gammaExpanded = gammaExpander.getExpandedColor(colors[index]);
            byte r = (byte)Ops8.red(gammaExpanded);
            byte g = (byte)Ops8.green(gammaExpanded);
            byte b = (byte)Ops8.blue(gammaExpanded);
            byte w = r < g ? r : g;
            if (b < w) {
                w = b;
            }
            r -= w;
            g -= w;
            b -= w;
            byte y = r < g ? r : g; // yellow/amber
            r -= y;
            g -= y;
            byte p = b; // purple/UV

            // Now apply the modified values back into the colors array using the same index
            colors[index] = LXColor.rgb(r, g, b); // 1 2 3
            colors[index + 1] = LXColor.rgb(w, y, p); // 4 5 6
        }
    }
}
