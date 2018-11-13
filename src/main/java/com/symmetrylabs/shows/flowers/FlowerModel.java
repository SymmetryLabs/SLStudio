package com.symmetrylabs.shows.flowers;

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
            setBaseLocation(new LXVector(0, 0, 0));
        }

        public FlowerPoint(float x, float y, float z, Group g, Direction d) {
            super(x, y, z);
            group = g;
            direction = d;
        }

        void setBaseLocation(LXVector base) {
            switch (group) {
            case STEM: y = STY; break;
            case PETAL1: y = P1Y; break;
            case PETAL2: y = P2Y; break;
            case STAMEN: y = UPY; break;
            }
            y += base.y;
            switch (direction) {
            case UP: x = 0; z = 0; break;
            case A: x = AX; z = AZ; break;
            case B: x = BX; z = BZ; break;
            case C: x = CX; z = CZ; break;
            }
            x += base.x;
            z += base.z;
        }
    }

    private final List<FlowerPoint> flowerPoints;
    private final FlowerData flowerData;

    public FlowerModel() {
        super();
        flowerPoints = new ArrayList<>();
        flowerData = null;
    }

    public FlowerModel(
        List<LXPoint> points, List<FlowerPoint> flowerPoints, FlowerData flowerData) {
        super(points);
        this.flowerPoints = flowerPoints;
        this.flowerData = flowerData;
    }

    public List<FlowerPoint> getFlowerPoints() {
        return flowerPoints;
    }

    public FlowerData getFlowerData() {
        return flowerData;
    }

    public static FlowerModel create() {
        return create(new FlowerData(new LXVector(0, 0, 0)));
    }

    /* ABC XZ are chosen so that they form an equilaterial triangle about the origin */
    private static final float AX = (float) (4. / Math.tan(60. / 180. * Math.PI));
    private static final float BX = 0;
    private static final float CX = (float) (-4. / Math.tan(60. / 180. * Math.PI));
    private static final float UPY = 2.f;
    private static final float P2Y = 1.f;
    private static final float P1Y = -1.f;
    private static final float STY = -4.f;
    private static final float AZ = -4.f / 3.f;
    private static final float BZ = 8.f / 3.f;
    private static final float CZ = -4.f / 3.f;

    public static FlowerModel create(FlowerData fd) {
        List<FlowerPoint> points = new ArrayList<>();

        points.add(new FlowerPoint(Group.STEM, Direction.A));
        points.add(new FlowerPoint(Group.PETAL1, Direction.A));
        points.add(new FlowerPoint(Group.PETAL2, Direction.A));
        points.add(new FlowerPoint(Group.PETAL2, Direction.B));
        points.add(new FlowerPoint(Group.PETAL1, Direction.B));
        points.add(new FlowerPoint(Group.STEM, Direction.B));
        points.add(new FlowerPoint(Group.STEM, Direction.C));
        points.add(new FlowerPoint(Group.PETAL1, Direction.C));
        points.add(new FlowerPoint(Group.PETAL2, Direction.C));
        points.add(new FlowerPoint(Group.STAMEN, Direction.UP));
        for (FlowerPoint fp : points) {
            fp.setBaseLocation(fd.location);
        }

        List<LXPoint> lxPoints = new ArrayList<>();
        for (FlowerPoint fp : points) {
            lxPoints.add(fp);
        }
        return new FlowerModel(lxPoints, points, fd);
    }
}
