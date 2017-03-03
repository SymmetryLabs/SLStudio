/**
 * Copyright 2013- Mark C. Slee, Heron Arts LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ##library.name##
 * ##library.sentence##
 * ##library.url##
 *
 * @author      ##author##
 * @modified    ##date##
 * @version     ##library.prettyVersion## (##library.version##)
 */

package heronarts.p3lx.ui;

import heronarts.lx.LXLoopTask;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import processing.core.PGraphics;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

public abstract class UIObject extends UIEventHandler implements LXLoopTask {

    UI ui = null;

    public final BooleanParameter visible = new BooleanParameter("Visible", true);

    final List<UIObject> children = new CopyOnWriteArrayList<UIObject>();

    UIObject parent = null;

    UIObject focusedChild = null;

    private UIObject pressedChild = null;

    private boolean hasFocus = false;

    private final List<LXLoopTask> loopTasks = new ArrayList<LXLoopTask>();

    protected UIObject() {
        this.visible.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter parameter) {
                if (!UIObject.this.visible.isOn()) {
                    blur();
                }
            }
        });
    }

    /**
     * Add a task to be performed on every loop of the UI engine.
     *
     * @param loopTask
     * @return
     */
    protected UIObject addLoopTask(LXLoopTask loopTask) {
        this.loopTasks.add(loopTask);
        return this;
    }

    /**
     * Remove a task from the UI engine
     *
     * @param loopTask
     * @return
     */
    protected UIObject removeLoopTask(LXLoopTask loopTask) {
        this.loopTasks.remove(loopTask);
        return this;
    }

    /**
     * Processes all the loop tasks in this object
     */
    @Override
    public final void loop(double deltaMs) {
        if (isVisible()) {
            for (LXLoopTask loopTask : this.loopTasks) {
                loopTask.loop(deltaMs);
            }
            for (UIObject child : this.children) {
                child.loop(deltaMs);
            }
        }
    }

    /**
     * Internal method to track the UI that this is a part of
     *
     * @param ui UI context
     */
    void setUI(UI ui) {
        this.ui = ui;
        for (UIObject child : this.children) {
            child.setUI(ui);
        }
    }

    /**
     * Subclasses may access the object that is containing this one
     *
     * @return Parent object
     */
    protected UIObject getParent() {
        return this.parent;
    }

    /**
     * Whether the given point is contained by this object
     *
     * @param x x-coordinate
     * @param y y-coordinate
     * @return True if the object contains this point
     */
    protected boolean contains(float x, float y) {
        return true;
    }

    float getX() {
        return 0;
    }

    float getY() {
        return 0;
    }

    public float getWidth() {
        return (this.ui != null) ? this.ui.applet.width : 0;
    }

    public float getHeight() {
        return (this.ui != null) ? this.ui.applet.height : 0;
    }

    /**
     * Whether this object is visible.
     *
     * @return True if this object is being displayed
     */
    public boolean isVisible() {
        return this.visible.isOn();
    }

    /**
     * Toggle visible state of this component
     *
     * @return this
     */
    public UIObject toggleVisible() {
        this.visible.toggle();
        return this;
    }

    /**
     * Set whether this object is visible
     *
     * @param visible Whether the object is visible
     * @return this
     */
    public UIObject setVisible(boolean visible) {
        this.visible.setValue(visible);
        return this;
    }

    /**
     * Whether this object has focus
     *
     * @return true or false
     */
    public boolean hasFocus() {
        return this.hasFocus;
    }

    /**
     * Whether this object has direct focus, meaning that no
     * child element is focused
     *
     * @return true or false
     */
    public boolean hasDirectFocus() {
        return hasFocus() && (this.focusedChild == null);
    }

    /**
     * Gets which immediate child of this object is focused, may be null. Child
     * may also have focused children.
     *
     * @return immediate child of this object which has focus
     */
    public UIObject getFocusedChild() {
        return this.focusedChild;
    }

    /**
     * Focuses on this object, giving focus to everything above
     * and whatever was previously focused below.
     *
     * @return this
     */
    public UIObject focus() {
        if (this.focusedChild != null) {
            this.focusedChild.blur();
        }
        _focusParents();
        return this;
    }

    private void _focusParents() {
        if (this.parent != null) {
            if (this.parent.focusedChild != this) {
                if (this.parent.focusedChild != null) {
                    this.parent.focusedChild.blur();
                }
                this.parent.focusedChild = this;
            }
            this.parent._focusParents();
        }
        if (!this.hasFocus) {
            this.hasFocus = true;
            _onFocus();
        }
    }

    private void _onFocus() {
        onFocus();
        if (this instanceof UI2dComponent) {
            ((UI2dComponent) this).redraw();
        }
    }

    /**
     * Blur this object. Blurs its children from the bottom of
     * the tree up.
     *
     * @return this
     */
    public UIObject blur() {
        if (this.hasFocus) {
            for (UIObject child : this.children) {
                child.blur();
            }
            if (this.parent != null) {
                if (this.parent.focusedChild == this) {
                    this.parent.focusedChild = null;
                }
            }
            this.hasFocus = false;
            onBlur();
            if (this instanceof UI2dComponent) {
                ((UI2dComponent)this).redraw();
            }
        }
        return this;
    }

    /**
     * Brings this object to the front of its container.
     *
     * @return this
     */
    public UIObject bringToFront() {
        if (this.parent == null) {
            throw new IllegalStateException("Cannot bring to front when not in any container");
        }
        this.parent.children.remove(this);
        this.parent.children.add(this);
        return this;
    }

    void draw(UI ui, PGraphics pg) {
        if (isVisible()) {
            onDraw(ui, pg);
            for (UIObject child : this.children) {
                float cx = child.getX();
                float cy = child.getY();
                pg.translate(cx, cy);
                child.draw(ui, pg);
                pg.translate(-cx, -cy);
            }
        }
    }

    boolean isControlTarget() {
        return this.ui.getControlTarget() == this;
    }

    boolean isMapping() {
        return
            this.ui.midiMapping &&
            (this instanceof UIControlTarget) &&
            ((UIControlTarget) this).getControlTarget() != null;
    }

    void resize(UI ui) {
        this.onUIResize(ui);
        for (UIObject child : this.children) {
            child.resize(ui);
        }
    }

    /**
     * Subclasses may override this method to handle resize events on the global UI.
     * Called on the UI thread, only happens if ui.setResizable(true) has been called.
     *
     * @param ui The UI object
     */
    protected void onUIResize(UI ui) {}

    /**
     * Subclasses should override this method to perform their drawing functions.
     *
     * @param ui UI context
     * @param pg Graphics context
     */
    protected void onDraw(UI ui, PGraphics pg) {}

    /**
     * Called in a key event handler to stop this event from bubbling up the
     * parent container chain. For example, a button which responds to a space bar
     * press should call consumeKeyEvent() to stop the event from being handled by its
     * container.
     *
     * @return this
     */
    protected UIObject consumeKeyEvent() {
        this.keyEventConsumed = true;
        return this;
    }

    /**
     * Checks whether key event was already consumed
     *
     * @return
     */
    protected boolean keyEventConsumed() {
        return this.keyEventConsumed;
    }

    /**
     * Called in a mouse wheel handler to stop this mouse wheel event from
     * bubbling. Invoked by nested scroll views.
     *
     * @return
     */
    protected UIObject consumeMouseWheelEvent() {
        this.mouseWheelEventConsumed = true;
        return this;
    }

    private boolean keyEventConsumed = false;
    private boolean mouseWheelEventConsumed = false;

    void mousePressed(MouseEvent mouseEvent, float mx, float my) {
        if (isMapping()) {
            this.ui.setControlTarget((UIControlTarget) this);
            return;
        }
        for (int i = this.children.size() - 1; i >= 0; --i) {
            UIObject child = this.children.get(i);
            if (child.isVisible() && child.contains(mx, my)) {
                child.mousePressed(mouseEvent, mx - child.getX(), my - child.getY());
                this.pressedChild = child;
                break;
            }
        }
        if (!hasFocus() && (this instanceof UIMouseFocus)) {
            focus();
        }
        onMousePressed(mouseEvent, mx, my);
    }

    void mouseReleased(MouseEvent mouseEvent, float mx, float my) {
        if (this.pressedChild != null) {
            this.pressedChild.mouseReleased(
                mouseEvent,
                mx - this.pressedChild.getX(),
                my - this.pressedChild.getY()
            );
            this.pressedChild = null;
        }
        onMouseReleased(mouseEvent, mx, my);
    }

    void mouseClicked(MouseEvent mouseEvent, float mx, float my) {
        if (isMapping()) {
            return;
        }
        for (int i = this.children.size() - 1; i >= 0; --i) {
            UIObject child = this.children.get(i);
            if (child.isVisible() && child.contains(mx, my)) {
                child.mouseClicked(mouseEvent, mx - child.getX(), my - child.getY());
                break;
            }
        }
        onMouseClicked(mouseEvent, mx, my);
    }

    void mouseDragged(MouseEvent mouseEvent, float mx, float my, float dx, float dy) {
        if (isMapping()) {
            return;
        }
        if (this.pressedChild != null) {
            this.pressedChild.mouseDragged(
                mouseEvent,
                mx - this.pressedChild.getX(),
                my - this.pressedChild.getY(),
                dx,
                dy
            );
        }
        onMouseDragged(mouseEvent, mx, my, dx, dy);
    }

    void mouseMoved(MouseEvent mouseEvent, float mx, float my) {
        for (int i = this.children.size() - 1; i >= 0; --i) {
            UIObject child = this.children.get(i);
            if (child.isVisible() && child.contains(mx, my)) {
                child.mouseMoved(mouseEvent, mx - child.getX(), my - child.getY());
                break;
            }
        }
        onMouseMoved(mouseEvent, mx, my);
    }

    void mouseWheel(MouseEvent mouseEvent, float mx, float my, float delta) {
        this.mouseWheelEventConsumed = false;
        for (int i = this.children.size() - 1; i >= 0; --i) {
            UIObject child = this.children.get(i);
            if (child.isVisible() && child.contains(mx, my)) {
                child.mouseWheel(mouseEvent, mx - child.getX(), my - child.getY(), delta);
                this.mouseWheelEventConsumed = child.mouseWheelEventConsumed;
                break;
            }
        }
        if (!this.mouseWheelEventConsumed) {
            onMouseWheel(mouseEvent, mx, my, delta);
        }
    }

    void keyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
        this.keyEventConsumed = false;
        if (this.focusedChild != null) {
            UIObject delegate = this.focusedChild;
            delegate.keyPressed(keyEvent, keyChar, keyCode);
            this.keyEventConsumed = delegate.keyEventConsumed;
        }
        if (!this.keyEventConsumed) {
            onKeyPressed(keyEvent, keyChar, keyCode);
        }
    }

    void keyReleased(KeyEvent keyEvent, char keyChar, int keyCode) {
        this.keyEventConsumed = false;
        if (this.focusedChild != null) {
            UIObject delegate = this.focusedChild;
            delegate.keyReleased(keyEvent, keyChar, keyCode);
            this.keyEventConsumed = delegate.keyEventConsumed;
        }
        if (!this.keyEventConsumed) {
            onKeyReleased(keyEvent, keyChar, keyCode);
        }
    }

    void keyTyped(KeyEvent keyEvent, char keyChar, int keyCode) {
        this.keyEventConsumed = false;
        if (this.focusedChild != null) {
            UIObject delegate = this.focusedChild;
            delegate.keyTyped(keyEvent, keyChar, keyCode);
            this.keyEventConsumed = delegate.keyEventConsumed;
        }
        if (!this.keyEventConsumed) {
            onKeyTyped(keyEvent, keyChar, keyCode);
        }
    }

    /**
     * Subclasses override when element is focused
     */
    protected void onFocus() {
    }

    /**
     * Subclasses override when element loses focus
     */
    protected void onBlur() {
    }
}
