/**
 * Copyright 2017- Mark C. Slee, Heron Arts LLC
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

package heronarts.p3lx.ui.component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import heronarts.lx.LXUtils;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dScrollContext;
import heronarts.p3lx.ui.UIFocus;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

/**
 * An ItemList is a scrollable list of elements with a focus state
 * and action handling when the elements are focused or clicked on.
 */
public class UIItemList extends UI2dScrollContext implements UIFocus {

    private static final int PADDING = 2;
    private static final int SCROLL_BAR_WIDTH = 8;
    private static final int ROW_HEIGHT = 16;
    private static final int ROW_MARGIN = 2;
    private static final int ROW_SPACING = ROW_HEIGHT + ROW_MARGIN;
    private static final int CHECKBOX_SIZE = 8;

    /**
     * Interface to which items in the list must conform
     */
    public static interface Item {

        /**
         * Whether this item is in a special active state
         *
         * @return If this item is active
         */
        public boolean isActive();

        /**
         * Whether the item is checked, applies only if checkbox mode set on the list.
         *
         * @return If this item is checked
         */
        public boolean isChecked();

        /**
         * Active background color for this item
         *
         * @param ui UI context
         * @return Background color
         */
        public int getActiveColor(UI ui);

        /**
         * String label that displays on this item
         *
         * @return Label for the item
         */
        public String getLabel();

        /**
         * Action handler, invoked when item is activated
         */
        public void onActivate();

        /**
         * Action handler invoked when item is checked
         *
         * @param checked If checked
         */
        public void onCheck(boolean checked);

        /**
         * Action handler, invoked when item is deactivated. Only applies when setMomentary(true)
         */
        public void onDeactivate();

        /**
         * Action handler, invoked when an item is renamed. Only applies when setRenamable(true)
         */
        public void onRename(String name);

        /**
         * Action handler, invoked when item is deleted
         */
        public void onDelete();

        /**
         * Action handler, invoked when item is focused
         */
        public void onFocus();
    }

    /**
     * Helper class to make item construction easier
     */
    public static abstract class AbstractItem implements Item {

        public boolean isActive() {
            return false;
        }

        public boolean isChecked() {
            return false;
        }

        public int getActiveColor(UI ui) {
            return ui.theme.getControlBackgroundColor();
        }

        public void onActivate() {}

        public void onDeactivate() {}

        public void onCheck(boolean on) {}

        public void onDelete() {}

        public void onRename(String name) {
            throw new UnsupportedOperationException("Item does not implement renaming");
        }

        public void onFocus()  {}
    }

    private List<Item> items = new CopyOnWriteArrayList<Item>();

    private int focusIndex = -1;

    private boolean singleClickActivate = false;

    private boolean isMomentary = false;

    private boolean isRenamable = false;

    private boolean showCheckboxes = false;

    private boolean renaming = false;

    private String renameBuffer = "";

    /**
     * Constructs an item list
     *
     * @param ui UI
     * @param x x-position
     * @param y y-position
     * @param w width
     * @param h height
     */
    public UIItemList(UI ui, float x, float y, float w, float h) {
        super(ui, x, y, w, h);
        setBackgroundColor(0xff191919);
        setBorderRounding(4);
    }

    /**
     * Sets the index of the focused item in the list. Checks the bounds
     * and adjusts the scroll position if necessary.
     *
     * @param focusIndex Index of item to focus
     * @return this
     */
    public UIItemList setFocusIndex(int focusIndex) {
        return setFocusIndex(focusIndex, true);
    }

    private UIItemList setFocusIndex(int focusIndex, boolean scroll) {
        focusIndex = LXUtils.constrain(focusIndex, 0, this.items.size() - 1);
        if (this.focusIndex != focusIndex) {
            if (scroll) {
                float yp = ROW_SPACING * focusIndex + this.getScrollY();
                if (yp < 0) {
                    setScrollY(-ROW_SPACING * focusIndex);
                } else if (yp >= height - ROW_SPACING) {
                    setScrollY(-ROW_SPACING * focusIndex + height - ROW_SPACING);
                }
            }
            this.focusIndex = focusIndex;
            this.items.get(this.focusIndex).onFocus();
            redraw();
        }
        return this;
    }

