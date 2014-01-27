/**
 * ##library.name##
 * ##library.sentence##
 * ##library.url##
 *
 * Copyright ##copyright## ##author##
 * All Rights Reserved
 * 
 * @author      ##author##
 * @modified    ##date##
 * @version     ##library.prettyVersion## (##library.version##)
 */

package heronarts.lx.ui;

import java.util.ArrayList;
import java.util.List;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PGraphics;

/**
 * Top-level container for all overlay UI elements. 
 */
public class UI {
    
    /**
     * PApplet that this UI belongs to
     */
    final PApplet applet;
    
    /**
     * All the contexts in this UI
     */
    private final List<UIContext> contexts = new ArrayList<UIContext>();
    
    /**
     * Default item font in this UI
     */
    private PFont itemFont;
    
    /**
     * Default title font in this UI
     */
    private PFont titleFont;
    
    /**
     * Default text color
     */
    private int textColor = 0xff999999;

    /**
     * Default background color
     */
    private int backgroundColor = 0xff444444;
    
    /**
     * Default selected highlight color
     */
    private int highlightColor = 0xff669966;
    
    /**
     * Default active highlight color
     */
    private int activeColor = 0xff666699;
    
    /**
     * White color
     */
    public final int WHITE = 0xffffffff;
    
    /**
     * Black color
     */
    public final int BLACK = 0xff000000;
    
    /**
     * Creates a new UI instance
     * 
     * @param applet The PApplet
     */
    public UI(PApplet applet) {
        this.applet = applet;
        this.itemFont = applet.createFont("Lucida Grande", 11);
        this.titleFont = applet.createFont("Myriad Pro", 10);
    }
    
    /**
     * Add a context to this UI
     * 
     * @param context UI context
     * @return this UI
     */
    public UI addContext(UIContext context) {
        this.contexts.add(context);
        return this;
    }
    
    /**
     * Remove a context from thsi UI
     * 
     * @param context UI context
     * @return this UI
     */
    public UI removeContext(UIContext context) {
        this.contexts.remove(context);
        return this;
    }
    
    /**
     * Brings a context to the top of the UI stack
     * 
     * @param context UI context
     * @return this UI
     */
    public UI bringToTop(UIContext context) {
        removeContext(context);
        addContext(context);
        return this;
    }
    
    /**
     * Gets the default item font for this UI
     * 
     * @return The default item font for this UI
     */
    public PFont getItemFont() {
        return this.itemFont;
    }
    
    /**
     * Sets the default item font for this UI
     * 
     * @param font Font to use
     * @return this UI
     */
    public UI setItemFont(PFont font) {
        this.itemFont = font;
        return this;
    }
    
    /**
     * Gets the default title font for this UI
     * 
     * @return default title font for this UI
     */
    public PFont getTitleFont() {
        return this.titleFont;
    }
    
    /**
     * Sets the default title font for this UI
     * 
     * @param font Default title font
     * @return this UI
     */
    public UI setTitleFont(PFont font) {
        this.titleFont = font;
        return this;
    }
    
    /**
     * Gets the default text color
     * 
     * @return default text color
     */
    public int getTextColor() {
        return this.textColor;
    }
    
    /**
     * Sets the default text color for UI
     * 
     * @param color Color
     * @return this UI
     */
    public UI setTextColor(int color) {
        this.textColor = color;
        return this;
    }
    
    /**
     * Gets background color
     * 
     * @return backgroundc olor
     */
    public int getBackgroundColor() {
        return this.backgroundColor;
    }
    
    /**
     * Sets default background color
     * 
     * @param color color
     * @return this UI
     */
    public UI setBackgroundColor(int color) {
        this.backgroundColor = color;
        return this;
    }
    
    /**
     * Gets highlight color
     * 
     * @return Highlight color
     */
    public int getHighlightColor() {
        return this.highlightColor;
    }
    
    /**
     * Sets highlight color
     * 
     * @param color
     * @return this UI
     */
    public UI setHighlightColor(int color) {
        this.highlightColor = color;
        return this;
    }
    
    /**
     * Get active color
     * 
     * @return Active color
     */
    public int getActiveColor() {
        return this.activeColor;
    }
    
    /**
     * Set active color
     * 
     * @param color Color
     * @return this UI
     */
    public UI setActiveColor(int color) {
        this.activeColor = color;
        return this;
    }
    
    /**
     * Draws the UI
     */
    public final void draw() {
        for (UIContext context : this.contexts) {
            context.draw();
        }
    }
    
    public final void mousePressed(int x, int y) {
        for (UIContext context : this.contexts) {
            context.mousePressed(x, y);
        }
    }
    
    public final void mouseReleased(int x, int y) {
        for (UIContext context : this.contexts) {
            context.mouseReleased(x, y);
        }
    }
    
    public final void mouseDragged(int x, int y) {
        for (UIContext context : this.contexts) {
            context.mouseDragged(x, y);
        }
    }
    
    public final void mouseWheel(int x, int y, int rotation) {
        for (UIContext context : this.contexts) {
            context.mouseWheel(x, y, rotation);
        }
    }
}
