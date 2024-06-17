package com.symmetrylabs.shows.mikey;

import heronarts.lx.transform.LXTransform;
import heronarts.lx.model.LXPoint;
import java.util.ArrayList;
import java.util.List;

public class ForestFlowerModel {
    private List<LXPoint> points;
    private String id;

    public ForestFlowerModel(LXTransform transform, String id) {
        this.id = id;
        this.points = new ArrayList<>();
        createPoints(transform);
        System.out.println("Created ForestFlowerModel with " + points.size() + " points.");

    }

    private void createPoints(LXTransform transform) {
        int numPetals = 7;
        float radius = 1.0f;

        for (int i = 0; i < numPetals; i++) {
            double angle = (2 * Math.PI / numPetals) * i;
            float x = (float) (radius * Math.cos(angle));
            float y = (float) (radius * Math.sin(angle));
            
            transform.push();
            transform.translate(x, y, 0);
            LXPoint point = new LXPoint(transform.x(), transform.y(), transform.z());
            points.add(point);
            // System.out.println("Created point at: (" + point.x + ", " + point.y + ", " + point.z + ")");
            transform.pop();
        }
    }

    public List<LXPoint> getPoints() {
        // System.out.println("Returning " + points.size() + " points from ForestFlowerModel.");
        return points;
    }

    public String getId() {
        return id;
    }
}