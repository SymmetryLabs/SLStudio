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