package com.symmetrylabs.shows.flower;

import com.symmetrylabs.slstudio.model.SLModel;
import heronarts.lx.model.LXPoint;
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
        DOWN,
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
        List<FlowerPoint> points = new ArrayList<>();
        /* D1 */ points.add(new FlowerPoint(5, -10, 0, Group.STEM, Direction.DOWN));
        /* A1 */ points.add(new FlowerPoint(5, -2, 0, Group.PETAL1, Direction.A));
        /* A2 */ points.add(new FlowerPoint(5, 2, 0, Group.PETAL2, Direction.A));
        /* B2 */ points.add(new FlowerPoint(0, 2, 5, Group.PETAL2, Direction.B));
        /* B1 */ points.add(new FlowerPoint(0, -2, 5, Group.PETAL1, Direction.B));
        /* D2 */ points.add(new FlowerPoint(-5, -10, 0, Group.STEM, Direction.DOWN));
        /* C1 */ points.add(new FlowerPoint(-5, -2, 0, Group.PETAL1, Direction.C));
        /* C2 */ points.add(new FlowerPoint(-5, 2, 0, Group.PETAL2, Direction.C));
        /* U1 */ points.add(new FlowerPoint(0, 10, 0, Group.STAMEN, Direction.UP));
        List<LXPoint> lxPoints = new ArrayList<>();
        for (FlowerPoint fp : points) {
            lxPoints.add(fp);
        }
        return new FlowerModel(lxPoints, points);
    }
}
