package com.symmetrylabs.slstudio.model;

import java.util.List;
import java.util.ArrayList;
import java.util.function.BiFunction;

import heronarts.lx.transform.LXTransform;

public final class CirclesBuilder<T extends Strip> {
    private List<CirclesModel.Circle<T>> circles = new ArrayList<>();

    private final BiFunction<String, LXTransform, T> stripFactory;

    public CirclesBuilder(BiFunction<String, LXTransform, T> defaultStripFactory) {
        stripFactory = defaultStripFactory;
    }

    public CircleBuilder addCircle() {
        return new CircleBuilder();
    }

    public CirclesModel<T> build() {
        return new CirclesModel<T>(circles);
    }

    public class CircleBuilder {
        private double radius;
        private List<T> strips = new ArrayList<>();

        public CircleBuilder withRadius(double radius) {
            this.radius = radius;
            return this;
        }

        private void finish() {
            circles.add(new CirclesModel.Circle<T>(strips));
        }

        public CircleBuilder addCircle() {
            finish();
            return CirclesBuilder.this.addCircle();
        }

        public StripsBuilder addStrips(int count) {
            return new StripsBuilder(count);
        }

        public CirclesModel<T> build() {
            finish();
            return CirclesBuilder.this.build();
        }

        public class StripsBuilder {
            private final int count;
            private double degreeOffset = 0;
            private double degreeSpacing = 0;

            private StripsBuilder(int count) {
                this.count = count;
            }

            public StripsBuilder withDegreeOffset(double degreeOffset) {
                this.degreeOffset = degreeOffset;
                return this;
            }

            public StripsBuilder withDegreeSweep(double degreeSweep) {
                degreeSpacing = count < 2 ? 0 : degreeSweep / (count - 1);
                return this;
            }

            public StripsBuilder withDegreeSpacing(double degreeSpacing) {
                this.degreeSpacing = degreeSpacing;
                return this;
            }

            private void finish() {
                for (int i = 0; i < count; ++i) {
                    LXTransform t = new LXTransform();
                    t.rotateY(Math.toRadians(90 - (degreeOffset + i * degreeSpacing)));
                    t.translate(0, 0, (float)radius);
                    strips.add(stripFactory.apply("", t));
                }
            }

            public CircleBuilder addCircle() {
                finish();
                return CircleBuilder.this.addCircle();
            }

            public StripsBuilder addStrips(int count) {
                finish();
                return CircleBuilder.this.addStrips(count);
            }

            public CirclesModel<T> build() {
                finish();
                return CircleBuilder.this.build();
            }
        }
    }
}
