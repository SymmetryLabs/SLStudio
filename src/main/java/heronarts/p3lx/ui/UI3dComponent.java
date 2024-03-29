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

/**
 * A component in a CameraLayer. Draws itself and may draw children.
 */
public abstract class UI3dComponent extends UIObject {

    @Override
    public boolean contains(float x, float y) {
        return false;
    }

    @Override
    public float getWidth() {
        return -1;
    }

    @Override
    public float getHeight() {
        return -1;
    }

    /**
     * Adds a child to this component
     *
     * @param child Child component
     * @return this
     */
    public final UI3dComponent addChild(UI3dComponent child) {
        this.mutableChildren.add(child);
        return this;
    }

    /**
     * Removes a child from this component
     *
     * @param child Child component
     * @return this
     */
    public final UI3dComponent removeChild(UI3dComponent child) {
        this.mutableChildren.remove(child);
        return this;
    }
}
