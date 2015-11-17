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
     * radius of this point from origin in the x-y plane
     */
    public final float r;

    /**
     * angle of this point about the origin in the x-y plane
     */
    public final float theta;

    /**
     * angle of this point about the origin in the x-z plane
     */
    public final float ztheta;

    /**
     * Index of this point in the colors array
     */
    public final int index;

    /**
     * Construct a point in 2-d space, z-val is 0
     *
     * @param x
     * @param y
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
        this.r = (float) Math.sqrt(x * x + y * y);
        this.theta = (float) ((Math.PI * 2 + Math.atan2(y, x)) % (Math.PI * 2));
        this.ztheta = (float) ((Math.PI * 2 + Math.atan2(z, x)) % (Math.PI * 2));
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
