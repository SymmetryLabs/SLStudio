package com.symmetrylabs.slstudio.model;

import com.google.common.collect.Lists;
import heronarts.lx.model.LXAbstractFixture;
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXTransform;

import java.util.ArrayList;
import java.util.List;

public class CandyBar extends LXAbstractFixture{
    private int BAR_LENGTH_MID = 139;

    public CandyBar(){
        LXTransform t = new LXTransform();

        // the firt strip
        StripForm strip0 = new StripForm("strip1", BAR_LENGTH_MID, 1);
        points.addAll(strip0.getPoints());

        // the second strip
        // we can use the same strip form from above and just translate it and reflect it.
        t.translate(1.0f,0,0); // translate
        LocatedForm locateStrip1 = new LocatedForm(t, strip0);
        List<LXPoint> reverse_these = Lists.reverse(locateStrip1.getPoints()); // reflect
        points.addAll(reverse_these);
    }
}
