package com.symmetrylabs.slstudio.pattern;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.color.LXColor;

public class ForestFlowerDynamicIterator extends LXPattern {

    private final DiscreteParameter modeParam;
    private final CompoundParameter indexParam;

    public ForestFlowerDynamicIterator(LX lx) {
        super(lx);

        // Initialize the mode parameter with the names and corresponding LED counts
        modeParam = new DiscreteParameter("Mode", 0, 4)
            .setDescription("Choose iteration mode")
            .setOptions(new String[]{"Flower", "SplitOutput", "PixOutput", "Pixlite"});

        // Maximum index is based on the smallest iteration size (7 LEDs)
        int maxIndex = (lx.model.size / 7) - 1;
        if (lx.model.size % 7 != 0) {
            maxIndex++;
        }

        // Initialize the index parameter
        indexParam = new CompoundParameter("Index", 0, 0, maxIndex);

        addParameter(modeParam);
        addParameter(indexParam);
    }

    @Override
    public void run(double deltaMs) {
        setColors(LXColor.BLACK);

        int[] ledCounts = {7, 63, 126, 504}; // Corresponding LED counts for each mode
        int mode = modeParam.getValuei();
        int ledsPerIteration = ledCounts[mode];
        
        int index = (int) indexParam.getValue() * ledsPerIteration; // Calculate the start index based on the chosen mode

        for (int i = 0; i < ledsPerIteration; i++) {
            if (index + i < lx.model.size) {
                setColor(index + i, palette.getColor());
            }
        }
    }
}
