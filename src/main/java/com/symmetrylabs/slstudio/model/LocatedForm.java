package com.symmetrylabs.slstudio.model;

import heronarts.lx.model.LXAbstractFixture;
import heronarts.lx.model.LXFixture;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXTransform;

import java.util.ArrayList;
import java.util.List;

public class LocatedForm extends LXAbstractFixture {
    LXModel global_space_points;
    LXTransform placement;

    //unique id
    public final String uid;

    // used to create unique ids
    private static int uid_counter = 0;

    public LocatedForm(LXTransform t, Form f){
        // creates a unique (to this model) identifier as the name of the Form concatenated with an incrementing id
        this.uid = f.name.concat(String.valueOf(uid_counter++));
        this.placement = t;

        for (LXPoint p : f.getPoints()){
            addPoint(new LXPoint(p.x + t.x(), p.y + t.y(), p.z + t.z()));
        }
    }
}
