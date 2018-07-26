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

import heronarts.lx.model.LXModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Class to compute projections of a model or of an array of warped model vectors.
 * These are applied cheaply by using direct manipulation rather than matrix
 * multiplication; no push or pop is available.
 */
public class LXProjection implements Iterable<LXVector> {
    protected final LXVector[] inputVectors;
    protected final LXVector[] vectors;
    protected float cx = 0;
    protected float cy = 0;
    protected float cz = 0;

    public Iterator<LXVector> iterator() {
        return Arrays.asList(vectors).iterator();
    }

    /** Constructs a projection view of the given array of nullable vectors. */
    public LXProjection(LXVector[] inputVectors) {
        this.inputVectors = inputVectors;
        vectors = new LXVector[inputVectors.length];
        for (int i = 0; i < inputVectors.length; i++) {
            vectors[i] = inputVectors[i] == null ? null : new LXVector(inputVectors[i]);
        }
    }

    /** Constructs a projection of the given array of nullable vectors, using the model's center point. */
    public LXProjection(LXModel model, LXVector[] inputVectors) {
        this(inputVectors);
        cx = model.cx;
        cy = model.cy;
        cz = model.cz;
    }


    /** Constructs a projection of the given model, using the model's center point. */
    public LXProjection(LXModel model) {
        this(model, model.getVectorArray());
    }

    /**
     * Reset all points in the projection to their original positions.  This method
     * is not safe to call if the original list of vectors has changed; if you have
     * a new list of input vectors, you should throw away this LXProjection and make
     * a new one.
     *
     * @return this, for method chaining
     */
    public LXProjection reset() {
        for (int i = 0; i < inputVectors.length; i++) {
            if (inputVectors[i] == null) {
                vectors[i] = null;
            } else if (vectors[i] == null) {
                vectors[i] = new LXVector(inputVectors[i]);
            } else {
                vectors[i].set(inputVectors[i]);
            }
        }
        return this;
    }

    /**
     * Scales the projection
     *
     * @param sx x-factor
     * @param sy y-factor
     * @param sz z-factor
     * @return this, for method chaining
     */
    public LXProjection scale(float sx, float sy, float sz) {
        for (LXVector v : vectors) {
            if (v != null) {
                v.x *= sx;
                v.y *= sy;
                v.z *= sz;
            }
        }
        return this;
    }

    /**
     * Translates the projection
     *
     * @param tx x-translation
     * @param ty y-translation
     * @param tz z-translation
     * @return this, for method chaining
     */
    public LXProjection translate(float tx, float ty, float tz) {
        for (LXVector v : vectors) {
            if (v != null) {
                v.x += tx;
                v.y += ty;
                v.z += tz;
            }
        }
        return this;
    }

    /**
     * Centers the projection, by translating it such that the origin (0, 0, 0)
     * becomes the center of the model
     *
     * @return this, for method chaining
     */
    public LXProjection center() {
        return translate(-cx, -cy, -cz);
    }

    /**
     * Translates the model from its center, so (0, 0, 0) becomes (tx, ty, tz)
     *
     * @param tx x-translation
     * @param ty y-translation
     * @param tz z-translation
     * @return this, for method chaining
     */
    public LXProjection translateCenter(float tx, float ty, float tz) {
        return translate(tx - cx, ty - cy, tz - cz);
    }

    /**
     * Reflects the projection about the x-axis
     *
     * @return this, for method chaining
     */
    public LXProjection reflectX() {
        for (LXVector v : this.vectors) {
            if (v != null) {
                v.x = -v.x;
            }
        }
        return this;
    }

    /**
     * Reflects the projection about the y-axis
     *
     * @return this, for method chaining
     */
    public LXProjection reflectY() {
        for (LXVector v : this.vectors) {
            if (v != null) {
                v.y = -v.y;
            }
        }
        return this;
    }

    /**
     * Reflects the projection about the z-axis
     *
     * @return this, for method chaining
     */
    public LXProjection reflectZ() {
        for (LXVector v : this.vectors) {
            if (v != null) {
                v.z = -v.z;
            }
        }
        return this;
    }

    /**
     * Rotates the projection about a vector
     *
     * @param angle Angle to rotate by, in radians
     * @param l vector x-value
     * @param m vector y-value
     * @param n vector z-value
     * @return this, for method chaining
     */
    public LXProjection rotate(float angle, float l, float m, float n) {
        float ss = l * l + m * m + n * n;
        if (ss != 1) {
            float sr = (float) Math.sqrt(ss);
            l /= sr;
            m /= sr;
            n /= sr;
        }

        float sinv = (float) Math.sin(angle);
        float cosv = (float) Math.cos(angle);
        float a1 = l * l * (1 - cosv) + cosv;
        float a2 = l * m * (1 - cosv) - n * sinv;
        float a3 = l * n * (1 - cosv) + m * sinv;
        float b1 = l * m * (1 - cosv) + n * sinv;
        float b2 = m * m * (1 - cosv) + cosv;
        float b3 = m * n * (1 - cosv) - l * sinv;
        float c1 = l * n * (1 - cosv) - m * sinv;
        float c2 = m * n * (1 - cosv) + l * sinv;
        float c3 = n * n * (1 - cosv) + cosv;
        float xp, yp, zp;

        for (LXVector v : this.vectors) {
            if (v != null) {
                xp = v.x * a1 + v.y * a2 + v.z * a3;
                yp = v.x * b1 + v.y * b2 + v.z * b3;
                zp = v.x * c1 + v.y * c2 + v.z * c3;
                v.x = xp;
                v.y = yp;
                v.z = zp;
            }
        }

        return this;
    }

    /**
     * Rotate about the x-axis
     *
     * @param angle Angle in radians
     * @return this
     */
    public LXProjection rotateX(float angle) {
        return rotate(angle, 1, 0, 0);
    }

    /**
     * Rotate about the x-axis
     *
     * @param angle Angle in radians
     * @return this
     */
    public LXProjection rotateY(float angle) {
        return rotate(angle, 0, 1, 0);
    }

    /**
     * Rotate about the x-axis
     *
     * @param angle Angle in radians
     * @return this
     */
    public LXProjection rotateZ(float angle) {
        return rotate(angle, 0, 0, 1);
    }
}
