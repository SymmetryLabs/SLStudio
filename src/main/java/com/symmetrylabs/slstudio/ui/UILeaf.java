package com.symmetrylabs.slstudio.ui;

import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI3dComponent;
import heronarts.lx.color.LXColor;
import processing.core.PGraphics;

import processing.core.PConstants;
import processing.core.PImage;
import processing.core.PApplet;

public class UILeaf extends UI3dComponent {
    private PImage leaf = null;

    float x = 0;
    float y = 0;

    public UILeaf(PApplet applet) {
        setVisible(true);
        this.leaf = applet.loadImage("leaf.png");

    }

    protected void onDraw(UI ui, PGraphics pg) {
        int leftColor = LXColor.BLUE;
        int rightColor = LXColor.RED;
     
        pg.textureMode(PConstants.NORMAL);
        pg.beginShape(PConstants.POLYGON);
        //pg.texture(leaf);
         pg.fill(leftColor);
             pg.vertex(x+=0.1, 0, 0, 0);
         pg.fill(rightColor);
             pg.vertex(100, y+=0.1, 0, 0);
         pg.fill(rightColor);
             pg.vertex(100, 100, 0, 100);
         pg.fill(leftColor);
             pg.vertex(0, 100, 0, 100);
        pg.endShape(PConstants.CLOSE);
    }
}
