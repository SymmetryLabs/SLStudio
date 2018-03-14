package com.symmetrylabs.slstudio.layout;

import com.symmetrylabs.slstudio.Environment;
import com.symmetrylabs.slstudio.model.nissan.NissanModel;
import com.symmetrylabs.slstudio.model.nissan.NissanCar;
import com.symmetrylabs.slstudio.output.Hardware;
import com.symmetrylabs.slstudio.output.pixlites.PixliteHardware;
import heronarts.lx.transform.LXTransform;

import java.util.*;

import static processing.core.PConstants.PI;

public class NissanLayout extends Layout {

    @Override
    public Environment getEnvironment() {
        return new Environment() {
            @Override
            public String getMappingsFilename() {
                return "data/nissanMappings.json";
            }
        };
    }

    static final float globalOffsetX = 0;
    static final float globalOffsetY = 0;
    static final float globalOffsetZ = 0;

    static final float globalRotationX = 0;
    static final float globalRotationY = 0;
    static final float globalRotationZ = 0;

    public NissanModel createModel() {
        // Any global transforms
        LXTransform transform = new LXTransform();
        transform.translate(globalOffsetX, globalOffsetY, globalOffsetZ);
        transform.rotateX(globalRotationX * PI / 180.);
        transform.rotateY(globalRotationY * PI / 180.);
        transform.rotateZ(globalRotationZ * PI / 180.);

        List<NissanCar> cars = new ArrayList<>();

        // This needs to be calibrated with thermal camera coordinate system
        cars.add(createCar("car1", new float[] {0, 0, 0}, new float[] {0, 0, 0}, transform));
        cars.add(createCar("car2", new float[] {10, 0, 75}, new float[] {0, 25, 0}, transform));
        cars.add(createCar("car3", new float[] {30, 0, -75}, new float[] {0, -25, 0}, transform));

        return new NissanModel(cars);
    }

    private NissanCar createCar(String id, float[] coordinates, float[] rotations, LXTransform transform) {
        return new NissanCar(id, coordinates, rotations, transform);
    }

    @Override
    protected Hardware createHardware() {
        return new PixliteHardware();
    }

//  @Override
//  public LXOutput[] createOutputs(LX lx) {
//    return NissanPixliteConfigs.setupPixlites(lx);
//  }

}
