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

import processing.core.PGraphics;
import processing.event.MouseEvent;
import heronarts.lx.LXUtils;

public class UI2dScrollContext extends UI2dContext {

    private float scrollWidth;
    private float scrollHeight;

    private boolean horizontalScrollingEnabled = false;
    private boolean verticalScrollingEnabled = true;

    private static final int SCROLL_BAR_WIDTH = 6;
    private static final int SCROLL_BAR_PADDING = 2;

    private boolean scrollBarDragging = false;
    private float scrollBarDragStartY = 0;
    private float scrollBarDragStartScrollY = 0;

    public UI2dScrollContext(UI ui, float x, float y, float w, float h) {
        super(ui, x, y, w, h);
        this.scrollWidth = w;
        this.scrollHeight = h;
    }

    @Override
    public UI2dContainer setContentSize(float w, float h) {
        return setScrollSize(w, h);
    }

    public UI2dScrollContext setScrollSize(float scrollWidth, float scrollHeight) {
        if ((this.scrollWidth != scrollWidth) || (this.scrollHeight != scrollHeight)) {
            this.scrollWidth = scrollWidth;
            this.scrollHeight = scrollHeight;
            rescroll();
        }
        return this;
    }

    public float getScrollHeight() {
        return this.scrollHeight;
    }

    public UI2dScrollContext setScrollHeight(float scrollHeight) {
        if (this.scrollHeight != scrollHeight) {
            this.scrollHeight = scrollHeight;
            rescroll();
        }
        return this;
    }

    public float getScrollWidth() {
        return this.scrollWidth;
    }

    public UI2dScrollContext setScrollWidth(float scrollWidth) {
        if (this.scrollWidth != scrollWidth) {
            this.scrollWidth = scrollWidth;
            rescroll();
        }
        return this;
    }

    public UI2dScrollContext setHorizontalScrollingEnabled(boolean horizontalScrollingEnabled) {
        this.horizontalScrollingEnabled = horizontalScrollingEnabled;
        return this;
    }

    public UI2dScrollContext setVerticalScrollingEnabled(boolean verticalScrollingEnabled) {
        this.verticalScrollingEnabled = verticalScrollingEnabled;
        return this;
    }

    @Override
    protected void onResize() {
        super.onResize();
        rescroll();
    }

    private float minScrollX() {
        return Math.min(0, this.width - this.scrollWidth);
    }

    private float minScrollY() {
        return Math.min(0, this.height - this.scrollHeight);
    }

    public float getScrollX() {
        return this.scrollX;
    }

    public float getScrollY() {
        return this.scrollY;
    }

    public UI2dScrollContext setScrollX(float scrollX) {
        scrollX = LXUtils.constrainf(scrollX, minScrollX(), 0);
        if (this.scrollX != scrollX) {
            this.scrollX = scrollX;
            redraw();
        }
        return this;
    }

    public UI2dScrollContext setScrollY(float scrollY) {
        scrollY = LXUtils.constrainf(scrollY, minScrollY(), 0);
        if (this.scrollY != scrollY) {
            this.scrollY = scrollY;
            redraw();
        }
        return this;
    }

    private void rescroll() {
        float minScrollX = minScrollX();
        float minScrollY = minScrollY();
        if ((this.scrollX < minScrollX) || (this.scrollY < minScrollY)) {
            this.scrollX = Math.max(this.scrollX, minScrollX);
            this.scrollY = Math.max(this.scrollY, minScrollY);
            redraw();
        }
    }

    private boolean isScrollBarVisible() {
        return this.verticalScrollingEnabled && this.scrollHeight > this.height;
    }

    private float scrollBarThumbY() {
        float ratio = -this.scrollY / (this.scrollHeight - this.height);
        float trackH = this.height - 2 * SCROLL_BAR_PADDING;
        float thumbH = Math.max(20, (this.height / this.scrollHeight) * trackH);
        return SCROLL_BAR_PADDING + ratio * (trackH - thumbH);
    }

