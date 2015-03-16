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

package heronarts.lx.transform;

import heronarts.lx.LXUtils;
import heronarts.lx.model.LXPoint;

import java.lang.Math;

/**
 * A mutable version of an LXPoint, which has had a transformation applied to
 * it, and may have other transformations applied to it. For Processing applications,
 * this mostly conforms to the PVector API.
 */
public class LXVector {

    public float x;

    public float y;

    public float z;

    /**
     * Helper to retrieve the point this corresponds to
     */
    public final LXPoint point;

    /**
     * Index of the LXPoint this corresponds to
     */
    public final int index;

    /**
     * Construct a mutable vector based on an LXPoint
     *
     * @param point Point with index reference
     */
    public LXVector(LXPoint point) {
        this.x = point.x;
        this.y = point.y;
        this.z = point.z;
        this.point = point;
        this.index = point.index;
    }

    public LXVector(LXVector that) {
        this.x = that.x;
        this.y = that.y;
        this.z = that.z;
        this.point = that.point;
        this.index = that.index;
    }

    public LXVector(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.point = null;
        this.index = -1;
    }

    public LXVector set(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public LXVector set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public LXVector set(LXVector that) {
        this.x = that.x;
        this.y = that.y;
        this.z = that.z;
        return this;
    }

    public LXVector copy() {
        return new LXVector(this);
    }

    public LXVector add(float x, float y) {
        this.x += x;
        this.y += y;
        return this;
    }

    public LXVector add(float x, float y, float z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    public LXVector add(LXVector that) {
        this.x += that.x;
        this.y += that.y;
        this.z += that.z;
        return this;
    }

    public LXVector mult(float n) {
        this.x *= n;
        this.y *= n;
        this.z *= n;
        return this;
    }

    public LXVector div(float n) {
        this.x /= n;
        this.y /= n;
        this.z /= n;
        return this;
    }

    public float mag() {
        return (float) Math.sqrt(this.x*this.x + this.y*this.y + this.z*this.z);
    }

    public float magSq() {
        return this.x*this.x + this.y*this.y + this.z*this.z;
    }

    public float dist(LXVector that) {
        float dx = this.x - that.x;
        float dy = this.y - that.y;
        float dz = this.z - that.z;
        return (float) Math.sqrt(dx*dx + dy*dy + dz*dz);
    }

    public float dot(float x, float y, float z) {
        return this.x*x + this.y*y + this.z*z;
    }

    public float dot(LXVector that) {
        return this.x*that.x + this.y*that.y + this.z*that.z;
    }

    public LXVector cross(LXVector that) {
        return this.cross(that.x, that.y, that.z);
    }

    public LXVector cross(float x, float y, float z) {
        float cx = this.y*z - this.z*y;
        float cy = this.z*x - this.x*z;
        float cz = this.x*y - this.y*x;
        return set(cx, cy, cz);
    }

    public LXVector normalize() {
        float m = mag();
        if ((m != 0) && (m != 1)) {
            div(m);
        }
        return this;
    }

    public LXVector limit(float max) {
        float mag2 = magSq();
        if (mag2 > max*max) {
            mult(max/(float) Math.sqrt(mag2));
        }
        return this;
    }

    public LXVector setMag(float mag) {
        normalize();
        return mult(mag);
    }

    public LXVector lerp(LXVector that, float amt) {
        return set(
            LXUtils.lerpf(this.x, that.x, amt),
            LXUtils.lerpf(this.y, that.y, amt),
            LXUtils.lerpf(this.z, that.z, amt)
        );
    }

    @Override
    public String toString() {
        return "[ " + this.x + ", " + this.y + ", " + this.z + " ]";
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LXVector)) {
            return false;
        }
        LXVector that = (LXVector) o;
        return
            (this.x == that.x) &&
            (this.y == that.y) &&
            (this.z == that.z) &&
            (this.point == that.point) &&
            (this.index == that.index);
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + Float.floatToIntBits(this.x);
        result = 31 * result + Float.floatToIntBits(this.y);
        result = 31 * result + Float.floatToIntBits(this.z);
        return result;
    }
}
