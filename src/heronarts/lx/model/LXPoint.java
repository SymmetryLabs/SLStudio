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
    public final float phi;

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
        this.theta = (float) ((Math.PI * 2 + Math.atan2(y, x)) % (Math.PI * 2));
        this.phi = (float) ((Math.PI * 2 + Math.atan2(z, x)) % (Math.PI * 2));
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

}
