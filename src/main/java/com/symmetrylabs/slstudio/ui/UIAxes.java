package com.symmetrylabs.slstudio.ui;

import com.symmetrylabs.slstudio.SLStudio;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI3dComponent;
import processing.core.PConstants;
import processing.core.PGraphics;


/**
 * Draws a set of XYZ axes.  X axis is red, Y axis is green, Z axis is blue.
 * Axes extend to 1000 units from the origin, with a tick mark every 100 units,
 * and each tick mark extends 10 units from the axis.
 */
public class UIAxes extends UI3dComponent {
    public UIAxes() {
        setVisible(false);
    }

    protected void onDraw(UI ui, PGraphics pg) {
        pg.strokeWeight(1);
        pg.stroke(255, 0, 0);
        drawLineWithTicks(pg, 0, 0, 0, 100, 0, 0, 10);
        pg.stroke(128, 0, 0);
        drawLineWithTicks(pg, 0, 0, 0, -100, 0, 0, 10);
        pg.stroke(0, 255, 0);
        drawLineWithTicks(pg, 0, 0, 0, 0, 100, 0, 10);
        pg.stroke(0, 128, 0);
        drawLineWithTicks(pg, 0, 0, 0, 0, -100, 0, 10);
        pg.stroke(0, 0, 255);
        drawLineWithTicks(pg, 0, 0, 0, 0, 0, 100, 10);
        pg.stroke(0, 0, 128);
        drawLineWithTicks(pg, 0, 0, 0, 0, 0, -100, 10);

        pg.textFont(SLStudio.MONO_FONT.getFont());
        pg.textAlign(PConstants.CENTER, PConstants.BASELINE);
        pg.textSize(60);
        pg.pushMatrix();
        pg.rotateX((float) Math.PI);  // otherwise text appears upside-down
        pg.fill(255, 0, 0);
        pg.text("x", 1040, 0, 0);
        pg.fill(0, 255, 0);
        pg.text("y", 0, -1040, 0);
        pg.fill(0, 0, 255);
        pg.text("z", 0, 0, -1040);
        pg.popMatrix();
    }

    private void drawLineWithTicks(PGraphics pg, float x, float y, float z, float dx, float dy, float dz, int count) {
        float tx = 0;
        float ty = 0;
        float tz = 0;
        float ax = Math.abs(dx);
        float ay = Math.abs(dy);
        float az = Math.abs(dz);
        if (ax <= ay && ax <= az) {
            tx = 10;
        } else if (az <= ay) {
            tz = 10;
        } else {
            ty = 10;
        }
        for (int i = 0; i < count; i++) {
            float nx = x + dx, ny = y + dy, nz = z + dz;
            pg.line(x, y, z, nx, ny, nz);
            pg.line(nx - tx, ny - ty, nz - tz, nx + tx, ny + ty, nz + tz);
            x += dx;
            y += dy;
            z += dz;
        }
    }
}
