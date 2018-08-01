package com.symmetrylabs.util;

import com.symmetrylabs.slstudio.SLStudio;

import processing.core.PConstants;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PVector;

public class TextMarker implements Marker {
    public PVector pos;
    public float size;
    public int rgb;
    public String text;
    static private PFont font = null;

    public TextMarker(PVector pos, float size, int rgb, String text) {
        this.pos = pos.copy();
        this.size = size;
        this.rgb = rgb;
        this.text = text;
        if (font == null) {
            font = SLStudio.applet.loadFont("Inconsolata-Bold-14.vlw");
        }
    }

    @Override public void draw(PGraphics pg) {
        pg.textFont(font);
        pg.textAlign(PConstants.CENTER, PConstants.CENTER);
        pg.textSize(size);
        pg.fill((rgb >> 16) & 0xff, (rgb >> 8) & 0xff, rgb & 0xff);
        pg.pushMatrix();
        pg.translate(pos.x, pos.y, pos.z);
        pg.rotateX((float) Math.PI);  // otherwise text appears upside-down
        pg.text(text, 0, 0, 0);
        pg.popMatrix();
    }
}
