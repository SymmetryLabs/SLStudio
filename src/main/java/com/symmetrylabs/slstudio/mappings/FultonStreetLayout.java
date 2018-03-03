package com.symmetrylabs.slstudio.mappings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.symmetrylabs.slstudio.Environment;
import com.symmetrylabs.slstudio.model.CurvedStrip;
import com.symmetrylabs.slstudio.model.SunsModel;
import com.symmetrylabs.slstudio.model.Sun;
import com.symmetrylabs.slstudio.util.Utils;
import heronarts.lx.model.LXModel;
import heronarts.lx.transform.LXTransform;
import org.jetbrains.annotations.NotNull;
import processing.core.PApplet;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

import static com.symmetrylabs.slstudio.model.Slice.MAX_NUM_STRIPS_PER_SLICE;
import static processing.core.PConstants.PI;

/* Fulton Street Layout of the Suns */
public class FultonStreetLayout {

        public static final Environment environment = new Environment() {
                @Override
                public String getMappingsFilename() {
                        return "data/fultonStreetMapping.json";
                }
        };

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

    private final Map<String, float[]> positions = new HashMap<>();
    public static Mappings mappings;


    public FultonStreetLayout() {
        // These are the center positions of the sun stages as measured
        // photographically using the thermal cameras and perspective
        // transforms, NOT as measured in real life.  Measurements are
        // in inches but may be ~12 inches off from real-life distances.
        // Please talk to Ping if you need to touch this.
        // Note: y values are set in Sun class by type

        // Calibration after the cameras were remounted
        // positions.put("A" /* 01 */, new float[]{1436, 0, 210});
        // positions.put("B" /* 02 */, new float[]{1243, 0, 90});
        // positions.put("C" /* 03 */, new float[]{1104, 0, 269});
        // positions.put("D" /* 04 */, new float[]{933, 0, 128});
        // positions.put("E" /* 05 */, new float[]{772, 0, 295});
        // positions.put("F" /* 06 */, new float[]{380, 0, 45});
        // positions.put("G" /* 07 */, new float[]{230, 0, 340});
        // positions.put("H" /* 08 */, new float[]{0, 0, 0});  // origin, nearest to power supply
        // positions.put("I" /* 09 */, new float[]{-280, 0, 201});
        // positions.put("J" /* 10 */, new float[]{-401, 0, 356});
        // positions.put("K" /* 11 */, new float[]{-615, 0, 250});

        // model coordinates that were in use prior to and until 2018-02-21
        // positions.put("A" /* 01 */, new float[]{1456, 0, 200});    // Sun02  closest to Water Street
        // positions.put("B" /* 02 */, new float[]{1243, 0, 96});     // Sun01
        // positions.put("C" /* 03 */, new float[]{1104, 0, 269});    // Sun04
        // positions.put("D" /* 04 */, new float[]{933, 0, 128});     // Sun06
        // positions.put("E" /* 05 */, new float[]{772, 0, 295});     // Sun07
        // positions.put("F" /* 06 */, new float[]{395, 0, 58});      // Sun09
        // positions.put("G" /* 07 */, new float[]{264, 0, 369});     // Sun10
        // positions.put("H" /* 08 */, new float[]{0, 0, 0});         // Sun11  origin, nearest to power supply
        // positions.put("I" /* 09 */, new float[]{-265, 0, 201});    // Sun08
        // positions.put("J" /* 10 */, new float[]{-401, 0, 356});    // Sun05
        // positions.put("K" /* 11 */, new float[]{-686, 0, 261});    // Sun03  closest to the river

        // calibrated according to camera images taken on 2018-02-21
        positions.put("A" /* 01 */, new float[]{1441, 0, 195});    // Sun02  closest to Water Street
        positions.put("B" /* 02 */, new float[]{1247, 0, 88});     // Sun01
        positions.put("C" /* 03 */, new float[]{1106, 0, 270});    // Sun04
        positions.put("D" /* 04 */, new float[]{935, 0, 111});     // Sun06
        positions.put("E" /* 05 */, new float[]{766, 0, 299});     // Sun07
        positions.put("F" /* 06 */, new float[]{387, 0, 61});      // Sun09
        positions.put("G" /* 07 */, new float[]{240, 0, 378});     // Sun10
        positions.put("H" /* 08 */, new float[]{0, 0, 0});         // Sun11  origin, nearest to power supply
        positions.put("I" /* 09 */, new float[]{-248, 0, 185});    // Sun08
        positions.put("J" /* 10 */, new float[]{-393, 0, 325});    // Sun05
        positions.put("K" /* 11 */, new float[]{-638, 0, 228});    // Sun03  closest to the river

        if (mappings == null) loadMappings();
    }

    public static Mappings loadMappings() {
        if (mappings == null) {
                        mappings = Mappings.loadMappingData(environment);
                        if (mappings == null) mappings = new Mappings();
                }
                return mappings;
        }

        public static void saveMappings() {
                mappings.saveMappingData(environment);
        }

    public float[] get(String id) {
        return positions.get(id);
    }

    public static SunsModel buildModel() {
        FultonStreetLayout layout = new FultonStreetLayout();
        final float[] NO_ROTATION = {0, 0, 0};
        final float[] FLIP_Y = {0, 180, 0};

        // Any global transforms
        LXTransform transform = new LXTransform();
        transform.translate(globalOffsetX, globalOffsetY, globalOffsetZ);
        transform.rotateX(globalRotationX * PI / 180.);
        transform.rotateY(globalRotationY * PI / 180.);
        transform.rotateZ(globalRotationZ * PI / 180.);

    /* Suns ------------------------------------------------------------*/
        List<Sun> suns = new ArrayList<>();

        /*
            Position : SunId
            (1)    A : sun2
            (2)    B : sun1
            (3)    C : sun4
            (4)    D : sun6
            (5)    E : sun7
            (6)    F : sun9
            (7)    G : sun10
            (8)    H : sun11
            (9)    I : sun8
            (10)   J : sun5
            (11)   K : sun3
         */

        suns.add(createSun("sun1", Sun.Type.ONE_THIRD, layout.get("B"), FLIP_Y, transform));
        suns.add(createSun("sun2", Sun.Type.ONE_THIRD, layout.get("A"), NO_ROTATION, transform));
        suns.add(createSun("sun3", Sun.Type.ONE_THIRD, layout.get("K"), NO_ROTATION, transform));
        suns.add(createSun("sun4", Sun.Type.ONE_HALF, layout.get("C"), NO_ROTATION, transform));
        suns.add(createSun("sun5", Sun.Type.ONE_HALF, layout.get("J"), NO_ROTATION, transform));
        suns.add(createSun("sun6", Sun.Type.TWO_THIRDS, layout.get("D"), FLIP_Y, transform));
        suns.add(createSun("sun7", Sun.Type.TWO_THIRDS, layout.get("E"), FLIP_Y, transform));
        suns.add(createSun("sun8", Sun.Type.TWO_THIRDS, layout.get("I"), NO_ROTATION, transform));
        suns.add(createSun("sun9", Sun.Type.FULL, layout.get("F"), NO_ROTATION, transform));
        suns.add(createSun("sun10", Sun.Type.FULL, layout.get("G"), NO_ROTATION, transform));
        suns.add(createSun("sun11", Sun.Type.FULL, layout.get("H"), NO_ROTATION, transform));

        return new SunsModel(suns);
    }

    private static Sun createSun(String id, Sun.Type type, float[] coordinates, float[] rotations, LXTransform transform) {
        return new Sun(mappings.getChildById(id), id, type, coordinates, rotations, transform);
    }
}
