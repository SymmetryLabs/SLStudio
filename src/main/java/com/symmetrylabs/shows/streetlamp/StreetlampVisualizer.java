package com.symmetrylabs.shows.streetlamp;

import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.p3lx.P3LX;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI3dComponent;
import processing.core.PGraphics;

public class StreetlampVisualizer extends UI3dComponent {
    private static final float TOWER_H = 14 * 12;
    private static final float TOP_PLANE = 5 * StreetlampModel.ELEMENT_Y_DELTA;

    private static final int TOWER_COLOR = 0xFF555555;

    private final P3LX lx;
    private final LXModel model;

    public StreetlampVisualizer(P3LX lx) {
        this.lx = lx;
        this.model = lx.model;
    }

    @Override
    protected void onDraw(UI ui, PGraphics pg) {
        int[] colors = lx.getColors();
        pg.noStroke();
        pg.fill(TOWER_COLOR);

        for (int i = 0; i < 6; i++) {
            pg.pushMatrix();
            pg.translate(
                StreetlampModel.ELEMENT_X_SIZE * i,
                -(TOWER_H / 2 - TOP_PLANE),
                StreetlampModel.ELEMENT_Z_SIZE * (5 - i));
            pg.box(StreetlampModel.ELEMENT_X_SIZE, TOWER_H, StreetlampModel.ELEMENT_Z_SIZE);
            pg.popMatrix();
        }

        for (LXPoint p : model.points) {
            int c = colors[p.index];
            pg.pushMatrix();
            pg.translate(p.x, p.y, p.z);
            pg.fill(c);
            pg.box(
                StreetlampModel.ELEMENT_X_SIZE,
                StreetlampModel.ELEMENT_Y_DELTA,
                StreetlampModel.ELEMENT_Z_SIZE);
            pg.fill(TOWER_COLOR);
            float fillTowerHeight = TOP_PLANE - p.y - StreetlampModel.ELEMENT_Y_DELTA / 2f;
            pg.translate(
                0,
                fillTowerHeight / 2f + StreetlampModel.ELEMENT_Y_DELTA / 2f,
                0);
            pg.box(
                StreetlampModel.ELEMENT_X_SIZE,
                fillTowerHeight,
                StreetlampModel.ELEMENT_Z_SIZE);
            pg.popMatrix();
        }
    }
}
