package com.symmetrylabs.shows.hhgarden;

import com.symmetrylabs.slstudio.model.SLModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXVector;
import java.util.ArrayList;
import java.util.List;

public class FlowerModel extends SLModel {
    public enum Group {
        STAMEN,
        PETAL1,
        PETAL2,
        STEM,
    }

    public enum Direction {
        A,
        B,
        C,
        UP,
    }

    public static class FlowerPoint extends LXPoint {
        public final Group group;
        public final Direction direction;

        public FlowerPoint(Group g, Direction d) {
            super(0, 0, 0);
            group = g;
            direction = d;
        }

        public FlowerPoint(float x, float y, float z, Group g, Direction d) {
            super(x, y, z);
            group = g;
            direction = d;
        }

        void setBaseLocation(LXVector base) {
            switch (group) {
            case STEM: z = STZ; break;
            case PETAL1: z = P1Z; break;
            case PETAL2: z = P2Z; break;
            case STAMEN: z = UPZ; break;
            }
            switch (direction) {
            case UP: x = 0; y = 0; break;
            case A: x = AX; y = AY; break;
            case B: x = BX; y = BY; break;
            case C: x = CX; y = CY; break;
            }
            x += base.x;
            y += base.y;
            z += base.z;
            update();
        }
    }

    private final List<FlowerPoint> flowerPoints;
    private final FlowerData flowerData;

    public FlowerModel() {
        super();
        flowerPoints = new ArrayList<>();
        flowerData = null;
    }

    private FlowerModel(List<LXPoint> points, FlowerData flowerData) {
        super(points);
        flowerPoints = new ArrayList<>(points.size());
        for (int i = 0; i < points.size(); i++) {
            flowerPoints.add((FlowerPoint) points.get(i));
        }
        this.flowerData = flowerData;
    }

    public List<FlowerPoint> getFlowerPoints() {
        return flowerPoints;
    }

    public FlowerData getFlowerData() {
        return flowerData;
    }

    public void onDataUpdated() {
        for (FlowerPoint fp : flowerPoints) {
            fp.setBaseLocation(flowerData.location);
        }
        update(true, true);
    }

    public static FlowerModel create() {
        return create(new FlowerData(new LXVector(0, 0, 0)));
    }

    /* ABC XZ are chosen so that they form an equilaterial triangle about the origin */
    private static final float AX = (float) (4. / Math.tan(60. / 180. * Math.PI));
    private static final float BX = 0;
    private static final float CX = (float) (-4. / Math.tan(60. / 180. * Math.PI));
    private static final float AY = -4.f / 3.f;
    private static final float BY = 8.f / 3.f;
    private static final float CY = -4.f / 3.f;
    private static final float UPZ = 2.f;
    private static final float P2Z = 1.f;
    private static final float P1Z = -1.f;
    private static final float STZ = -4.f;

    public static FlowerModel create(FlowerData fd) {
        List<FlowerPoint> points = new ArrayList<>();

        points.add(new FlowerPoint(Group.STEM, Direction.A));
        points.add(new FlowerPoint(Group.PETAL1, Direction.A));
        points.add(new FlowerPoint(Group.PETAL2, Direction.A));
        points.add(new FlowerPoint(Group.STAMEN, Direction.UP));
        points.add(new FlowerPoint(Group.PETAL2, Direction.B));
        points.add(new FlowerPoint(Group.PETAL1, Direction.B));
        points.add(new FlowerPoint(Group.STEM, Direction.B));
        points.add(new FlowerPoint(Group.STEM, Direction.C));
        points.add(new FlowerPoint(Group.PETAL1, Direction.C));
        points.add(new FlowerPoint(Group.PETAL2, Direction.C));
        for (FlowerPoint fp : points) {
            fp.setBaseLocation(fd.location);
        }

        List<LXPoint> lxPoints = new ArrayList<>();
        for (FlowerPoint fp : points) {
            lxPoints.add(fp);
        }
        return new FlowerModel(lxPoints, fd);
    }
}
