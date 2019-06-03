package com.symmetrylabs.slstudio.ui.v2;

import com.badlogic.gdx.math.collision.Ray;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import java.util.ArrayList;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;
import heronarts.lx.model.LXFixture;
import com.symmetrylabs.slstudio.model.SLModel;
import java.util.Iterator;
import com.google.common.base.Preconditions;
import java.util.Comparator;


public class ModelPicker implements Window {
    private static final float MIN_PICK_WORLD_DIST = 2;

    public static final class PickPoint {
        public final LXPoint point;
        public final Vector3 vec;
        public String description;
        protected Vector3 hitPoint = new Vector3();

        PickPoint(LXPoint p) {
            point = p;
            vec = new Vector3(p.x, p.y, p.z);
        }

        @Override
        public String toString() {
            return description == null ? "orphaned pick point" : description;
        }
    }

    private final SLCamera cam;
    private final LXModel model;
    private final ArrayList<PickPoint> pickPoints;
    private PickPoint currentHover = null;
    public boolean enabled = false;

    public ModelPicker(LXModel model, SLCamera cam) {
        this.cam = cam;
        this.model = model;
        this.pickPoints = new ArrayList<>();
        buildPickPoints();
    }

    public PickPoint getHovered() {
        if (!enabled) {
            return null;
        }
        return currentHover;
    }

    protected void buildPickPoints() {
        pickPoints.clear();
        for (LXPoint p : model.points) {
            PickPoint pp = new PickPoint(p);
            Preconditions.checkState(p.index == pickPoints.size());
            pickPoints.add(pp);
        }
        recursePickPoints(-1, null, model);
    }

    protected void recursePickPoints(int index, String prefix, LXFixture fix) {
        String id = null;
        if (fix instanceof SLModel) {
            SLModel slm = (SLModel) fix;
            String mid = slm.modelId;
            if (mid != null) {
                id = mid;
            }
        }
        if (id == null) {
            String className = fix.getClass().getSimpleName();
            if (index < 0) {
                id = className;
            } else {
                id = String.format("%s %d", className, index);
            }
        }
        if (prefix == null) {
            prefix = id;
        } else {
            prefix = prefix + " / " + id;
        }

        /* pre-order traversal means that if these points are owned by a deeper model than this one,
           those points will be re-assigned on a later recursion. */
        int pindex = 0;
        for (LXPoint pt : fix.getPoints()) {
            pickPoints.get(pt.index).description = String.format("%s / Point %d", prefix, pindex++);
        }

        if (fix instanceof SLModel) {
            SLModel mod = (SLModel) fix;
            Iterator<? extends LXModel> children = mod.getChildren();
            int childIndex = 0;
            while (children.hasNext()) {
                recursePickPoints(childIndex++, prefix, children.next());
            }
        }
    }

    public void mouseMoved(int x, int y) {
        if (!enabled) {
            return;
        }

        Ray ray = cam.getPickRay(x, y);
        float nearestNormalProjection = 0;
        PickPoint nearest = null;

        for (PickPoint p : pickPoints) {
            if (Intersector.intersectRaySphere(ray, p.vec, MIN_PICK_WORLD_DIST, p.hitPoint)) {
                p.hitPoint.sub(p.vec);
                float d = Math.abs(p.hitPoint.dot(ray.direction));
                if (d > nearestNormalProjection) {
                    nearest = p;
                    nearestNormalProjection = d;
                }
            }
        }
        currentHover = nearest;
    }

    @Override
    public void draw() {
        if (!enabled) {
            return;
        }
        if (currentHover != null) {
            UI.beginTooltip();
            UI.text(currentHover.description);
            UI.endTooltip();
        }
    }
}
