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

import heronarts.lx.model.LXPoint;

/**
 * A mutable version of an LXPoint, which has had a transformation applied to
 * it, and may have other transformations applied to it.
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

    public LXVector(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.point = null;
        this.index = -1;
    }
}
