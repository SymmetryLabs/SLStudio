package com.symmetrylabs.slstudio.objimporter;

import java.io.*;
import java.util.*;

import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXTransform;

import com.symmetrylabs.slstudio.model.SLModel;
import static com.symmetrylabs.util.MathConstants.*;
import static com.symmetrylabs.util.DistanceConstants.*;


class ObjModelBuilder {

    private String name;

    public ObjConfig config;

    private List<float[]> vertices;

    private float xMin = 0, xMax = 0;
    private float yMin = 0, yMax = 0;
    private float zMin = 0, zMax = 0;

    public ObjModelBuilder(File file, ObjConfigReader configReader) {
        this.name = file.getName().substring(0, file.getName().lastIndexOf('.'));
        this.config = configReader.readConfig(name);

        readVertices(file);
        calculateBounds();
        scaleFixture();
        centerFixture();
    }

    private void readVertices(File file) {
        this.vertices = new ArrayList<>();

        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));

            String line;
            while ((line = bufferedReader.readLine()) != null) {

                // if it's a vertex
                if (line.startsWith("v ")) {
                    String[] vertexString = line.substring(2).split("\\s+");

                    float x = Float.parseFloat(vertexString[0]) * INCHES_PER_METER;
                    float y = Float.parseFloat(vertexString[1]) * INCHES_PER_METER;
                    float z = Float.parseFloat(vertexString[2]) * INCHES_PER_METER;

                    this.vertices.add(new float[]{x, y, z});
                }
            }
        } catch (Exception e) {
            System.out.println("Problem reading vertices in obj file: " + name);
        }
    }

    private void calculateBounds() {
        for (float[] vertex : this.vertices) {
            if (vertex[0] < xMin) xMin = vertex[0];
            if (vertex[0] > xMax) xMax = vertex[0];
            if (vertex[1] < yMin) yMin = vertex[1];
            if (vertex[1] > yMax) yMax = vertex[1];
            if (vertex[2] < zMin) zMin = vertex[2];
            if (vertex[2] > zMax) zMax = vertex[2];
        }
    }

    private void scaleFixture() {
        for (float[] vertex : this.vertices) {
            vertex[0] = config.scale * (vertex[0] += (-xMin));
            vertex[1] = config.scale * (vertex[1] += (-yMin));
            vertex[2] = config.scale * (vertex[2] += (-zMin));
        }
    }

    private void centerFixture() {
        calculateBounds();
        float translateX = (-Math.abs(xMax - xMin) / 2) + (-xMin);
        float translateY = (-Math.abs(yMax - yMin) / 2) + (-yMin);
        float translateZ = (-Math.abs(zMax - zMin) / 2) + (-zMin);

        for (float[] vertex : this.vertices) {
            vertex[0] += translateX;
            vertex[1] += translateY;
            vertex[2] += translateZ;
        }
    }

    public SLModel buildModel(LXTransform transform) {
        List<LXPoint> points = new ArrayList();

        for (float[] vertex : this.vertices) {
            transform.push();
            transform.translate(config.x, config.y, config.z);
            transform.rotateX(config.xRotation * PI / 180f);
            transform.rotateY(config.yRotation * PI / 180f);
            transform.rotateZ(config.zRotation * PI / 180f);

            transform.translate(vertex[0], vertex[1], vertex[2]);

            LXPoint point = new LXPoint(transform.x(), transform.y(), transform.z());
            points.add(point);

            transform.pop();
        }

        System.out.println("Created a fixture with obj file: " + name);
        return new SLModel(points);
    }
}
