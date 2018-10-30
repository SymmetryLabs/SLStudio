package com.symmetrylabs.layouts.tree.ui;

import com.symmetrylabs.layouts.tree.TreeModel;
import static com.symmetrylabs.util.DistanceConstants.*;

import heronarts.p3lx.ui.UI3dComponent;
import processing.core.PApplet;
import static processing.core.PConstants.*;
import processing.core.PGraphics;
import processing.core.PImage;

public class UITreeGround extends UI3dComponent {

    private final PImage dust;
    private final PImage person;
    private static final int DUST_FILL = 0xFF005b1e;

    public UITreeGround(PApplet applet) {
        this.dust = applet.loadImage("dust.png");
        this.person = applet.loadImage("person.png");
    }

    @Override
    protected void onDraw(heronarts.p3lx.ui.UI ui, PGraphics pg) {
        pg.tint(DUST_FILL);
        pg.textureMode(NORMAL);
        pg.beginShape();
        pg.texture(this.dust);
        pg.vertex(-50*FEET, 1, -50*FEET, 0, 0);
        pg.vertex(50*FEET, 1, -50*FEET, 0, 1);
        pg.vertex(50*FEET, 1, 50*FEET, 1, 1);
        pg.vertex(-50*FEET, 1, 50*FEET, 1, 0);
        pg.endShape(CLOSE);
    }
}

