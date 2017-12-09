package com.symmetrylabs.pattern.raven;

import heronarts.lx.LX;
import heronarts.p3lx.P3LX;
import processing.core.PGraphics;
import processing.core.PVector;

import static processing.core.PConstants.*;


public class RKPatternTest extends P3CubeMapPattern {

    public RKPatternTest(LX lx) {

        super(
            (P3LX) lx,
            new PVector(lx.model.cx, lx.model.cy, lx.model.cz),
            new PVector(lx.model.xRange, lx.model.yRange, lx.model.zRange),
            200
        );

    }

    public void run(double deltaMs, PGraphics pg) {

        updateCubeMaps();

        pg.beginDraw();
        pg.background(0);
        pg.image(pgL, 0, faceRes);
        pg.image(pgR, faceRes * 2, faceRes);
        pg.image(pgD, faceRes, 0);
        pg.image(pgU, faceRes, faceRes * 2);
        pg.image(pgF, faceRes, faceRes);
        pg.image(pgB, faceRes * 3, faceRes);
        pg.endDraw();
    }

    void updateCubeMap(
        PGraphics pg,
        float eyeX,
        float eyeY,
        float eyeZ,
        float centerX,
        float centerY,
        float centerZ,
        float upX,
        float upY,
        float upZ
    ) {
        pg.beginDraw();
        pg.background(0);
        pg.camera(eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ);
        pg.frustum(-10, 10, -10, 10, 10, 1000);
        drawScene(pg);
        pg.endDraw();
    }

    void updateCubeMaps() {
        updateCubeMap(pgF, 0, 0, 0, 0, 0, 1, 0, 1, 0);
        updateCubeMap(pgL, 0, 0, 0, 1, 0, 0, 0, 1, 0);
        updateCubeMap(pgR, 0, 0, 0, -1, 0, 0, 0, 1, 0);
        updateCubeMap(pgB, 0, 0, 0, 0, 0, -1, 0, 1, 0);
        updateCubeMap(pgD, 0, 0, -.001f, 0, -1, 0, 0, 1, 0);
        updateCubeMap(pgU, 0, 0, -.001f, 0, 1, 0, 0, 1, 0);
    }

    void drawScene(PGraphics pg) {
        pg.textAlign(CENTER, CENTER);
        pg.textSize(96);
        pg.pushMatrix();
        pg.translate(0, 0, 100);
        pg.text("F", 0, 0);
        pg.popMatrix();
        pg.pushMatrix();
        pg.translate(0, 0, -100);
        pg.rotateY(PI);
        pg.text("B", 0, 0);
        pg.popMatrix();
        pg.pushMatrix();
        pg.translate(-100, 0, 0);
        pg.rotateY(-HALF_PI);
        pg.text("R", 0, 0);
        pg.popMatrix();
        pg.pushMatrix();
        pg.translate(100, 0, 0);
        pg.rotateY(HALF_PI);
        pg.text("L", 0, 0);
        pg.popMatrix();
        pg.pushMatrix();
        pg.translate(0, -100, 0);
        pg.rotateX(HALF_PI);
        pg.text("D", 0, 0);
        pg.popMatrix();
        pg.pushMatrix();
        pg.translate(0, 100, 0);
        pg.rotateX(-HALF_PI);
        pg.text("U", 0, 0);
        pg.popMatrix();
    }
}