    private float scrollBarThumbHeight() {
        float trackH = this.height - 2 * SCROLL_BAR_PADDING;
        return Math.max(20, (this.height / this.scrollHeight) * trackH);
    }

    @Override
    void draw(UI ui, PGraphics pg) {
        if (!isVisible()) {
            return;
        }
        boolean needsUpdate = this.needsRedraw || this.childNeedsRedraw;
        super.draw(ui, pg);
        if (needsUpdate && isScrollBarVisible()) {
            PGraphics internal = getGraphics();
            internal.beginDraw();
            internal.noStroke();
            internal.fill(0x44ffffff);
            float thumbY = scrollBarThumbY();
            float thumbH = scrollBarThumbHeight();
            internal.rect(this.width - SCROLL_BAR_PADDING - SCROLL_BAR_WIDTH, thumbY, SCROLL_BAR_WIDTH, thumbH, 3);
            internal.endDraw();
            pg.image(internal, 0, 0);
        }
    }

    @Override
    void mousePressed(MouseEvent mouseEvent, float mx, float my) {
        if (isScrollBarVisible() && mx >= this.width - SCROLL_BAR_PADDING - SCROLL_BAR_WIDTH - 2) {
            float thumbY = scrollBarThumbY();
            float thumbH = scrollBarThumbHeight();
            if (my >= thumbY && my <= thumbY + thumbH) {
                this.scrollBarDragging = true;
                this.scrollBarDragStartY = my;
                this.scrollBarDragStartScrollY = this.scrollY;
                return;
            }
        }
        super.mousePressed(mouseEvent, mx - this.scrollX, my - this.scrollY);
    }

    @Override
    void mouseReleased(MouseEvent mouseEvent, float mx, float my) {
        if (this.scrollBarDragging) {
            this.scrollBarDragging = false;
            return;
        }
        super.mouseReleased(mouseEvent, mx - this.scrollX, my - this.scrollY);
    }

    @Override
    void mouseClicked(MouseEvent mouseEvent, float mx, float my) {
        if (this.scrollBarDragging) {
            return;
        }
        super.mouseClicked(mouseEvent, mx - this.scrollX, my - this.scrollY);
    }

    @Override
    void mouseDragged(MouseEvent mouseEvent, float mx, float my, float dx, float dy) {
        if (this.scrollBarDragging) {
            float trackH = this.height - 2 * SCROLL_BAR_PADDING;
            float thumbH = scrollBarThumbHeight();
            float scrollRange = this.scrollHeight - this.height;
            float dragRatio = scrollRange / (trackH - thumbH);
            setScrollY(this.scrollBarDragStartScrollY - (my - this.scrollBarDragStartY) * dragRatio);
            return;
        }
        super.mouseDragged(mouseEvent, mx - this.scrollX, my - this.scrollY, dx, dy);
    }

    @Override
    void mouseMoved(MouseEvent mouseEvent, float mx, float my) {
        super.mouseMoved(mouseEvent, mx - this.scrollX, my - this.scrollY);
    }

    @Override
    void mouseWheel(MouseEvent mouseEvent, float mx, float my, float delta) {
        super.mouseWheel(mouseEvent, mx - this.scrollX, my - this.scrollY, delta);
    }

    @Override
    public void onMouseWheel(MouseEvent e, float mx, float my, float delta) {
        if (e.isShiftDown()) {
            if (this.horizontalScrollingEnabled) {
                if (this.scrollWidth > this.width) {
                    consumeMouseWheelEvent();
                }
                setScrollX(this.scrollX - delta);
            }
        } else {
            if (this.verticalScrollingEnabled) {
                if (this.scrollHeight > this.height) {
                    consumeMouseWheelEvent();
                }
                setScrollY(this.scrollY - delta);
            }
        }
    }
}
