package com.symmetrylabs.slstudio.model;

import heronarts.lx.model.LXAbstractFixture;
import heronarts.lx.model.LXFixture;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXTransform;

import java.util.ArrayList;
import java.util.List;

public class LocatedForm extends LXAbstractFixture {
    LXFixture form;
    LXModel global_space_points;
    LXTransform placement;

    public LocatedForm(LXTransform t, LXFixture f){
        this.placement = t;
        this.form = f;
        System.out.println(f.getPoints());

        for (LXPoint p : f.getPoints()){
            this.points.add(new LXPoint(p.x + t.x(), p.y + t.y(), p.z + t.z()));
        }
    }
}
