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

import processing.event.MouseEvent;
import heronarts.lx.LXUtils;

public class UI2dScrollContext extends UI2dContext {

    private float scrollWidth;
    private float scrollHeight;

    private boolean horizontalScrollingEnabled = false;
    private boolean verticalScrollingEnabled = true;

    public UI2dScrollContext(UI ui, float x, float y, float w, float h) {
        super(ui, x, y, w, h);
        this.scrollWidth = w;
        this.scrollHeight = h;
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

    @Override
    void mousePressed(MouseEvent mouseEvent, float mx, float my) {
        super.mousePressed(mouseEvent, mx - this.scrollX, my - this.scrollY);
    }

    @Override
    void mouseReleased(MouseEvent mouseEvent, float mx, float my) {
        super.mouseReleased(mouseEvent, mx - this.scrollX, my - this.scrollY);
    }

    @Override
    void mouseClicked(MouseEvent mouseEvent, float mx, float my) {
        super.mouseClicked(mouseEvent, mx - this.scrollX, my - this.scrollY);
    }

    @Override
    void mouseDragged(MouseEvent mouseEvent, float mx, float my, float dx, float dy) {
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
    protected void onMouseWheel(MouseEvent e, float mx, float my, float delta) {
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