    /**
     * Retrieves the currently focused item in the list.
     *
     * @return Focused item, or null if none is focused
     */
    public UIItemList.Item getFocusedItem() {
        if (this.focusIndex >= 0 && this.focusIndex < this.items.size()) {
            return this.items.get(this.focusIndex);
        }
        return null;
    }

    /**
     * Adds an item to the list
     *
     * @param item Item to remove
     * @return this
     */
    public UIItemList addItem(Item item) {
        this.items.add(item);
        setScrollHeight(ROW_SPACING * this.items.size() + ROW_MARGIN);
        redraw();
        return this;
    }

    /**
     * Removes an item from the list
     *
     * @param item Item to remove
     * @return this
     */
    public UIItemList removeItem(Item item) {
        this.items.remove(item);
        if (this.focusIndex >= this.items.size()) {
            setFocusIndex(items.size() - 1);
        }
        setScrollHeight(ROW_SPACING * this.items.size() + ROW_MARGIN);
        redraw();
        return this;
    }

    /**
     * Sets the items in the list and redraws it
     *
     * @param items Items
     * @return this
     */
    public UIItemList setItems(List<? extends Item> items) {
        this.items.clear();
        this.items.addAll(items);
        if (this.focusIndex >= items.size()) {
            setFocusIndex(items.size() - 1);
        } else if (this.focusIndex >= 0) {
            this.items.get(this.focusIndex).onFocus();
        }
        setScrollHeight(ROW_SPACING * items.size() + ROW_MARGIN);
        redraw();
        return this;
    }

    /**
     * Sets whether single-clicks on an item should activate them. Default behavior
     * requires double-click or ENTER keypress
     *
     * @param singleClickActivate Whether to activate on a single click
     * @return this
     */
    public UIItemList setSingleClickActivate(boolean singleClickActivate) {
        this.singleClickActivate = singleClickActivate;
        return this;
    }

    /**
     * Sets whether a column of checkboxes should be shown on the item list, to the
     * left of the labels. Useful for a secondary selection state.
     *
     * @param showCheckboxes
     * @return
     */
    public UIItemList setShowCheckboxes(boolean showCheckboxes) {
        if (this.showCheckboxes != showCheckboxes) {
            this.showCheckboxes = showCheckboxes;
            redraw();
        }
        return this;
    }

    /**
     * Sets whether renaming items is allowed
     *
     * @param isRenamable If items may be renamed
     * @return
     */
    public UIItemList setRenamable(boolean isRenamable) {
        this.isRenamable = isRenamable;
        return this;
    }

    /**
     * Sets whether the item list is momentary. If so, then clicking on an item
     * or pressing ENTER/SPACE sends a deactivate action after the click ends.
     *
     * @param momentary
     * @return this
     */
    public UIItemList setMomentary(boolean momentary) {
        this.isMomentary = momentary;
        return this;
    }

    private void activate() {
        if (this.focusIndex >= 0) {
            this.items.get(this.focusIndex).onActivate();
        }
    }

    private void delete() {
        if (this.focusIndex >= 0) {
            this.items.get(this.focusIndex).onDelete();
        }
    }

    private void check() {
        if (this.focusIndex >= 0) {
            Item item = this.items.get(this.focusIndex);
            item.onCheck(!item.isChecked());
            redraw();
        }
    }

    private float getRowWidth() {
        return (getScrollHeight() > this.height) ? this.width - SCROLL_BAR_WIDTH - PADDING : this.width;
    }

    @Override
    public void drawFocus(UI ui, PGraphics pg) {
        float yp = ROW_MARGIN + ROW_SPACING*this.focusIndex + this.getScrollY();
        super.drawFocus(ui, pg, ui.theme.getFocusColor(), PADDING, yp, getRowWidth()-2*PADDING, ROW_HEIGHT, 2);
    }

