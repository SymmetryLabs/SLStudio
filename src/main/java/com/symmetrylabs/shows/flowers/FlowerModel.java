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

        public FlowerPoint(float x, float y, float z, Group g, Direction d) {
            super(x, y, z);
            group = g;
            direction = d;
        }
    }

    private final List<FlowerPoint> flowerPoints;

    public FlowerModel() {
        super();
        flowerPoints = new ArrayList<>();
    }

    public FlowerModel(List<LXPoint> points, List<FlowerPoint> flowerPoints) {
        super(points);
        this.flowerPoints = flowerPoints;
    }

    public List<FlowerPoint> getFlowerPoints() {
        return flowerPoints;
    }

    public static FlowerModel create() {
        return create(new LXVector(0, 0, 0));
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

    public static FlowerModel create(LXVector base) {
        List<FlowerPoint> points = new ArrayList<>();

        points.add(
            new FlowerPoint(
                base.x + AX, base.y + STY, base.z + AZ, Group.STEM, Direction.A));
        points.add(
            new FlowerPoint(
                base.x + AX, base.y + P1Y, base.z + AZ, Group.PETAL1, Direction.A));
        points.add(
            new FlowerPoint(
                base.x + AX, base.y + P2Y, base.z + AZ, Group.PETAL2, Direction.A));
        points.add(
            new FlowerPoint(
                base.x + BX, base.y + P2Y, base.z + BZ, Group.PETAL2, Direction.B));
        points.add(
            new FlowerPoint(
                base.x + BX, base.y + P1Y, base.z + BZ, Group.PETAL1, Direction.B));
        points.add(
            new FlowerPoint(
                base.x + BX, base.y + STY, base.z + BZ, Group.STEM, Direction.B));
        points.add(
            new FlowerPoint(
                base.x + CX, base.y + STY, base.z + CZ, Group.STEM, Direction.C));
        points.add(
            new FlowerPoint(
                base.x + CX, base.y + P1Y, base.z + CZ, Group.PETAL1, Direction.C));
        points.add(
            new FlowerPoint(
                base.x + CX, base.y + P2Y, base.z + CZ, Group.PETAL2, Direction.C));
        points.add(
            new FlowerPoint(
                base.x,  base.y + UPY, base.z, Group.STAMEN, Direction.UP));

        List<LXPoint> lxPoints = new ArrayList<>();
        for (FlowerPoint fp : points) {
            lxPoints.add(fp);
        }
        return new FlowerModel(lxPoints, points);
    }
}
