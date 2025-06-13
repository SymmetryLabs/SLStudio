package com.symmetrylabs.slstudio.pattern;

import com.symmetrylabs.slstudio.model.StripsModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.DiscreteParameter;
import java.awt.Color;

/**
 * Lights up a selected output (block of 300 LEDs) within a selected controller (block of 2400 LEDs).
 * Parameters:
 *   - selectedController: 0-7 (which controller)
 *   - selectedOutput: 0-7 (which output within controller)
 * Only the selected 300-LED block is lit; all others are off.
 */
public class CloudMapping extends SLPattern<StripsModel> {
    private static final int LEDS_PER_SECTION = 300;
    private static final int OUTPUTS_PER_CONTROLLER = 8;
    private static final int LEDS_PER_CONTROLLER = LEDS_PER_SECTION * OUTPUTS_PER_CONTROLLER; // 2400
    private static final int NUM_CONTROLLERS = 8;
    private static final Color[] CONTROLLER_COLORS = new Color[] {
        Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.MAGENTA, Color.CYAN, Color.ORANGE, Color.PINK
    };

    private final DiscreteParameter controllerParam;
    private final DiscreteParameter outputParam;

    public CloudMapping(LX lx) {
        super(lx);
        controllerParam = new DiscreteParameter("Controller", 0, NUM_CONTROLLERS);
        outputParam = new DiscreteParameter("Output", 0, OUTPUTS_PER_CONTROLLER);
        addParameter(controllerParam);
        addParameter(outputParam);
    }

    @Override
    public void run(double deltaMs) {
        setColors(LXColor.BLACK);
        int selectedController = controllerParam.getValuei();
        int selectedOutput = outputParam.getValuei();
        int startIdx = selectedController * LEDS_PER_CONTROLLER + selectedOutput * LEDS_PER_SECTION;
        int endIdx = startIdx + LEDS_PER_SECTION;
        for (int i = startIdx; i < endIdx && i < colors.length; i++) {
            colors[i] = LXColor.BLUE;
        }
    }

    /**
     * Get the maximum number of sections for the current LED count.
     */
    public int getMaxSections(int totalLeds) {
        return (int)Math.ceil((double)totalLeds / LEDS_PER_SECTION);
    }

    /**
     * Get the number of outputs (fixed at 16).
     */
    public int getNumOutputs() {
        return OUTPUTS_PER_CONTROLLER;
    }
}




//each section should be a different color

