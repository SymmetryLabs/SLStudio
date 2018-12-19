package com.symmetrylabs.slstudio.pattern.instruments;

import com.symmetrylabs.color.Ops16;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXVector;

public class MarkUtils {
    static Random random = new Random();

    public static <T> T randomElement(List<T> elements) {
        return elements.get(random.nextInt(elements.size()));
    }

    public static LXVector randomXyDisc() {
        while (true) {
            float x = random.nextFloat() * 2 - 1;
            float y = random.nextFloat() * 2 - 1;
            if (x*x + y*y < 1) {
                return new LXVector(x, y, 0);
            }
        }
    }

    public static LXVector randomSphere() {
        while (true) {
            float x = random.nextFloat() * 2 - 1;
            float y = random.nextFloat() * 2 - 1;
            float z = random.nextFloat() * 2 - 1;
            if (x*x + y*y + z*z < 1) {
                return new LXVector(x, y, z);
            }
        }
    }

    public static double randomVariation() {
        return random.nextDouble() * 2 - 1;
    }

    public static List<LXPoint> getAllPointsWithin(LXModel model, LXVector center, double radius) {
        List<LXPoint> points = new ArrayList<>();
        LXVector p = new LXVector(0, 0, 0);
        for (LXPoint point : model.points) {
            p.x = point.x;
            p.y = point.y;
            p.z = point.z;
            if (Math.abs(p.x - center.x) < radius && Math.abs(p.y - center.y) < radius) {
                if (center.dist(p) < radius) {
                    points.add(point);
                }
            }
        }
        return points;
    }

    public static void addColor(long[] colors, int index, long color) {
        long c = colors[index];
        colors[index] = Ops16.rgba(
            Ops16.red(c) + Ops16.red(color),
            Ops16.green(c) + Ops16.green(color),
            Ops16.blue(c) + Ops16.blue(color),
            Ops16.alpha(c) + Ops16.alpha(color)
        );
    }

    public static void addColor(long[] colors, int index, long color, double alpha) {
        long c = colors[index];
        colors[index] = Ops16.rgba(
            Ops16.red(c) + (int) (Ops16.red(color) * alpha),
            Ops16.green(c) + (int) (Ops16.green(color) * alpha),
            Ops16.blue(c) + (int) (Ops16.blue(color) * alpha),
            Ops16.alpha(c) + (int) (Ops16.alpha(color) * alpha)
        );
    }
}
