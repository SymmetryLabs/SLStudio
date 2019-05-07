package com.symmetrylabs.util;

import com.symmetrylabs.slstudio.SLStudio;
import org.lwjgl.system.CallbackI.Z;
import com.symmetrylabs.slstudio.ui.GraphicsAdapter;

import processing.core.PConstants;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PVector;

public class TextMarker implements Marker {
    public enum FlipDir {
        NONE, X, Y, Z,
    };

    public PVector pos;
    public float size;
    public int rgb;
    public String text;
    public FlipDir flip;
    static private PFont font = null;

    public TextMarker(PVector pos, float size, int rgb, String text) {
        this(pos, size, rgb, FlipDir.X, text);
    }

    public TextMarker(PVector pos, float size, int rgb, FlipDir flip, String text) {
        this.pos = pos.copy();
        this.size = size;
        this.rgb = rgb;
        this.text = text;
        this.flip = flip;
        if (font == null && SLStudio.applet != null) {
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
        /* we usually need to flip the text so it doesn't appear upside-down.
             depending on the orientation of the model, we might want to flip it
             a couple different ways. We let marker sources pick, under the
             assumption that they know better than we do. */
        switch (flip) {
        case X:
            pg.rotateX((float) Math.PI);
            break;
        case Y:
            pg.rotateY((float) Math.PI);
            break;
        case Z:
            pg.rotateZ((float) Math.PI);
            break;
        case NONE:
            break;
        }
        pg.text(text, 0, 0, 0);
        pg.popMatrix();
    }
}
