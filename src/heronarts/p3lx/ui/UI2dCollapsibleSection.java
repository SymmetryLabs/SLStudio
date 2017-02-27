/**
 * Copyright 2017- Mark C. Slee, Heron Arts LLC
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

import heronarts.p3lx.ui.component.UILabel;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.event.MouseEvent;

/**
 * Section with a title which can collapse/expand
 */
public class UI2dCollapsibleSection extends UI2dContainer {

    private static final int PADDING = 4;
    private static final int TITLE_LABEL_HEIGHT = 12;
    private static final int CHEVRON_PADDING = 14;
    private static final int CONTENT_Y = TITLE_LABEL_HEIGHT + 6;
    private static final int CLOSED_HEIGHT = TITLE_LABEL_HEIGHT + PADDING;

    private final UILabel title;
    private boolean expanded = true;
    private final float expandedHeight;

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
        this.content = new UI2dContainer(PADDING, CONTENT_Y, this.width - 2*PADDING, h - CONTENT_Y - PADDING);
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
        if (!(container instanceof UI2dCollapsibleContainer)) {
            throw new UnsupportedOperationException("Can only add UI2dCollapsibleSection to UI2dCollapsibleContainer");
        }
        super.addToContainer(container, index);
        onResize();
        return this;
    }

    @Override
    public void onResize() {
        if (this.parent != null) {
            ((UI2dCollapsibleContainer) this.parent).onSectionResize();
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