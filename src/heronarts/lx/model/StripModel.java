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

import heronarts.lx.transform.LXVector;

/**
 * Simple model of a strip of points in one axis.
 */
public class StripModel extends LXModel {

    public static class Metrics {
        public final int length;
        private final LXVector origin = new LXVector(0, 0, 0);
        private final LXVector spacing = new LXVector(1, 0, 0);

        public Metrics(int length) {
            this.length = length;
        }

        public Metrics setOrigin(float x, float y, float z) {
            this.origin.set(x, y, z);
            return this;
        }

        public Metrics setOrigin(LXVector v) {
            this.origin.set(v);
            return this;
        }

        public Metrics setSpacing(float x, float y, float z) {
            this.spacing.set(x, y, z);
            return this;
        }

        public Metrics setSpacing(LXVector v) {
            this.spacing.set(v);
            return this;
        }
    }

    public final Metrics metrics;

    public final int length;

    public StripModel(Metrics metrics) {
        super(new Fixture(metrics));
        this.metrics = metrics;
        this.length = metrics.length;
    }

    public StripModel(int length) {
        this(new Metrics(length));
    }

    private static class Fixture extends LXAbstractFixture {
        private Fixture(Metrics metrics) {
            for (int i = 0; i < metrics.length; ++i) {
                addPoint(new LXPoint(
                    metrics.origin.x + i * metrics.spacing.x,
                    metrics.origin.y + i * metrics.spacing.y,
                    metrics.origin.z + i * metrics.spacing.z
                ));
            }
        }
    }
}
