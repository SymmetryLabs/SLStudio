package heronarts.lx.modulator;

import heronarts.lx.parameter.BoundedParameter;

public class GrandMaMacroKnobs extends LXModulator {

    public static int NUM_MACRO = 64;
    public final BoundedParameter[] macros = new BoundedParameter[NUM_MACRO];

    public static String knobLabel = "grMA";

    public GrandMaMacroKnobs() {
        this("MACRO");
    }

    public GrandMaMacroKnobs(String label) {
        super(label);
        for (int i = 0; i < NUM_MACRO; i++ ){
            this.macros[i] = new BoundedParameter(knobLabel + i);
            addParameter(knobLabel + i, this.macros[i]);
        }
    }

    @Override
    protected double computeValue(double deltaMs) {
        // Not relevant
        return 0;
    }
}
