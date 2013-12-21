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
    
    final PApplet applet;
    
    private final List<UIContext> contexts = new ArrayList<UIContext>();
    
    private PFont itemFont;
    
    private PFont titleFont;
    
    public UI(PApplet applet) {
        this.applet = applet;
        this.itemFont = applet.createFont("Lucida Grande", 11);
        this.titleFont = applet.createFont("Myriad Pro", 10);
    }
    
    public UI addContext(UIContext context) {
        this.contexts.add(context);
        return this;
    }
    
    public UI removeContext(UIContext context) {
        this.contexts.remove(context);
        return this;
    }
    
    public UI bringToTop(UIContext context) {
        removeContext(context);
        addContext(context);
        return this;
    }
    
    public PFont getItemFont() {
        return this.itemFont;
    }
    
    public UI setItemFont(PFont font) {
        this.itemFont = font;
        return this;
    }
    
    public PFont getTitleFont() {
        return this.titleFont;
    }
    
    public UI setTitleFont(PFont font) {
        this.titleFont = font;
        return this;
    }
    
    public void draw() {
        for (UIContext context : this.contexts) {
            context.draw(this);
        }
    }
    
    public void mousePressed(float mx, float my) {
        
    }
    
    public void mouseReleased(float mx, float my) {
        
    }
    
    public void mouseDragged(float mx, float my) {
        
    }
    
    

}
