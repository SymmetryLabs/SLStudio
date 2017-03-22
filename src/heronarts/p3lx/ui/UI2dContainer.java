/**
 * Copyright 2013- Mark C. Slee, Heron Arts LLC
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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

import java.util.Iterator;

public class UI2dContainer extends UI2dComponent implements UIContainer, Iterable<UIObject> {

    public enum Layout {
        NONE,
        VERTICAL,
        HORIZONTAL
    }

    private Layout layout = Layout.NONE;

    private int padding = 0;

    private int childMargin = 0;

    private UI2dContainer contentTarget;

    public UI2dContainer(float x, float y, float w, float h) {
        super(x, y, w, h);
        this.contentTarget = this;
    }

    public UI2dContainer setPadding(int padding) {
        if (this.padding != padding) {
            this.padding = padding;
            reflow();
        }
        return this;
    }

    public UI2dContainer setChildMargin(int childMargin) {
        if (this.contentTarget.childMargin != childMargin) {
            this.contentTarget.childMargin = childMargin;
            this.contentTarget.reflow();
        }
        return this;
    }

    public UI2dContainer setLayout(Layout layout) {
        if (this.contentTarget.layout != layout) {
            this.contentTarget.layout = layout;
            this.contentTarget.reflow();
        }
        return this;
    }

    protected void reflow() {
        if (this.layout == Layout.VERTICAL) {
            float y = this.padding;
            for (UIObject child : this) {
                UI2dComponent component = (UI2dComponent) child;
                component.setY(y);
                y += component.getHeight() + this.childMargin;
            }
            setContentHeight(y - this.childMargin);
        } else if (this.layout == Layout.HORIZONTAL) {
            float x = this.padding;
            for (UIObject child : this) {
                UI2dComponent component = (UI2dComponent) child;
                component.setX(x);
                x += component.getWidth() + this.childMargin;
            }
            setContentWidth(x - this.childMargin);
        }
    }

    protected UI2dContainer setContentTarget(UI2dContainer contentTarget) {
        this.contentTarget = contentTarget;
        this.children.add(contentTarget);
        contentTarget.parent = this;
        contentTarget.setUI(this.ui);
        redraw();
        return this;
    }

    protected UI2dContainer addTopLevelComponent(UI2dComponent child) {
        if (child.parent != null) {
            child.removeFromContainer();
        }
        this.children.add(child);
        child.parent = this;
        child.setUI(this.ui);
        redraw();
        return this;
    }

    /**
     * Returns the object that elements are added to when placed in this container.
     * In most cases, it will be "this" - but some elements have special subcontainers.
     *
     * @return Element
     */
    @Override
    public UIObject getContentTarget() {
        return this.contentTarget;
    }

    @Override
    public float getContentWidth() {
        return getContentTarget().getWidth();
    }

    @Override
    public float getContentHeight() {
        return getContentTarget().getHeight();
    }

    public UI2dContainer setContentWidth(float w) {
        return setContentSize(w, getContentHeight());
    }

    public UI2dContainer setContentHeight(float h) {
        return setContentSize(getContentWidth(), h);
    }

    public UI2dContainer setContentSize(float w, float h) {
        this.contentTarget.setSize(w, h);
        return this;
    }

    @Override
    public Iterator<UIObject> iterator() {
        return this.contentTarget.children.iterator();
    }
}
