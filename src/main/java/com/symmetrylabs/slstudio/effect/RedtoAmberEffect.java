package com.symmetrylabs.slstudio.effect;

import heronarts.lx.LX;
import heronarts.lx.LXEffect;
import heronarts.lx.model.LXPoint;
import heronarts.lx.color.LXColor;

public class RedtoAmberEffect extends LXEffect {

    public RedtoAmberEffect(LX lx) {
        super(lx);
    }

    @Override
    public void run(double deltaMs, double amount) {
        for (LXPoint point : model.points) {
            int index = point.index * 3; // Calculate the starting index for this point's RGB data

            // Ensure we do not exceed the bounds of the colors array
            if (index + 11 >= colors.length) {
                continue; // Skip to the next point if out of bounds
            }

            // Retrieve the current color
            int c = colors[index];
            int r = LXColor.red(c);
            int g = LXColor.green(c);
            int b = LXColor.blue(c);

            // Modify the colors according to the required channel manipulation
            int modifiedRed = r;       // Set the red value to the blue channel
            int modifiedGreen = g;      // Keep green unchanged
            int modifiedBlue = b;
            int blackColor = 0;         // Set unused channels to black (0)

            // Now apply the modified values back into the colors array using the same index
            colors[index] = LXColor.rgb(blackColor, modifiedGreen, modifiedBlue); // 1 2 3
            colors[index + 1] = LXColor.rgb(blackColor, modifiedRed, blackColor); // 4 5 6
            colors[index + 2] = LXColor.rgb(blackColor, modifiedGreen, modifiedBlue); // 7 8 9
            colors[index + 3] = LXColor.rgb(blackColor, modifiedRed, blackColor); // 10 11 12   
            colors[index + 4] = LXColor.rgb(blackColor, modifiedGreen, modifiedBlue); // 7 8 9
            colors[index + 5] = LXColor.rgb(blackColor, modifiedRed, blackColor); // 10 11 12  
            colors[index + 6] = LXColor.rgb(blackColor, modifiedGreen, modifiedBlue); // 7 8 9
            colors[index + 7] = LXColor.rgb(blackColor, modifiedRed, blackColor); // 10 11 12  
            colors[index + 8] = LXColor.rgb(blackColor, modifiedGreen, modifiedBlue); // 7 8 9
            colors[index + 9] = LXColor.rgb(blackColor, modifiedRed, blackColor); // 10 11 12  
            colors[index + 10] = LXColor.rgb(blackColor, modifiedGreen, modifiedBlue); // 7 8 9
            colors[index + 11] = LXColor.rgb(blackColor, modifiedRed, blackColor); // 10 11 12   
        }
    }
}
