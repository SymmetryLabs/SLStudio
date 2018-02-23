package com.symmetrylabs.slstudio.model;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Collections;

import heronarts.lx.model.LXAbstractFixture;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;

public class NissanModel extends StripsModel<Strip> {

    // Cars
    protected final List<NissanCar> cars = new ArrayList<>();
    protected final Map<String, NissanCar> carTable = new HashMap<>();
    protected final NissanCar masterCar;

    // Windows
    protected final List<NissanWindow> windows = new ArrayList<>();
    protected final Map<String, NissanWindow> windowTable = new HashMap<>();

    private final List<NissanCar> carsUnmodifiable = Collections.unmodifiableList(cars);
    private final List<NissanWindow> windowsUnmodifiable = Collections.unmodifiableList(windows);

    // Strips
    protected final Map<String, Strip> stripTable = new HashMap<>();

    // Array of points stored as contiguous floats for performance
    public final float[] pointsXYZ;

    public NissanModel() {
        this(new ArrayList<>());
    }

    public NissanModel(List<NissanCar> cars) {
        super(new Fixture(cars));

        for (NissanCar car : cars) {
            this.cars.add(car);
            this.carTable.put(car.id, car);

            for (NissanWindow window : car.windows) {
                this.windows.add(window);
                this.windowTable.put(window.id, window);
                this.strips.addAll(window.getStrips());

                for (Strip strip : window.getStrips()) {
                    this.stripTable.put(strip.id, strip);
                }
            }
        }

        masterCar = this.carTable.get("car1"); // (TODO) Change if needed!
        for (NissanCar car : cars) {
            if (car != masterCar) {
                car.computeMasterIndexes(masterCar);
            }
        }

        this.pointsXYZ = new float[this.points.length * 3];
        for (int i = 0; i < this.points.length; i++) {
            LXPoint point = this.points[i];
            this.pointsXYZ[3 * i] = point.x;
            this.pointsXYZ[3 * i + 1] = point.y;
            this.pointsXYZ[3 * i + 2] = point.z;
        }
    }

    public List<NissanCar> getCars() {
        return carsUnmodifiable;
    }

    public List<NissanWindow> getWindows() {
        return windowsUnmodifiable;
    }

    public NissanCar getMasterCar() {
        return masterCar;
    }

    private static class Fixture extends LXAbstractFixture {
        private Fixture(List<NissanCar> cars) {
            for (NissanCar car : cars) {
                for (LXPoint point : car.points) {
                    this.points.add(point);
                }
            }
        }
    }

    public NissanCar getCarById(String id) {
        return carTable.get(id);
    }

    public NissanWindow getWindowById(String id) {
        NissanWindow window = windowTable.get(id);
        if (window == null) {
            System.out.println("Missing window id: " + id);
            System.out.print("Valid ids: ");
            for (String key : windowTable.keySet()) {
                System.out.print(key + ", ");
            }
            System.out.println();
            throw new IllegalArgumentException("Invalid window id:" + id);
        }
        return window;
    }


    public Strip getStripById(String id) {
        Strip strip = stripTable.get(id);
        if (strip == null) {
            System.out.println("Missing strip id: " + id);
            System.out.print("Valid ids: ");
            for (String key : stripTable.keySet()) {
                System.out.print(key + ", ");
            }
            System.out.println();
            throw new IllegalArgumentException("Invalid strip id:" + id);
        }
        return strip;
    }
}
