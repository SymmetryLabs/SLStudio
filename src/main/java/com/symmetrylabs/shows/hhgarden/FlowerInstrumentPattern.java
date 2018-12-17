package com.symmetrylabs.shows.hhgarden;

import com.symmetrylabs.shows.hhgarden.FlowerModel.FlowerPoint;
import com.symmetrylabs.shows.hhgarden.FlowerModel.Group;
import com.symmetrylabs.slstudio.pattern.instruments.InstrumentPattern;

import heronarts.lx.LX;
import heronarts.lx.parameter.EnumParameter;

/** InstrumentPattern, plus an extra parameter to mask out parts of flowers. */
public class FlowerInstrumentPattern extends InstrumentPattern {
    public static final String GROUP_NAME = HHGardenShow.SHOW_NAME;

    public enum FlowerMask {
        ALL,
        STAMEN,
        PETAL1,
        PETAL2,
        STEM
    };

    public static boolean[][] MASK_GROUPS = new boolean[FlowerMask.values().length][];
    static {
        for (FlowerMask mask : FlowerMask.values()) {
            MASK_GROUPS[mask.ordinal()] = new boolean[Group.values().length];
        }
        for (Group group : Group.values()) {
            MASK_GROUPS[FlowerMask.ALL.ordinal()][group.ordinal()] = true;
        }
        MASK_GROUPS[FlowerMask.STAMEN.ordinal()][Group.STAMEN.ordinal()] = true;
        MASK_GROUPS[FlowerMask.PETAL1.ordinal()][Group.PETAL1.ordinal()] = true;
        MASK_GROUPS[FlowerMask.PETAL2.ordinal()][Group.PETAL2.ordinal()] = true;
        MASK_GROUPS[FlowerMask.STEM.ordinal()][Group.STEM.ordinal()] = true;
    }

    private final EnumParameter<FlowerMask> maskParam = new EnumParameter<>("Mask", FlowerMask.ALL);

    public FlowerInstrumentPattern(LX lx) {
        super(lx);
        addParameter(maskParam);
    }

    private void applyMask(int[] colors) {
        FlowerPoint[] points = FlowerUtils.getFlowerPoints(model);
        if (points != null) {
            FlowerMask mask = maskParam.getEnum();
            boolean[] maskGroups = MASK_GROUPS[mask.ordinal()];
            for (int i = 0; i < points.length; i++) {
                if (!maskGroups[points[i].group.ordinal()]) {
                    colors[i] = colors[i] & 0x00_ff_ff_ff;
                }
            }
        }
    }

    private void applyMask(long[] colors) {
        FlowerPoint[] points = FlowerUtils.getFlowerPoints(model);
        if (points != null) {
            FlowerMask mask = maskParam.getEnum();
            boolean[] maskGroups = MASK_GROUPS[mask.ordinal()];
            for (int i = 0; i < points.length; i++) {
                if (!maskGroups[points[i].group.ordinal()]) {
                    colors[i] = colors[i] & 0x0000_ffff_ffff_ffffL;
                }
            }
        }
    }
}
