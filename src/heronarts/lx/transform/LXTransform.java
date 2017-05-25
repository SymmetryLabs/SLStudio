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
 * @author Mark C. Slee <mark@heronarts.com>
 */

package heronarts.lx.transform;

import java.util.Stack;

/**
 * A transform is a matrix stack, quite similar to the OpenGL implementation.
 * This class can be used to push a point around in 3-d space. The matrix itself
 * is not directly exposed, but the x,y,z values are.
 */
public class LXTransform {

    private final Stack<LXMatrix> matrices;

    /**
     * Constructs a new transform
     */
    public LXTransform() {
        this.matrices = new Stack<LXMatrix>();
        this.matrices.push(new LXMatrix());
    }

    public LXMatrix getMatrix() {
        return this.matrices.peek();
    }

    /**
     * Gets the current x, y, z of the transform as a vector
     *
     * @return Vector of current position
     */
    public LXVector vector() {
        LXMatrix m = getMatrix();
        return new LXVector(m.m14, m.m24, m.m34);
    }

    /**
     * Gets the x value of the transform
     *
     * @return x value of transform
     */
    public float x() {
        return getMatrix().m14;
    }

    /**
     * Gets the y value of the transform
     *
     * @return y value of transform
     */
    public float y() {
        return getMatrix().m24;
    }

    /**
     * Gets the z value of the transform
     *
     * @return z value of transform
     */
    public float z() {
        return getMatrix().m34;
    }

    /**
     * Translates the point, default of 0 in the z-axis
     *
     * @param tx x translation
     * @param ty y translation
     * @return this
     */
    public LXTransform translate(float tx, float ty) {
        return translate(tx, ty, 0);
    }

    /**
     * Translates the point
     *
     * @param tx x translation
     * @param ty y translation
     * @param tz z translation
     * @return this
     */
    public LXTransform translate(float tx, float ty, float tz) {
        getMatrix().translate(tx, ty, tz);
        return this;
    }

    /**
     * Rotates about the x axis
     *
     * @param rx Degrees, in radians
     * @return this, for method chaining
     */
    public LXTransform rotateX(float rx) {
        getMatrix().rotateX(rx);
        return this;
    }

    public LXTransform rotateX(double rx) {
        return rotateX((float) rx);
    }

    /**
     * Rotates about the y axis
     *
     * @param ry Degrees, in radians
     * @return this, for method chaining
     */
    public LXTransform rotateY(float ry) {
        getMatrix().rotateY(ry);
        return this;
    }

    public LXTransform rotateY(double ry) {
        return rotateY((float) ry);
    }

    /**
     * Rotates about the z axis
     *
     * @param rz Degrees, in radians
     * @return this, for method chaining
     */
    public LXTransform rotateZ(float rz) {
        getMatrix().rotateZ(rz);
        return this;
    }

    public LXTransform rotateZ(double rz) {
        return rotateZ((float) rz);
    }

    /**
     * Pushes the matrix stack, future operations can be undone by pop()
     *
     * @return this, for method chaining
     */
    public LXTransform push() {
        this.matrices.push(new LXMatrix(this.matrices.peek()));
        return this;
    }

    /**
     * Pops the matrix stack, to its previous state
     *
     * @return this, for method chaining
     */
    public LXTransform pop() {
        this.matrices.pop();
        return this;
    }

}
