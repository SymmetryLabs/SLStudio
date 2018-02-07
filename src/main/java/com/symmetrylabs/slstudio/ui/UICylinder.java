package com.symmetrylabs.slstudio.ui;

import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI3dComponent;
import static processing.core.PApplet.cos;
import static processing.core.PApplet.sin;
import static processing.core.PConstants.*;
import processing.core.PGraphics;
import processing.core.PVector;

/**
 * Utility class for drawing cylinders. Assumes the cylinder is oriented with
 * the y-axis vertical. Use transforms to position accordingly.
 */
public class UICylinder extends UI3dComponent {

    private final PVector[] base;
    private final PVector[] top;
    private final int detail;
    public final float len;
                private final int fill;

    public UICylinder(float radius, float len, int detail, int fill) {
        this(radius, radius, 0, len, detail, fill);
    }

    public UICylinder(float baseRadius, float topRadius, float len, int detail, int fill) {
        this(baseRadius, topRadius, 0, len, detail, fill);
    }

    public UICylinder(float baseRadius, float topRadius, float yMin, float yMax, int detail, int fill) {
        this.base = new PVector[detail];
        this.top = new PVector[detail];
        this.detail = detail;
                                this.fill = fill;
        this.len = yMax - yMin;
        for (int i = 0; i < detail; ++i) {
            float angle = i * TWO_PI / detail;
            this.base[i] = new PVector(baseRadius * cos(angle), yMin, baseRadius * sin(angle));
            this.top[i] = new PVector(topRadius * cos(angle), yMax, topRadius * sin(angle));
        }
    }

    public void onDraw(UI ui, PGraphics pg) {
        pg.beginShape(TRIANGLE_STRIP);
                                pg.fill(fill);
        for (int i = 0; i <= this.detail; ++i) {
            int ii = i % this.detail;
            pg.vertex(this.base[ii].x, this.base[ii].y, this.base[ii].z);
            pg.vertex(this.top[ii].x, this.top[ii].y, this.top[ii].z);
        }
        pg.endShape(CLOSE);
    }
}
