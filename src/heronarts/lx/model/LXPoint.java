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
     * x coordinate of this point
     */
    public final float x;

    /**
     * y coordinate of this point
     */
    public final float y;

    /**
     * z coordinate of this point
     */
    public final float z;

    /**
     * Radius of this point from origin in 3 dimensions
     */
    public final float r;

    /**
     * Radius of this point from origin in the x-y plane
     */
    public final float rxy;

    /**
     * Radius of this point from origin in the x-z plane
     */
    public final float rxz;

    /**
     * angle of this point about the origin in the x-y plane
     */
    public final float theta;

    /**
     * angle of this point about the origin in the x-z plane
     */
    public final float azimuth;

    /**
     * angle of this point about the origin in the x-z plane
     */
    public final float elevation;

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
        this.r = (float) Math.sqrt(x * x + y * y + z * z);
        this.rxy = (float) Math.sqrt(x * x + y * y);
        this.rxz = (float) Math.sqrt(x * x + z * z);
        this.theta = (float) ((LX.TWO_PI + Math.atan2(y, x)) % (LX.TWO_PI));
        this.azimuth = (float) ((LX.TWO_PI + Math.atan2(z, x)) % (LX.TWO_PI));
        this.elevation = (float) ((LX.TWO_PI + Math.atan2(y, rxz)) % (LX.TWO_PI));
        this.index = counter++;
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
     * Construct a point from transform
     *
     * @param transform LXTransform stack
     */
    public LXPoint(LXTransform transform) {
        this(transform.x(), transform.y(), transform.z());
    }

    void computeNormals(LXModel model) {
        this.xn = (this.x - model.xMin) / model.xRange;
        this.yn = (this.y - model.yMin) / model.yRange;
        this.zn = (this.z - model.zMin) / model.zRange;
        this.rn = this.r / model.rRange;
    }
}
