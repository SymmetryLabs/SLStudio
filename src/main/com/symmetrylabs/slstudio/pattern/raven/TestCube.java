package com.symmetrylabs.slstudio.pattern.raven;

import heronarts.lx.LX;
import heronarts.p3lx.P3LX;
import processing.core.PGraphics;
import processing.core.PVector;

import static processing.core.PConstants.CORNERS;
import static processing.core.PConstants.LINES;

/**
 * An example of a cube-mapped pattern.  This paints solid colours on the six faces of the cube, with markers to show
 * their orientation, black stripes to show how the texture is distorted by the projection, and grey lines to show how
 * the cube is formed by folding up the flat cubemap image.
 */
public class TestCube extends P3CubeMapPattern {
    public TestCube(LX lx) {
        // These parameters project this cubemap onto one of the 3/4-height suns.
        // See buildModel() in Mappings.pde for the sun positions.
        super(
            (P3LX) lx,
            new PVector(55 * 12 + 8 * 12, 4 * 12, 2 * 12 + 0.3f * 8 * 12),
            new PVector(16 * 12, 16 * 12, 16 * 12 * 0.3f),
            100
        );
    }

    public void run(double deltaMs, PGraphics pg) {
        pg.beginDraw();
        pg.background(0);

        pg.rectMode(CORNERS);
        pg.strokeWeight(0);

        // Fill the faces (red for L/R, green for U/D, blue for F/B).
        // To indicate orientation, add a white square at the +x, +y, +z corners.
        pg.fill(255, 0, 0);
        pg.rect(0, 100, 100, 200);  // left face
        pg.rect(200, 100, 300, 200);  // right face
        pg.fill(255);
        pg.rect(10, 110, 30, 130);  // +y, +z highlight
        pg.rect(270, 110, 290, 130);  // +y, +z highlight

        pg.fill(0, 255, 0);
        pg.rect(100, 0, 200, 100);  // up face
        pg.rect(100, 200, 200, 300);  // down face
        pg.fill(255);
        pg.rect(170, 10, 190, 30);  // +x, +z highlight
        pg.rect(170, 270, 190, 290);  // +x, +z highlight

        pg.fill(0, 0, 255);
        pg.rect(100, 100, 200, 200);  // front face
        pg.rect(300, 100, 400, 200);  // back face
        pg.fill(255);
        pg.rect(170, 110, 190, 130);  // +x, +y highlight
        pg.rect(310, 110, 330, 130);  // +x, +y highlight

        // Draw a black grid so that all faces are striped.
        pg.stroke(0);
        pg.strokeWeight(6);
        pg.beginShape(LINES);
        for (int i = 10; i < 400; i += 20) {
            pg.line(0, i, 400, i);
            pg.line(i, 0, i, 300);
        }
        pg.endShape();

        // A grey cross shows how the cube folds up, with the cross centered on the front face.
        pg.stroke(128);
        pg.strokeWeight(10);
        pg.line(40, 150, 360, 150);
        pg.line(150, 40, 150, 260);
        pg.endDraw();
    }
}
