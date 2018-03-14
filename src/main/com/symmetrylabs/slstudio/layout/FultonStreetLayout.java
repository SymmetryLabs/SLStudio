package com.symmetrylabs.slstudio.layout;

import com.symmetrylabs.slstudio.Environment;
import com.symmetrylabs.slstudio.model.suns.SunsModel;
import com.symmetrylabs.slstudio.model.suns.Sun;
import com.symmetrylabs.slstudio.output.Hardware;
import com.symmetrylabs.slstudio.output.pixlites.PixliteHardware;
import heronarts.lx.transform.LXTransform;

import java.util.*;

import static processing.core.PConstants.PI;

/* Fulton Street Layout of the Suns */
public class FultonStreetLayout extends Layout {

    @Override
    public Environment getEnvironment() {
        return new Environment() {
            @Override
            public String getMappingsFilename() {
                return "data/fultonStreetMappings.json";
            }
        };
    }

    static final float globalOffsetX = 0;
    static final float globalOffsetY = 0;
    static final float globalOffsetZ = 0;

    static final float globalRotationX = 0;
    static final float globalRotationY = 0;
    static final float globalRotationZ = 0;

    static final float objOffsetX = 0;
    static final float objOffsetY = 0;
    static final float objOffsetZ = 0;

    static final float objRotationX = 0;
    static final float objRotationY = 0;
    static final float objRotationZ = 0;

    @Override
    public SunsModel createModel() {
        final float[] NO_ROTATION = {0, 0, 0};
        final float[] FLIP_Y = {0, 180, 0};

        // Any global transforms
        LXTransform transform = new LXTransform();
        transform.translate(globalOffsetX, globalOffsetY, globalOffsetZ);
        transform.rotateX(globalRotationX * PI / 180.);
        transform.rotateY(globalRotationY * PI / 180.);
        transform.rotateZ(globalRotationZ * PI / 180.);

    /* Suns ------------------------------------------------------------*/

        /*
            Position : SunId
            (1)    A : sun2   closest to Water Street
            (2)    B : sun1
            (3)    C : sun4
            (4)    D : sun6
            (5)    E : sun7
            (6)    F : sun9
            (7)    G : sun10
            (8)    H : sun11  origin, nearest to power supply
            (9)    I : sun8
            (10)   J : sun5
            (11)   K : sun3   closest to the river
         */

        // These are the center positions of the sun stages as measured
        // photographically using the thermal cameras and perspective
        // transforms, NOT as measured in real life.  Measurements are
        // in inches but may be ~12 inches off from real-life distances.
        // Please talk to Ping if you need to touch this.
        // Note: y values are set in Sun class by type

        List<Sun> suns = new ArrayList<>();

        suns.add(createSun("sun1", Sun.Type.ONE_THIRD, new float[]{1243, 0, 96}, FLIP_Y, transform));
        suns.add(createSun("sun2", Sun.Type.ONE_THIRD, new float[]{1456, 0, 200}, NO_ROTATION, transform));
        suns.add(createSun("sun3", Sun.Type.ONE_THIRD, new float[]{-686, 0, 261}, NO_ROTATION, transform));
        suns.add(createSun("sun4", Sun.Type.ONE_HALF, new float[]{1104, 0, 269}, NO_ROTATION, transform));
        suns.add(createSun("sun5", Sun.Type.ONE_HALF, new float[]{-401, 0, 356}, NO_ROTATION, transform));
        suns.add(createSun("sun6", Sun.Type.TWO_THIRDS, new float[]{933, 0, 128}, FLIP_Y, transform));
        suns.add(createSun("sun7", Sun.Type.TWO_THIRDS, new float[]{772, 0, 295}, FLIP_Y, transform));
        suns.add(createSun("sun8", Sun.Type.TWO_THIRDS, new float[]{-265, 0, 201}, NO_ROTATION, transform));
        suns.add(createSun("sun9", Sun.Type.FULL, new float[]{395, 0, 58}, NO_ROTATION, transform));
        suns.add(createSun("sun10", Sun.Type.FULL, new float[]{264, 0, 369}, NO_ROTATION, transform));
        suns.add(createSun("sun11", Sun.Type.FULL, new float[]{0, 0, 0}, NO_ROTATION, transform));

        return new SunsModel(suns);
    }

    private Sun createSun(String id, Sun.Type type, float[] coordinates, float[] rotations, LXTransform transform) {
        return new Sun(getMappings().getChildById(id), id, type, coordinates, rotations, transform);
    }

    @Override
    protected Hardware createHardware() {
        return new PixliteHardware();
    }
}
