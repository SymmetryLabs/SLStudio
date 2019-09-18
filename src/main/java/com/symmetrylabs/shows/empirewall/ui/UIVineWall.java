package com.symmetrylabs.shows.empirewall.ui;

import static com.symmetrylabs.util.DistanceConstants.*;

import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI3dComponent;
import static processing.core.PConstants.*;
import processing.core.PGraphics;
import processing.core.PImage;
import com.symmetrylabs.slstudio.SLStudio;

public class UIVineWall extends UI3dComponent {

    private static final int WOOD_FILL = 0xFF281403;

    protected final PImage texImage;

    public UIVineWall() {
        this.texImage = SLStudio.applet.loadImage("patina.jpg");
    }

    protected void onDraw(UI ui, PGraphics pg) {
        pg.pushMatrix();
        pg.noFill();
        //pg.textureMode(NORMAL);
    	pg.fill(0xff222222);
    	pg.stroke(1);
    	pg.translate(1.5f*FEET, 6*FEET, 1);

    	for (int i = 0; i < 6; i++) {
    		pg.box(3*FEET, 12*FEET, 0);
    		pg.translate(3*FEET, 0, 0);
    	}
    	
    	// back
    	pg.translate(-10.5f*FEET, 0, 4.5f);
    	pg.box(17*FEET, 11*FEET, 8);
        pg.popMatrix();
    }
}
