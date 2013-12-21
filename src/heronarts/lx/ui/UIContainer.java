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

/**
 * This is a UIObject that may contain other UIObjects. Mouse and drawing events
 * are automatically delegated appropriately. The onDraw method of the container
 * itself is invoked before its children, meaning that children are drawn on top
 * of underlying elements.
 */
public class UIContainer extends UIObject {

    /**
     * Which child mouse events are focused to, if any.
     */
    private UIObject focusedChild = null;

    /**
     * Constructs an empty UIContainer with no size.
     */
    public UIContainer() {}

    /**
     * Constructs an empty container with a size.
     * 
     * @param x x-position
     * @param y y-position
     * @param w width
     * @param h height
     */
    public UIContainer(float x, float y, float w, float h) {
        super(x, y, w, h);
    }

    public UIContainer(UIObject[] children) {
        for (UIObject child : children) {
            child.addToContainer(this);
        }
    }

    protected void onMousePressed(float mx, float my) {
        for (int i = this.children.size() - 1; i >= 0; --i) {
            UIObject child = this.children.get(i);
            if (child.contains(mx, my)) {
                child.onMousePressed(mx - child.x, my - child.y);
                this.focusedChild = child;
                break;
            }
        }
    }

    protected void onMouseReleased(float mx, float my) {
        if (this.focusedChild != null) {
            this.focusedChild.onMouseReleased(mx - this.focusedChild.x, my - this.focusedChild.y);
        }
        this.focusedChild = null;
    }

    protected void onMouseDragged(float mx, float my, float dx, float dy) {
        if (this.focusedChild != null) {
            this.focusedChild.onMouseDragged(mx - this.focusedChild.x, my - this.focusedChild.y, dx, dy);
        }
    }

    protected void onMouseWheel(float mx, float my, float delta) {
        for (int i = this.children.size() - 1; i >= 0; --i) {
            UIObject child = this.children.get(i);
            if (child.contains(mx, my)) {
                child.onMouseWheel(mx - child.x, mx - child.y, delta);
                break;
            }
        }
    }

}