    @Override
    public void onDraw(UI ui, PGraphics pg) {
        float yp = ROW_MARGIN;
        pg.textFont(ui.theme.getControlFont());
        pg.textAlign(PConstants.LEFT, PConstants.TOP);
        pg.noStroke();
        int i = 0;

        float rowWidth = getRowWidth();
        if (getScrollHeight() > this.height) {
            pg.noStroke();
            pg.fill(0xff333333);
            float percentCovered = this.height / getScrollHeight();
            float barHeight = percentCovered * this.height - 2*PADDING;
            float startPosition = -getScrollY() / getScrollHeight();
            float barY = -getScrollY() + startPosition * this.height + PADDING;
            pg.rect(width-PADDING-SCROLL_BAR_WIDTH, barY, SCROLL_BAR_WIDTH, barHeight, 4);
        }

        for (Item item : this.items) {
            boolean renameItem = this.renaming && (this.focusIndex == i);

            int backgroundColor, textColor;
            if (item.isActive()) {
                backgroundColor = item.getActiveColor(ui);
                textColor = UI.WHITE;
            } else {
                backgroundColor = (i == this.focusIndex) ? 0xff333333 : ui.theme.getControlBackgroundColor();
                textColor = (i == this.focusIndex) ? UI.WHITE : ui.theme.getControlTextColor();
            }
            pg.noStroke();
            pg.fill(backgroundColor);
            pg.rect(PADDING, yp, rowWidth-2*PADDING, ROW_HEIGHT, 4);

            int textX = 6;
            if (this.showCheckboxes) {
             pg.stroke(textColor);
             pg.noFill();
             pg.rect(textX, yp+4, CHECKBOX_SIZE-1, CHECKBOX_SIZE-1);
             if (item.isChecked()) {
                 pg.noStroke();
                 pg.fill(textColor);
                 pg.rect(textX+2, yp+6, CHECKBOX_SIZE/2, CHECKBOX_SIZE/2);
             }
             textX += CHECKBOX_SIZE + 4;
            }
            if (renameItem) {
                pg.noStroke();
                pg.fill(UI.BLACK);
                pg.rect(textX-2, yp+1, rowWidth - PADDING - textX + 2, ROW_HEIGHT-2, 4);
                pg.fill(UI.WHITE);
                pg.text(clipTextToWidth(pg, this.renameBuffer, rowWidth - textX - 2), textX, yp + 4);
            } else {
                pg.fill(textColor);
                pg.text(clipTextToWidth(pg, item.getLabel(), rowWidth - textX - 2), textX, yp + 4);
            }
            yp += ROW_SPACING;
            ++i;
        }
        setScrollHeight(yp);
    }

    private int getMouseItemIndex(float my) {
        int index = (int) (my / (ROW_HEIGHT + ROW_MARGIN));
        return ((my % (ROW_HEIGHT + ROW_MARGIN)) >= ROW_MARGIN) ? index : -1;
    }

    @Override
    public void onMouseClicked(MouseEvent mouseEvent, float mx, float my) {
        if (!this.isMomentary && !this.singleClickActivate && (mouseEvent.getCount() == 2)) {
            int index = getMouseItemIndex(my);
            if (index >= 0) {
                setFocusIndex(index);
                activate();
            }
        }
    }

    private boolean dragging;

    @Override
    public void onMouseDragged(MouseEvent mouseEvent, float mx, float my, float dx, float dy) {
        if (this.dragging) {
            setScrollY(getScrollY() - dy*(getScrollHeight() / this.height));
        }
    }

    private int mouseActivate = -1;

