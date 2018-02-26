package com.symmetrylabs.slstudio.layout;

import com.symmetrylabs.slstudio.model.nissan.NissanModel;
import com.symmetrylabs.slstudio.model.nissan.NissanCar;
import heronarts.lx.transform.LXTransform;

import java.util.*;

import static processing.core.PConstants.PI;

public class NissanLayout {

    static final float globalOffsetX = 0;
    static final float globalOffsetY = 0;
    static final float globalOffsetZ = 0;

    static final float globalRotationX = 0;
    static final float globalRotationY = 0;
    static final float globalRotationZ = 0;

    private final Map<String, float[]> positions = new HashMap<>();

    public NissanLayout() {
        // This needs to be calibrated with thermal camera coordinate system
        positions.put("A", new float[]{0, 0, 0}); // origin
        positions.put("B", new float[]{10, 0, 75}); // 200, 0, 0
        positions.put("C", new float[]{30, 0, -75}); // -200, 0, 0
    }

    public float[] get(String id) {
        return positions.get(id);
    }

    public static NissanModel buildModel() {
        NissanLayout layout = new NissanLayout();

        // Any global transforms
        LXTransform transform = new LXTransform();
        transform.translate(globalOffsetX, globalOffsetY, globalOffsetZ);
        transform.rotateX(globalRotationX * PI / 180.);
        transform.rotateY(globalRotationY * PI / 180.);
        transform.rotateZ(globalRotationZ * PI / 180.);

        List<NissanCar> cars = new ArrayList<>();

        cars.add(createCar("car1", layout.get("A"), new float[] {0, 0, 0}, transform));
        cars.add(createCar("car2", layout.get("B"), new float[] {0, 25, 0}, transform));
        cars.add(createCar("car3", layout.get("C"), new float[] {0, -25, 0}, transform));

        return new NissanModel(cars);
    }

    private static NissanCar createCar(String id, float[] coordinates, float[] rotations, LXTransform transform) {
        return new NissanCar(id, coordinates, rotations, transform);
    }
}
