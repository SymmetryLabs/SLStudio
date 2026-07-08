package com.symmetrylabs.shows.cuddlefish;

import java.util.List;

import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;

import heronarts.lx.LX;
import heronarts.lx.LXEffect;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;

/**
 * Filters LED output by strip group. Strips are assigned to up to 8 groups
 * via the GROUPS tab (UICuddlefishStripGroupTool). When one or more groups are enabled
 * on this effect, only strips belonging to an enabled group light up; all
 * other strips are blacked out. When no groups are enabled, all strips pass
 * through unchanged.
 */
public class GroupStripFilter extends LXEffect {

    public final BooleanParameter[] groups = new BooleanParameter[StripGroups.NUM_GROUPS];

    public GroupStripFilter(LX lx) {
        super(lx);
        for (int g = 0; g < StripGroups.NUM_GROUPS; g++) {
            groups[g] = new BooleanParameter("g" + (g + 1), false);
            addParameter(groups[g]);
        }
    }

    @Override
    public void run(double deltaMs, double amount) {
        if (!(lx.model instanceof StripsModel)) return;

        // Determine which groups are enabled
        boolean anyEnabled = false;
        boolean[] enabled = new boolean[StripGroups.NUM_GROUPS];
        for (int g = 0; g < StripGroups.NUM_GROUPS; g++) {
            enabled[g] = groups[g].getValueb();
            anyEnabled |= enabled[g];
        }
        if (!anyEnabled) return;  // no filtering when no group is selected

        @SuppressWarnings("unchecked")
        List<Strip> strips = ((StripsModel<Strip>) lx.model).getStrips();
        for (int s = 0; s < strips.size(); s++) {
            boolean keep = false;
            for (int g = 0; g < StripGroups.NUM_GROUPS; g++) {
                if (enabled[g] && StripGroups.isInGroup(s, g)) {
                    keep = true;
                    break;
                }
            }
            if (!keep) {
                for (LXPoint p : strips.get(s).getPoints()) {
                    colors[p.index] = LXColor.BLACK;
                }
            }
        }
    }
}
