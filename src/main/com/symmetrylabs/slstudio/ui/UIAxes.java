package com.symmetrylabs.slstudio.ui;

import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI3dComponent;
import processing.core.PGraphics;


public class UIAxes extends UI3dComponent {
    public UIAxes() {
        setVisible(false);
    }

    protected void onDraw(UI ui, PGraphics pg) {
        pg.strokeWeight(1);

        pg.stroke(255, 0, 0);
        pg.line(0, 0, 0, 1000, 0, 0);

        pg.stroke(0, 255, 0);
        pg.line(0, 0, 0, 0, 1000, 0);

        pg.stroke(0, 0, 255);
        pg.line(0, 0, 0, 0, 0, 1000);
    }
}
