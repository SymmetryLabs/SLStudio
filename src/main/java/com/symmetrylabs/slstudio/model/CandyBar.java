package com.symmetrylabs.slstudio.model;

import com.google.common.collect.Lists;
import heronarts.lx.LX;
import heronarts.lx.model.LXAbstractFixture;
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXTransform;

import java.util.ArrayList;
import java.util.List;

public class CandyBar extends Form{
    private int BAR_LENGTH_MID = 139;

    public final String id;

    private List<LXPoint> local_space_points = new ArrayList<>();

    public CandyBar(String name){
        super(name);
        this.id = name;
        LXTransform t = new LXTransform();

        // the firt strip
        StripForm strip0 = new StripForm("strip1", BAR_LENGTH_MID, 1);
        local_space_points.addAll(strip0.getPoints());

        // the second strip
        // we can use the same strip form from above and just translate it and reflect it.
        t.translate(1.0f,0,0); // translate
        LocatedForm locateStrip1 = new LocatedForm(t, strip0);
        List<LXPoint> reverse_these = Lists.reverse(locateStrip1.getPoints()); // reflect
        local_space_points.addAll(reverse_these);

        // add the points we just collected
        for (LXPoint p : local_space_points){
            this.addPoint(p);
        }
    }
}
