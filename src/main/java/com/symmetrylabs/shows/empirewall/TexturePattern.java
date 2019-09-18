package com.symmetrylabs.shows.empirewall;

import heronarts.lx.LX;

import com.symmetrylabs.shows.tree.TreeModel;
import heronarts.lx.parameter.BooleanParameter;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;

public abstract class TexturePattern extends SLPattern {
    public static final String GROUP_NAME = EmpireWallShow.SHOW_NAME;

    public final VineModel model;

    public final BooleanParameter rotate =
        new BooleanParameter("Rotate", false)
        .setDescription("Rotates texture masks between elements");

    public TexturePattern(LX lx) {
        super(lx);
        this.model = (VineModel) lx.model;
        addParameter(rotate);
    }

    protected void setLeafMask(int[] leafMask) {
        int offset = 0;
        boolean rot = rotate.getValueb();
        for (TreeModel.Leaf leaf : model.leaves) {
            for (int i = 0; i < TreeModel.Leaf.NUM_LEDS; ++i) {
                colors[leaf.point.index + i] = leafMask[(i + offset) % leafMask.length];
            }
            if (rot) {
                offset++;
            }
        }
    }

    // protected void setVineMask(int[] vineMask) {
    //     int offset = 0;
    //     boolean rot = rotate.getValueb();
    //     //for (VineModel.Vine vine : model.vines) {
    //         for (int i = 0; i < vine.points.length; ++i) {
    //             colors[vine.points[i].index] = vineMask[(i + offset) % vineMask.length];
    //         }
    //         if (rot) {
    //             offset++;
    //         }
    //     //}
    // }
}
