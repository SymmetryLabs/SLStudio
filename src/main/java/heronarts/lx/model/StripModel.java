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

    public StripModel(String id, Metrics metrics) {
        super(id, new Fixture(metrics));
        this.metrics = metrics;
        this.length = metrics.length;
    }

    public StripModel(String id, int length) {
        this(id, new Metrics(length));
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
