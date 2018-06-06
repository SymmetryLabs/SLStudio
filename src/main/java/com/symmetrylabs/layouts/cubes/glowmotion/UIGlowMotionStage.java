package com.symmetrylabs.slstudio.ui;

import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI3dComponent;
import static processing.core.PApplet.cos;
import static processing.core.PApplet.sin;
import static processing.core.PConstants.*;
import processing.core.PGraphics;
import processing.core.PVector;

public class UIGlowMotionStage extends UI3dComponent {

    public UIGlowMotionStage() {
        
    }

    public void onDraw(UI ui, PGraphics pg) {
        pg.pushMatrix();
        pg.fill(0xff222222);
        pg.translate(287, -26, -165);
        pg.box(630, 50, 344);
        pg.popMatrix();
    }
}
