package com.symmetrylabs.slstudio.ui;

import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI3dComponent;

import processing.core.PGraphics;
import static processing.core.PConstants.CLOSE;
import static processing.core.PConstants.TRIANGLE_STRIP;
import org.apache.commons.math3.util.FastMath;

import com.symmetrylabs.slstudio.util.MathUtils;

public class UIDaughtryStage extends UI3dComponent {

    private final float[][] coordinates = new float[][] {
        new float[] {-120, 0, -200, 70},
        new float[] {390, 0, -200, 70},

        new float[] {-50, 0, -120, 120},
        new float[] {320, 0, -120, 120},
    };

    protected void onDraw(UI ui, PGraphics pg) {
        pg.pushMatrix();
        pg.translate(137, 1, -125);
        pg.stroke(15);
        pg.fill(28);
        pg.box(100, 65, 100);
        pg.popMatrix();

        pg.pushMatrix();
        pg.translate(140, -5, -170);
        pg.fill(34);
        pg.box(610, 5, 580);
        pg.popMatrix();

        for (float[] coordinate : coordinates) {
                pg.pushMatrix();
                pg.translate(coordinate[0], coordinate[1], coordinate[2]);
                pg.rotateX((float)(FastMath.PI/2.0));

                pg.stroke(20);
                pg.fill(40);
                pg.box(20, 20, 3);
                drawCylinder(pg, 10, 2, coordinate[3]);

                pg.popMatrix();
        }
    }

    private void drawCylinder(PGraphics pg, int sides, float r, float h) {
        float angle = 360 / sides;
        pg.noStroke();
        pg.fill(42);
        pg.beginShape();
        for (int i = 0; i < sides; i++) {
            float x = MathUtils.cos(MathUtils.radians(i * angle)) * r;
            float y = MathUtils.sin(MathUtils.radians(i * angle)) * r;
            pg.vertex(x, y, 0);
        }
        pg.endShape(CLOSE);
        pg.beginShape();
        for (int i = 0; i < sides; i++) {
            float x = MathUtils.cos(MathUtils.radians(i * angle)) * r;
            float y = MathUtils.sin(MathUtils.radians(i * angle)) * r;
            pg.vertex(x, y, -h);
        }
        pg.endShape(CLOSE);
        pg.beginShape(TRIANGLE_STRIP);
        for (int i = 0; i < sides + 1; i++) {
                float x = MathUtils.cos(MathUtils.radians(i * angle)) * r;
                float y = MathUtils.sin(MathUtils.radians(i * angle)) * r;
                pg.vertex(x, y, -h);
                pg.vertex(x, y, 0);
        }
        pg.endShape(CLOSE);
    } 
}