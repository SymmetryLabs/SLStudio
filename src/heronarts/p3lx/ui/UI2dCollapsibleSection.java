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

package heronarts.p3lx.ui;

import heronarts.p3lx.ui.component.UILabel;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.event.MouseEvent;

/**
 * Section with a title which can collapse/expand
 */
public class UI2dCollapsibleSection extends UI2dContainer implements UIMouseFocus {

    private static final int PADDING = 4;
    private static final int TITLE_LABEL_HEIGHT = 12;
    private static final int CHEVRON_PADDING = 14;
    private static final int CONTENT_Y = TITLE_LABEL_HEIGHT + 6;
    private static final int CLOSED_HEIGHT = TITLE_LABEL_HEIGHT + PADDING;

    private final UILabel title;
    private boolean expanded = true;
    private float expandedHeight;

    private final UI2dContainer content;

    /**
     * Constructs a new container for a collapsible section
     *
     * @param ui UI
     * @param x Xpos
     * @param y Ypos
     * @param w Width
     * @param h Height
     */
    public UI2dCollapsibleSection(UI ui, float x, float y, float w, float h) {
        super(x, y, w, h);
        setBackgroundColor(ui.theme.getWindowBackgroundColor());
        setBorderRounding(4);

        this.title = new UILabel(PADDING, PADDING, this.width - PADDING - CHEVRON_PADDING, TITLE_LABEL_HEIGHT);
        this.title.setTextAlignment(PConstants.LEFT, PConstants.TOP).setTextOffset(0,  1);
        addTopLevelComponent(this.title);

        this.expandedHeight = h;
        this.content = new UI2dContainer(PADDING, CONTENT_Y, this.width - 2*PADDING, h - CONTENT_Y - PADDING) {
            @Override
            public void onResize() {
                UI2dCollapsibleSection.this.setHeight(expandedHeight = (this.height == 0 ? CLOSED_HEIGHT : CONTENT_Y + this.height + PADDING));
            }
        };
        this.content.setVisible(this.expanded);
        setContentTarget(this.content);
    }

    /**
     * Sets the title of the section
     *
     * @param title Title
     * @return this
     */
    public UI2dCollapsibleSection setTitle(String title) {
        this.title.setLabel(title);
        return this;
    }

    @Override
    public UI2dComponent addToContainer(UIContainer container, int index) {
        if (!(container.getContentTarget() instanceof UI2dCollapsibleContainer)) {
            throw new UnsupportedOperationException("Can only add UI2dCollapsibleSection to UI2dCollapsibleContainer");
        }
        super.addToContainer(container, index);
        onResize();
        return this;
    }

    @Override
    public void onResize() {
        if (this.parent != null) {
            ((UI2dCollapsibleContainer) ((UI2dContainer) this.parent).getContentTarget()).onSectionResize();
        }
    }

    @Override
    public void onDraw(UI ui, PGraphics pg) {
        pg.noStroke();
        pg.fill(ui.theme.getControlTextColor());
        if (this.expanded) {
            pg.beginShape();
            pg.vertex(this.width-6, 7);
            pg.vertex(this.width-12, 7);
            pg.vertex(this.width-9, 11);
            pg.endShape(PConstants.CLOSE);
        } else {
            pg.ellipseMode(PConstants.CENTER);
            pg.ellipse(width-9, 8, 4, 4);
        }
    }

    /**
     * Toggles the expansion state of the section
     *
     * @return this
     */
    public UI2dCollapsibleSection toggle() {
        return setExpanded(!this.expanded);
    }

    /**
     * Sets the expanded state of this section
     * @param expanded
     * @return this
     */
    public UI2dCollapsibleSection setExpanded(boolean expanded) {
        if (this.expanded != expanded) {
            this.expanded = expanded;
            this.content.setVisible(this.expanded);
            setHeight(this.expanded ? this.expandedHeight : CLOSED_HEIGHT);
        }
        return this;
    }

    @Override
    public void onMousePressed(MouseEvent mouseEvent, float mx, float my) {
        if (my < TITLE_LABEL_HEIGHT + PADDING) {
            toggle();
        }
    }


    @Override
    public UI2dContainer getContentTarget() {
        return this.content;
    }
}