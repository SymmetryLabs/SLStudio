package com.symmetrylabs.slstudio.model;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import heronarts.lx.model.LXAbstractFixture;

import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;

public class CirclesModel<T extends Strip> extends StripsModel<T> {
    protected final List<Circle<T>> circles = new ArrayList<>();

    private final List<Circle<T>> circlesUnmodifiable = Collections.unmodifiableList(circles);

    public CirclesModel() {
    }

    public CirclesModel(List<Circle<T>> circles) {
        super(new Fixture(circles));

        this.circles.addAll(circles);

        for (Circle<T> circle : circles) {
            strips.addAll(circle.getStrips());
        }
    }

    public List<Circle<T>> getCircles() {
        return circlesUnmodifiable;
    }

    public static class Circle<T extends Strip> extends StripsModel<T> {
        public Circle(List<T> strips) {
            super(strips);
        }
    }

    private static class Fixture extends LXAbstractFixture {
        public <T extends Strip> Fixture(List<Circle<T>> circles) {
            for (Circle<T> circle : circles) {
                points.addAll(circle.getPoints());
            }
        }
    }
}
