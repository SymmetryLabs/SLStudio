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

import processing.core.PGraphics;

/**
 * Object in a UI hierarchy. UIObjects all have coordinate in an x-y space that uses
 * standard graphics coordinate system of (0,0) representing the top-left corner.
 * Increasing x-values move to the right, y-values move down. Object positions use
 * x, y to refer to the origin of the object at the top left corner, with width
 * and height forming a bounding rectangle.
 */
public abstract class UIObject {

    protected final static int DOUBLE_CLICK_THRESHOLD = 300;

    /**
     * Children of this object, latest elements are drawn on top.
     */
    protected final List<UIObject> children = new ArrayList<UIObject>();  
    
    /**
     * Internal state, true if this object needs to be redrawn.
     */
    protected boolean needsRedraw = true;
    
    /**
     * Internal state, true if a child of this object needs to be redrawn.
     */
    protected boolean childNeedsRedraw = true;

    /**
     * Position of the object, relative to parent, top left corner
     */
    protected float x;
    
    /**
     * Position of the object, relative to parent, top left corner
     */
    protected float y;
    
    /**
     * Width of the object
     */
    protected float width;
    
    /**
     * Height of the object
     */
    protected float height;
    
    /**
     * Parent object.
     */
    public UIContainer parent = null;

    /**
     * Whether this object is visible or not.
     */
    protected boolean visible = true;

    /**
     * Constructs a UIObject with no size.
     */
    protected UIObject() {
        this(0, 0, 0, 0);
    }

    /**
     * Constructs a UIObject
     * 
     * @param x x-position
     * @param y y-position
     * @param w width
     * @param h height
     */
    protected UIObject(float x, float y, float w, float h) {
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;
    }

    /**
     * Whether this object is visible.
     * 
     * @return True if this object is being displayed
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Set whether this object should be displayed
     * 
     * @param visible Whether to display this object
     * @return this object
     */
    public UIObject setVisible(boolean visible) {
        if (visible != this.visible) {
            this.visible = visible;
            redraw();
        }
        return this;
    }

    /**
     * Sets the position of this object, relative to its parent
     * 
     * @param x x-position, relative to parent's coordinate space
     * @param y y-position, relative to parent's coordinate space
     * @return this object
     */
    public final UIObject setPosition(float x, float y) {
        this.x = x;
        this.y = y;
        redraw();
        return this;
    }

    /**
     * Sets the size of this object.
     * 
     * @param width Width of object
     * @param height Height of object
     * @return this object
     */
    public final UIObject setSize(float width, float height) {
        this.width = width;
        this.height = height;
        redraw();
        return this;
    }
    
    /**
     * Width
     * 
     * @return width
     */
    public final float getWidth() {
        return this.width;
    }
    
    /**
     * Height 
     * 
     * @return height
     */
    public final float getHeight() {
        return this.height;
    }

    /**
     * Whether a given point, in the container's coordinate space, is within
     * this object.
     * 
     * @param x x-coordinate, in parent's coordinate space
     * @param y y-coordinate, in parent's coordinate space
     * @return true if the point is inside this object's bounds
     */
    public final boolean contains(float x, float y) {
        return
                (x >= this.x && x < (this.x + this.width)) &&
                (y >= this.y && y < (this.y + this.height));
    }
    
    /**
     * Places this object inside a container.
     * 
     * @param container The object in which to place this
     * @return this object
     */
    public final UIObject addToContainer(UIContainer container) {
        if (this.parent != null) {
            removeFromContainer();
        }
        container.children.add(this);
        this.parent = container;
        return this;
    }

    /**
     * Removes this object from a container that it is in.
     * 
     * @return this object
     */
    public final UIObject removeFromContainer() {
        if (this.parent != null) {
            this.parent.children.remove(this);
            this.parent = null;
        }
        return this;
    }

    /**
     * Redraws this object.
     * 
     * @return this object
     */
    public final UIObject redraw() {
        // Mark object and children as needing redraw
        _redraw();
        
        // Mark parent containers as needing a child redrawn
        UIObject p = this.parent;
        while (p != null) {
            p.childNeedsRedraw = true;
            p = p.parent;
        }
        return this;
    }

    /**
     * Internal helper. Marks this object and all of its children as needing to
     * be redrawn.
     */
    private final void _redraw() {
        this.needsRedraw = true;
        for (UIObject child : this.children) {
            this.childNeedsRedraw = true;
            child._redraw();
        }    
    }

    /**
     * Draws this object to the graphics context.
     * 
     * @param ui UI
     * @param pg graphics buffer
     */
    final void draw(UI ui, PGraphics pg) {
        if (!this.visible) {
            return;
        }
        if (this.needsRedraw) {
            this.needsRedraw = false;
            onDraw(ui, pg);
        }
        if (this.childNeedsRedraw) {
            this.childNeedsRedraw = false;
            for (UIObject child : children) {
                if (this.needsRedraw || child.needsRedraw || child.childNeedsRedraw) {
                    pg.pushMatrix();
                    pg.translate(child.x, child.y);
                    child.draw(ui, pg);
                    pg.popMatrix();
                }
            }
        }
    }

    /**
     * Invoked whenever this object needs to draw itself. Subclasses should override
     * to implement their drawing functionality.
     * 
     * @param ui UI
     * @param pg PGraphics context
     */
    protected void onDraw(UI ui, PGraphics pg) {}
    
    /**
     * Invoked when the mouse is pressed within the bounds of this object.
     * 
     * @param mx x-position in this object's coordinate space
     * @param my y-position in this object's coordinate space
     */
    protected void onMousePressed(float mx, float my) {}
    
    /**
     * Invoked when the mouse is released in this object, or after being initially
     * pressed inside this object.
     * 
     * @param mx x-position in this object's coordinate space
     * @param my y-position in this object's coordinate space
     */
    protected void onMouseReleased(float mx, float my) {}
    
    /**
     * Invoked when the mouse is dragged in this object, or after being initially
     * pressed inside this object.
     * 
     * @param mx x-position in this object's coordinate space
     * @param my y-position in this object's coordinate space
     * @param dx relative change in x-position since last invocation
     * @param dy relative change in y-position since last invocation
     */
    protected void onMouseDragged(float mx, float my, float dx, float dy) {}
        
    /**
     * Invoked when the mouse wheel is scrolled inside this object.
     *
     * @param mx x-position in this object's coordinate space
     * @param my y-position in this object's coordinate space
     * @param dx relative change in mouse wheel position
     */
    protected void onMouseWheel(float mx, float my, float dx) {}  
}
