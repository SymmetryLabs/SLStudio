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

package heronarts.lx.model;

import heronarts.lx.LX;
import heronarts.lx.transform.LXTransform;

/**
 * A point is a node with an immutable position in space and a location in
 */
public class LXPoint {

    static int counter = 0;

    /**
     * Reset ID counter, to be called before a new LX instance is created.
     *
     * LXPoint maintains a static counter to assign IDs to new LXPoint objects. This counter needs
     * to be reset before you create a new LXModel instance for a new instance of LX. It is a bad
     * idea to reset this counter before creating a new model for an existing LX instance; if you
     * reset this, any new models have to be for a new LX.
     */
    public static void resetIdCounter() {
        counter = 0;
    }

    /**
     * x coordinate of this point
     */
    public float x;

    /**
     * y coordinate of this point
     */
    public float y;

    /**
     * z coordinate of this point
     */
    public float z;

    /**
     * Radius of this point from origin in 3 dimensions
     */
    public float r;

    /**
     * Radius of this point from origin in the x-y plane
     */
    public float rxy;

    /**
     * Radius of this point from origin in the x-z plane
     */
    public float rxz;

    /**
     * angle of this point about the origin in the x-y plane
     */
    public float theta;

    /**
     * Angle of this point about the origin in the x-z plane
     * (right-handed angle of rotation about the Y-axis)
     */
    public float azimuth;

    /**
     * Angle of this point between the y-value and the x-z plane
     */
    public float elevation;

    /**
     * normalized position of point in x-space (0-1);
     */
    public float xn = 0;

    /**
     * normalized position of point in y-space (0-1);
     */
    public float yn = 0;

    /**
     * normalized position of point in z-space (0-1);
     */
    public float zn = 0;

    /**
     * normalized position of point in radial space (0-1), 0 is origin, 1 is max radius
     */
    public float rn = 0;

    /**
     * Index of this point in the colors array
     */
    public final int index;

    /**
     * Construct a point in 2-d space, z-val is 0
     *
     * @param x X-coordinate
     * @param y Y-coordinate
     */
    public LXPoint(float x, float y) {
        this(x, y, 0);
    }

    /**
     * Construct a point in 3-d space
     *
     * @param x X-coordinate
     * @param y Y-coordinate
     * @param z Z-coordinate
     */
    public LXPoint(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.index = counter++;
        update();
    }

    /**
     * Construct a point in 3-d space
     *
     * @param x X-coordinate
     * @param y Y-coordinate
     * @param z Z-coordinate
     */
    public LXPoint(double x, double y, double z) {
        this((float) x, (float) y, (float) z);
    }

    /**
     * Updates this point to a new x-y-z position
     *
     * @param x X-position
     * @param y Y-position
     * @param z Z-position
     * @return this
     */
    public LXPoint update(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return update();
    }

    public LXPoint updateX(float x) {
        this.x = x;
        return update();
    }

    public LXPoint updateY(float y) {
        this.y = y;
        return update();
    }

    public LXPoint updateZ(float z) {
        this.z = z;
        return update();
    }

    /**
     * Updates the point's meta-coordinates, based upon the x y z values.
     *
     * @return
     */
    public LXPoint update() {
        this.r = (float) Math.sqrt(x * x + y * y + z * z);
        this.rxy = (float) Math.sqrt(x * x + y * y);
        this.rxz = (float) Math.sqrt(x * x + z * z);
        this.theta = (float) ((LX.TWO_PI + Math.atan2(y, x)) % (LX.TWO_PI));
        this.azimuth = (float) ((LX.TWO_PI + Math.atan2(z, x)) % (LX.TWO_PI));
        this.elevation = (float) ((LX.TWO_PI + Math.atan2(y, rxz)) % (LX.TWO_PI));
        return this;
    }

    /**
     * Construct a point from transform
     *
     * @param transform LXTransform stack
     */
    public LXPoint(LXTransform transform) {
        this(transform.x(), transform.y(), transform.z());
    }

    void normalize(LXModel model) {
        this.xn = (this.x - model.xMin) / model.xRange;
        this.yn = (this.y - model.yMin) / model.yRange;
        this.zn = (this.z - model.zMin) / model.zRange;
        this.rn = this.r / model.rRange;
    }
}
