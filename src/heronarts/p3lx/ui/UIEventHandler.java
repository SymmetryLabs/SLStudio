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

import processing.event.KeyEvent;
import processing.event.MouseEvent;

public abstract class UIEventHandler {
    /**
     * Subclasses override to receive mouse events
     *
     * @param mouseEvent Mouse event
     * @param mx x-coordinate
     * @param my y-coordinate
     */
    protected void onMousePressed(MouseEvent mouseEvent, float mx, float my) {
    }

    /**
     * Subclasses override to receive mouse events
     *
     * @param mouseEvent Mouse event
     * @param mx x-coordinate
     * @param my y-coordinate
     */
    protected void onMouseReleased(MouseEvent mouseEvent, float mx, float my) {
    }

    /**
     * Subclasses override to receive mouse events
     *
     * @param mouseEvent Mouse event
     * @param mx x-coordinate
     * @param my y-coordinate
     */
    protected void onMouseClicked(MouseEvent mouseEvent, float mx, float my) {
    }

    /**
     * Subclasses override to receive mouse events
     *
     * @param mouseEvent Mouse event
     * @param mx x-coordinate
     * @param my y-coordinate
     * @param dx movement in x
     * @param dy movement in y
     */
    protected void onMouseDragged(MouseEvent mouseEvent, float mx, float my, float dx, float dy) {
    }

    /**
     * Subclasses override to receive mouse events
     *
     * @param mouseEvent Mouse event
     * @param mx x-coordinate
     * @param my y-coordinate
     */
    protected void onMouseMoved(MouseEvent mouseEvent, float mx, float my) {
    }

    /**
     * Subclasses override to receive mouse events
     *
     * @param mouseEvent Mouse event
     * @param mx x-coordinate
     * @param my y-coordinate
     * @param delta Amount of wheel movement
     */
    protected void onMouseWheel(MouseEvent mouseEvent, float mx, float my, float delta) {
    }

    /**
     * Subclasses override to receive mouse events
     *
     * @param mouseEvent Mouse event
     * @param mx x-coordinate
     * @param my y-coordinate
     */
    protected void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
    }

    /**
     * Subclasses override to receive key events
     *
     * @param keyEvent Key event
     * @param keyChar Key character
     * @param keyCode Key code value
     */
    protected void onKeyReleased(KeyEvent keyEvent, char keyChar, int keyCode) {
    }

    /**
     * Subclasses override to receive key events
     *
     * @param keyEvent Key event
     * @param keyChar Key character
     * @param keyCode Key code value
     */
    protected void onKeyTyped(KeyEvent keyEvent, char keyChar, int keyCode) {
    }

}
