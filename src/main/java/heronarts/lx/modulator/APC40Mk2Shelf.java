package heronarts.lx.modulator;

import java.util.List;
import java.util.ArrayList;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.LXParameter;


/**
 * A shelf that mimics the layout of the "Device Control" area of the APC40 controller
 */
public class APC40Mk2Shelf extends Shelf {
    private final List<BoundedParameter> knobs = new ArrayList<>();
    private final List<BooleanParameter> triggers = new ArrayList<>();

    public APC40Mk2Shelf() {
        super("APC40Mk2 Shelf");

        for (int r = 0; r < 2; r++) {
            for (int c = 0; c < 4; c++) {
                BoundedParameter knob = new BoundedParameter(String.format("K%d", 4 * r + c));
                addParameter(knob);
                knobs.add(knob);
            }
        }
        for (int c = 0; c < 4; c++) {
            BooleanParameter trig = new BooleanParameter(String.format("T%d", c));
            addParameter(trig);
            triggers.add(trig);
        }
    }

    @Override
    public int rows() {
        return 3;
    }

    @Override
    public int cols() {
        return 4;
    }

    @Override
    public LXParameter getParameter(int row, int col) {
        if (row < 2) {
            return knobs.get(row * 4 + col);
        }
        return triggers.get(col);
    }
}
