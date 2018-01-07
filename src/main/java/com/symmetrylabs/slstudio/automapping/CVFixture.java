package com.symmetrylabs.slstudio.automapping;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import com.symmetrylabs.slstudio.mappings.CubesLayout;
import org.apache.commons.math3.util.FastMath;

import processing.core.PGraphics;
import processing.core.PMatrix;
import processing.core.PMatrix3D;
import processing.core.PVector;

import heronarts.lx.model.LXPoint;
import heronarts.lx.color.LXColor;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI3dComponent;

import com.symmetrylabs.slstudio.util.MathUtils;

import static com.symmetrylabs.slstudio.util.Utils.millis;
import static processing.core.PConstants.CENTER;
import static processing.core.PConstants.HSB;
import static processing.core.PConstants.SHAPE;

public class CVFixture extends UI3dComponent implements Comparable<CVFixture> {

    public String id;

    PMatrix3D mat;
    PVector rvec;
    PVector tvec;
    int ms;

    boolean flashOn = false;

    boolean selected = false;

    private List<LXPoint> points;
    private float w;
    private float h;
    private float d;

    float minX;
    float maxX;
    float minY;
    float maxY;
    float minZ;
    float maxZ;

    PVector currentCorner = null;

    private final Automapper automapper;

    void printMat(PMatrix3D mat) {
        System.out.println("==================");
        float target[] = new float[16];
        mat.get(target);

        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                int i = (r * 4) + c;
                System.out.print(target[i]);
                System.out.print(" ");
            }
            System.out.println(" ");
        }
        System.out.println("==================");
    }


    public CVFixture(Automapper automapper, List<List<Double>> rawMatrix, List<Double> rvec, List<Double> tvec, String id) {

        this.automapper = automapper;

        int rows = rawMatrix.size();
        int cols = rawMatrix.get(0).size();

        float[] values = new float[rows * cols];

        int i = 0;
        for (List<Double> row : rawMatrix) {
            for (Double col : row) {
                double v_ = col;
                float v = (float)v_;
                values[i++] = v;
            }
        }

        this.id = id;

        mat = new PMatrix3D();
        mat.set(values);

        this.rvec = new PVector(floatify(rvec.get(0)), floatify(rvec.get(1)), floatify(rvec.get(2)));
        this.tvec = new PVector(floatify(tvec.get(0)), floatify(tvec.get(1)), floatify(tvec.get(2)));

        ms = millis();

        LXPoint[] raw = automapper.getRawPointsForId(id);


        points = new ArrayList<>(Arrays.asList(raw));

        minX = points.get(0).x;
        maxX = points.get(0).x;
        minY = points.get(0).y;
        maxY = points.get(0).y;
        minZ = points.get(0).z;
        maxZ = points.get(0).z;

        for (LXPoint p : points) {
            minX = FastMath.min(p.x, minX);
            maxX = FastMath.max(p.x, maxX);
            minY = FastMath.min(p.y, minY);
            maxY = FastMath.max(p.y, maxY);
            minZ = FastMath.min(p.z, minZ);
            maxZ = FastMath.max(p.z, maxZ);
        }

        w = maxX - minX;
        h = maxY - minY;
        d = maxZ - minZ;
    }

    float floatify(Double v) {
        double v_ = v;
        return (float)v_;
    }


    List<List<Double>> getRawMatrix() {
        List<List<Double>> raw = new ArrayList<>();

        float target[] = new float[16];
        mat.get(target);

        for (int r = 0; r < 4; r++) {
            List<Double> row = new ArrayList<>();
            for (int c = 0; c < 4; c++) {
                int i = (r * 4) + c;
                row.add((double)target[i]);
            }
            raw.add(row);
        }

        return raw;
    }

    public void setSelected(boolean sel) {
        ms = millis();
        flashOn = true;
        selected = sel;
    }

    @Override
    public int compareTo(CVFixture other) {
        int me = Integer.parseInt(getLabel());
        int o = Integer.parseInt(other.getLabel());

        if (me < o) return -1;
        if (me > o) return 1;
        return 0;
    }


    public String getLabel() {
        if (CubesLayout.macToPhysid.containsKey(id)) {
            return CubesLayout.macToPhysid.get(id);
        } else {
            return id.substring(id.length() - 3);
        }
    }

    int getColor() {
        int hash = id.hashCode();

        // println("HASHER", id, abs(hash) % 256);

        return LXColor.hsb(MathUtils.abs(hash * 17) % 360, 100, 100);
    }

    void drawSideIfSelected(PGraphics pg, String label) {
        if (!selected) return;

        pg.translate(0, 7, 0);
        pg.textSize(5);
        pg.text(label, 0, 0, 0);
        pg.textSize(8);
        // pg.translate()
    }




    @Override
        protected void onDraw(UI ui, PGraphics pg) {

        pg.pushMatrix();


        PMatrix3D copy = new PMatrix3D(mat);

        PMatrix3D inverter = new PMatrix3D();
        inverter.m00 = -1;
        inverter.m22 = -1;


        copy.preApply(inverter);



        pg.applyMatrix(copy);


        if (millis() - ms > 500) {
            flashOn = !flashOn;
            ms = millis();
        }

        int mainColor;

        pg.noFill();
        pg.strokeWeight(3);
        if (flashOn && selected) {
            mainColor = 255;
        } else {
            pg.colorMode(HSB, 360, 100, 100);
            mainColor = getColor();
        }
        pg.stroke(mainColor);

        PVector a = new PVector(minX, minY, minZ);
        PVector b = new PVector(maxX, minY, minZ);

        float[] dX = {minX, maxX};
        float[] dY = {minY, maxY};
        float[] dZ = {minZ, maxZ};

        for (float startX : dX) {
        for (float endX : dX) {
        for (float startY : dY) {
        for (float endY : dY) {
        for (float startZ : dZ) {
        for (float endZ : dZ) {
            boolean drawX = startX != endX && startY == endY && startZ == endZ;
            boolean drawY = startX == endX && startY != endY && startZ == endZ;
            boolean drawZ = startX == endX && startY == endY && startZ != endZ;

            if (drawX || drawY || drawZ) {
                pg.line(startX, startY, startZ, endX, endY, endZ);
            }

        }
        }
        }
        }
        }
        }

        pg.pushMatrix();
        pg.pushStyle();

        PMatrix before = pg.getMatrix();

        boolean[] signs = {true, false};

        PVector[] corners = new PVector[8];

        int i = 0;
        for (boolean x : signs) {
            for (boolean y : signs) {
                for (boolean z : signs) {
                    corners[i++] = new PVector(x ? minX : maxX, y ? minY : maxY, z ? minZ : maxZ);
                }
            }
        }

        PVector minCorner = null;
        PVector target = new PVector();
        float minDist = Float.POSITIVE_INFINITY;
        for (PVector corner : corners) {
            before.mult(corner, target);
            float d = target.magSq();
            if (d < minDist) {
                minCorner = corner;
                minDist = d;
            }
        }

        float cDist = Float.POSITIVE_INFINITY;
        if (currentCorner != null) {
            before.mult(currentCorner, target);
            cDist = target.magSq();
        }



        if (minDist < (cDist - 750)) {
            currentCorner = minCorner;
        }



        pg.translate(currentCorner.x, currentCorner.y, currentCorner.z);

        PMatrix current = pg.getMatrix();
        PMatrix3D currentCopy = new PMatrix3D(current);
        current.invert();

        pg.applyMatrix(current);
        pg.translate(currentCopy.m03, currentCopy.m13, currentCopy.m23);
        // pg.noFill();
        // pg.noStroke();
        pg.textMode(SHAPE);
        pg.textSize(5);
        pg.textAlign(CENTER, CENTER);

        if (!automapper.hideLabels) {
            pg.fill(ui.theme.getDarkBackgroundColor());
            pg.noStroke();
            pg.box(10, 7, 1);
            pg.fill(mainColor);
            pg.stroke(mainColor);
            pg.text(getLabel(), 0, 0, 0.50000001f);
        }
        // pg.sphere(0.5);
        pg.popStyle();
        pg.popMatrix();

        pg.popMatrix();
    }
}