    @Override
    public void onMousePressed(MouseEvent mouseEvent, float mx, float my) {
        this.mouseActivate = -1;
        if (getScrollHeight() > this.height && mx >= getRowWidth()) {
            this.dragging = true;
        } else {
            this.dragging = false;
            int index = getMouseItemIndex(my);
            if (index >= 0) {
                setFocusIndex(index);
                if (this.showCheckboxes && (mx < (5*PADDING + CHECKBOX_SIZE))) {
                    if (mx >= 2*PADDING) {
                        check();
                    }
                } else if (this.isMomentary || this.singleClickActivate) {
                    this.mouseActivate = this.focusIndex;
                    activate();
                }
            }
        }
    }

    @Override
    public void onMouseReleased(MouseEvent mouseEvent, float mx, float my) {
        this.dragging = false;
        if (this.mouseActivate >= 0 && this.mouseActivate < this.items.size()) {
            this.items.get(this.mouseActivate).onDeactivate();
        }
        this.mouseActivate = -1;
    }

    private int keyActivate = -1;

    @Override
    public void onBlur() {
        if (this.renaming) {
            this.renaming = false;
            redraw();
        }
    }

    @Override
    public void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
        if (this.renaming) {
            if (keyCode == java.awt.event.KeyEvent.VK_ESCAPE) {
                consumeKeyEvent();
                this.renaming = false;
                redraw();
            } else if (keyCode == java.awt.event.KeyEvent.VK_ENTER) {
                consumeKeyEvent();
                String newName = this.renameBuffer.trim();
                if (newName.length() > 0) {
                    this.items.get(this.focusIndex).onRename(newName);
                }
                this.renaming = false;
                redraw();
            } else if (keyCode == java.awt.event.KeyEvent.VK_BACK_SPACE) {
                consumeKeyEvent();
                if (this.renameBuffer.length() > 0) {
                    this.renameBuffer = this.renameBuffer.substring(0, this.renameBuffer.length() - 1);
                    redraw();
                }
            } else if (UITextBox.isValidTextCharacter(keyChar)) {
                consumeKeyEvent();
                this.renameBuffer += keyChar;
                redraw();
            }
        } else {
            if (keyCode == java.awt.event.KeyEvent.VK_UP) {
                consumeKeyEvent();
                setFocusIndex(this.focusIndex - 1);
                redraw();
            } else if (keyCode == java.awt.event.KeyEvent.VK_DOWN) {
                consumeKeyEvent();
                setFocusIndex(this.focusIndex + 1);
                redraw();
            } else if (keyCode == java.awt.event.KeyEvent.VK_ENTER) {
                consumeKeyEvent();
                if (this.isMomentary) {
                    this.keyActivate = this.focusIndex;
                }
                activate();
            } else if (keyCode == java.awt.event.KeyEvent.VK_SPACE) {
                consumeKeyEvent();
                if (this.showCheckboxes) {
                    check();
                } else {
                    if (this.isMomentary) {
                        this.keyActivate = this.focusIndex;
                    }
                    activate();
                }
            } else if (keyEvent.isControlDown() || keyEvent.isMetaDown()) {
                if (keyCode == java.awt.event.KeyEvent.VK_BACK_SPACE || keyCode == java.awt.event.KeyEvent.VK_D) {
                    consumeKeyEvent();
                    delete();
                } else if (keyCode == java.awt.event.KeyEvent.VK_R) {
                    if (this.isRenamable && this.focusIndex >= 0) {
                        consumeKeyEvent();
                        this.renaming = true;
                        this.renameBuffer = "";
                        redraw();
                    }
                }
            }
        }
    }

    @Override
    public void onKeyReleased(KeyEvent keyEvent, char keyChar, int keyCode) {
        if (keyCode == java.awt.event.KeyEvent.VK_ENTER || keyCode == java.awt.event.KeyEvent.VK_SPACE) {
            if (this.isMomentary) {
                if (this.keyActivate >= 0 && this.keyActivate < this.items.size()) {
                    consumeKeyEvent();
                    this.items.get(this.keyActivate).onDeactivate();
                }
                this.keyActivate = -1;
            }
        }
    }

    @Override
    public void onFocus() {
        if (this.focusIndex < 0 && this.items.size() > 0) {
            setFocusIndex(0, false);
        }
    }
}
