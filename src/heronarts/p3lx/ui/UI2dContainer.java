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

public class UI2dContainer extends UI2dComponent implements UIContainer {

    private UI2dComponent contentTarget;

    public UI2dContainer(float x, float y, float w, float h) {
        super(x, y, w, h);
        this.contentTarget = this;
    }

    protected UI2dContainer setContentTarget(UI2dComponent contentTarget) {
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
}
