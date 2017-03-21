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

/**
 * Container class for collapsible sections with labels which automatically reflow as
 * they collapse and expand.
 */
public class UI2dCollapsibleContainer extends UI2dContainer {

    private static final int DEFAULT_SECTION_MARGIN = 4;

    private int sectionMargin = DEFAULT_SECTION_MARGIN;

    public UI2dCollapsibleContainer(UI ui, float x, float y, float w, float h) {
        super(x, y, w, h);
    }

    /**
     * Set the margin between sections. Does not redraw, should be called
     * before any children are added.
     *
     * @param sectionMargin Margin between collapsible sections
     * @return this
     */
    public UI2dCollapsibleContainer setSectionMargin(int sectionMargin) {
        if (this.sectionMargin != sectionMargin) {
            this.sectionMargin = sectionMargin;
            onSectionResize();
        }
        return this;
    }

    /**
     * Collapses all children
     *
     * @return this
     */
    public UI2dCollapsibleContainer collapseAll() {
        for (UIObject child : this.children) {
            ((UI2dCollapsibleSection) child).setExpanded(false);
        }
        return this;
    }

    /**
     * Expands all children
     *
     * @return this
     */
    public UI2dCollapsibleContainer expandAll() {
        for (UIObject child : this.children) {
            ((UI2dCollapsibleSection) child).setExpanded(true);
        }
        return this;
    }

    void onSectionResize() {
        float yp = 0;
        for (UIObject childObject : this.children) {
            UI2dComponent child = (UI2dComponent) childObject;
            child.setY(yp);
            yp += child.getHeight() + this.sectionMargin;
        }
        setHeight(yp - this.sectionMargin);
    }

}