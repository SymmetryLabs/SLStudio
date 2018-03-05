package com.symmetrylabs.slstudio.model.nissan;

import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;
import heronarts.lx.model.LXAbstractFixture;
import heronarts.lx.transform.LXMatrix;
import heronarts.lx.transform.LXTransform;
import heronarts.lx.model.LXPoint;

import java.util.*;

import static com.symmetrylabs.util.DistanceConstants.*;
import static com.symmetrylabs.util.MathConstants.*;


public class NissanWindow extends StripsModel<Strip> {

    public enum Type {
        //WINDSHIELD, FRONT_RIGHT, FRONT_LEFT, BACK_RIGHT, BACK_LEFT
        WINDSHIELD, FRONT, BACK_DRIVER, BACK_PASSENGER
    }

    public final static int LEDS_PER_METER = 60;
    public final static float PIXEL_PITCH = INCHES_PER_METER / LEDS_PER_METER;

    public final String carId;
    public final String id;
    public final Type type;
    private final Map<String, Strip> stripMap;

    public int min_x;
    public int max_x;
    public int range_x;
    public int min_y;
    public int max_y;
    public int range_y;

    /**
     * uvTransform is the transform between a 2d point in uv space
     * on this window and its 3d coordinates in world space.
     */
    public LXTransform uvTransform;

    public NissanWindow(String carId, String id, Type type, float[] coordinates, float[] rotations, LXTransform transform) {
        super(new Fixture(carId, id, type, coordinates, rotations, transform));

        Fixture fixture = (Fixture) this.fixtures.get(0);

        this.carId = carId;
        this.id = carId + "-" + id;
        this.type = type;

        this.strips.addAll(fixture.strips);
        this.stripMap = new HashMap<>();

        this.min_x = fixture.min_x;
        this.max_x = fixture.max_x;
        this.min_y = fixture.min_y;
        this.max_y = fixture.max_y;
        this.range_x = this.max_x - this.min_x;
        this.range_y = this.max_y - this.min_y;

        uvTransform = fixture.uvTransform;

        for (Strip strip : strips) {
            stripMap.put(strip.id, strip);
        }
    }

        public String getId() {
            return id;
        }

    // These are different than sun and slice ids, which are unique. These ids are all the same slice to slice.
    public Strip getStripById(String id) {
        return stripMap.get(id);
    }

    private static class Fixture extends LXAbstractFixture {
        private final String carId;
        private final String id;
        private List<Strip> strips = new ArrayList<>();

        public int min_x;
        public int max_x;
        public int range_x;
        public int min_y;
        public int max_y;
        public int range_y;
        public LXTransform uvTransform;

        private Fixture(String carId, String id, Type type, float[] coordinates, float[] rotations, LXTransform transform) {
            this.carId = carId;
            this.id = carId + "-" + id;
            System.out.println("car ID:" + carId);

            transform.push();
            transform.translate(coordinates[0], coordinates[1], coordinates[2]);

            if (type != Type.WINDSHIELD) {
                transform.rotateX(rotations[0] * DEG_TO_RAD);
                transform.rotateY(rotations[1] * DEG_TO_RAD);
                transform.rotateZ(rotations[2] * DEG_TO_RAD);
            }

            switch (type) {
                case WINDSHIELD:
                    transform.rotateY(rotations[1] * DEG_TO_RAD);
                    transform.push();
                    transform.translate(coordinates[0] * DEG_TO_RAD, 0, 0);
                    transform.rotateX(rotations[0] * DEG_TO_RAD);
                    if (carId == "car1") {
                        createWindshield1(coordinates, rotations, transform);
                    }
                    if (carId == "car2") {
                        createWindshield2(coordinates, rotations, transform);
                    }
                    if (carId == "car3") {
                        createWindshield3(coordinates, rotations, transform);
                    } else {
                        System.out.println("cant find car id");
                        //createWindshield1(coordinates, rotations, transform);
                    }

                    transform.pop();
                    break;
                case FRONT: // _RIGHT
                    System.out.println("front right rotations");
                    System.out.println(Arrays.toString(rotations));

                    createFrontWindow(coordinates, rotations, transform);
                    break;
                case BACK_DRIVER: // _RIGHT
                    System.out.println("back right rotations");
                    System.out.println(Arrays.toString(rotations));

                    createBackDriverWindow(coordinates, rotations, transform);
                    break;
//                case FRONT_LEFT:
//                    rotations[0] = rotations[0] + 180;
//                    createFrontWindow(coordinates, rotations, transform);
//                    System.out.println("front left rotations");
//                    System.out.println(Arrays.toString(rotations));
//                    break;
          case BACK_PASSENGER:
                    //rotations[0] = rotations[0] + 180;
              createBackPassengerWindow(coordinates, rotations, transform);
                    System.out.println("back left rotations");
                    System.out.println(Arrays.toString(rotations));
            }

            uvTransform = new LXTransform(new LXMatrix(transform.getMatrix()));
            uvTransform.scale(PIXEL_PITCH);
            uvTransform.translate(min_x, min_y);
            uvTransform.scale(max_x - min_x, max_y - min_y, 1);
            uvTransform.scale(-1, 1, 1);
            uvTransform.translate(-1, 0, 0);

            for (Strip strip : strips) {
                this.points.addAll(strip.getPoints());
            }

            transform.pop();
        }

        private void createWindshield1(float[] coordinates, float[] rotations, LXTransform transform) {
            // Perspective is from looking at the windshield from the outside front of car
            StripConfig[] stripConfigs = new StripConfig[] {
                new StripConfig(20, 0, 35),
                new StripConfig(17, 1, 43),
                new StripConfig(14, 2, 49),
                new StripConfig(12, 3, 53),
                new StripConfig(10, 4, 58), // keegan switched to 5 - 6:30 PM
                new StripConfig(8,  5, 62),
                // CH 2
                new StripConfig(6,  6,66), //67),
                new StripConfig(5,  7, 68),
                new StripConfig(4,  8, 70),


                new StripConfig(3,  9, 72),
                new StripConfig(2,  10, 74),
                new StripConfig(1,  11, 75),
                new StripConfig(0,  12, 77),
                new StripConfig(0,  13, 77),
                new StripConfig(0,  14, 77),
                new StripConfig(0,  15, 77),
                new StripConfig(0,  16, 77),

                new StripConfig(0,  17, 76),
                new StripConfig(1,  18, 75),//79),
                new StripConfig(1,  19, 75),
                new StripConfig(1,  20, 75),
                new StripConfig(1,  21, 75),
                new StripConfig(1,  22, 75),
                new StripConfig(1,  23, 75),
                new StripConfig(1,  24, 74),

                new StripConfig(1,  25, 74),
                // CH 7 (27th strip)
                new StripConfig(1,  26, 74),
                new StripConfig(2,  27, 73),

                new StripConfig(2,  28, 73),
                new StripConfig(2,  29, 73),
                // CH 8
                new StripConfig(2,  30, 73),
                new StripConfig(2,  31, 72),
                new StripConfig(2,  32, 72),
                new StripConfig(2,  33, 72),
                // CH 9
                new StripConfig(2,  34, 72),
                new StripConfig(2,  35, 72),
                new StripConfig(3,  36, 70),

                new StripConfig(3,  37, 70),
                // CH 10
                new StripConfig(3,  38, 70),
                new StripConfig(3,  39, 70),
                new StripConfig(3,  40, 70),
                new StripConfig(3,  41, 70),
                // CH 11
                new StripConfig(3,  42, 70),
                new StripConfig(3,  43, 70),
                new StripConfig(3,  44, 70),
                new StripConfig(3,  45, 70),
                // CH 12
                new StripConfig(46,  46, 27),
                new StripConfig(3,  46, 27),

                new StripConfig(3,  47, 19),
                new StripConfig(58,  47, 15),

                new StripConfig(63,  48, 9),
                new StripConfig(4,  48, 11),

                new StripConfig(4,  49, 7),
                new StripConfig(66,  49, 6),



            };
            createWindow(stripConfigs, coordinates, rotations, transform);
        }

        private void createWindshield2(float[] coordinates, float[] rotations, LXTransform transform) {
            // Perspective is from looking at the windshield from the outside front of car
            StripConfig[] stripConfigs = new StripConfig[] {
                new StripConfig(19, 0, 36), // 36
                new StripConfig(18, 1, 43),
                new StripConfig(14, 2, 48),
                new StripConfig(12, 3, 53),
                new StripConfig(10, 4, 58), // keegan switched to 5 - 6:30 PM
                new StripConfig(8,  5, 62),
                // CH 2
                new StripConfig(6,  6,66), //67),
                new StripConfig(5,  7, 68),
                new StripConfig(4,  8, 71), //71


                new StripConfig(3,  9, 72),
                new StripConfig(2,  10, 74),
                new StripConfig(1,  11, 75),
                new StripConfig(0,  12, 77),
                new StripConfig(0,  13, 77),
                new StripConfig(0,  14, 77),
                new StripConfig(0,  15, 77),
                new StripConfig(0,  16, 77),

                new StripConfig(0,  17, 77),
                new StripConfig(1,  18, 75),//79),
                new StripConfig(1,  19, 75),
                new StripConfig(1,  20, 75),
                new StripConfig(1,  21, 75),
                new StripConfig(1,  22, 75),
                new StripConfig(1,  23, 74),
                new StripConfig(1,  24, 74),

                new StripConfig(1,  25, 74),
                // CH 7 (27th strip)
                new StripConfig(1,  26, 74),
                new StripConfig(1,  27, 74),

                new StripConfig(1,  28, 74),
                new StripConfig(2,  29, 73),
                // CH 8
                new StripConfig(2,  30, 73),
                new StripConfig(2,  31, 72),
                new StripConfig(2,  32, 72),
                new StripConfig(2,  33, 72),
                // CH 9
                new StripConfig(2,  34, 72),
                new StripConfig(2,  35, 72),
                new StripConfig(2,  36, 72),

                new StripConfig(3,  37, 70),
                // CH 10
                new StripConfig(3,  38, 70),
                new StripConfig(3,  39, 70),
                new StripConfig(3,  40, 70),
                new StripConfig(3,  41, 70),
                // CH 11
                new StripConfig(3,  42, 70),
                new StripConfig(3,  43, 70),
                new StripConfig(3,  44, 70),
                new StripConfig(3,  45, 70),
                // CH 12
                new StripConfig(48,  46, 25),
                new StripConfig(3,  46, 26),

                new StripConfig(3,  47, 18),
                new StripConfig(59,  47, 14),

                new StripConfig(64,  48, 8),
                new StripConfig(4,  48, 11),

                new StripConfig(4,  49, 7), // 36 driver 1, 27 driver 1, 16 p 1
                new StripConfig(67,  49, 5),



            };
            createWindow(stripConfigs, coordinates, rotations, transform);
        }

        private void createWindshield3(float[] coordinates, float[] rotations, LXTransform transform) {
            // Perspective is from looking at the windshield from the outside front of car
            StripConfig[] stripConfigs = new StripConfig[] {
                new StripConfig(20, 0, 36), // 36
                new StripConfig(17, 1, 43),
                new StripConfig(15, 2, 48),
                new StripConfig(12, 3, 53),
                new StripConfig(10, 4, 58), // keegan switched to 5 - 6:30 PM
                new StripConfig(8,  5, 62),
                // CH 2
                new StripConfig(6,  6,66), //67),
                new StripConfig(5,  7, 68),
                new StripConfig(4,  8, 70),


                new StripConfig(3,  9, 72),
                new StripConfig(2,  10, 74),
                new StripConfig(1,  11, 75),
                new StripConfig(0,  12, 77),
                new StripConfig(0,  13, 77),
                new StripConfig(0,  14, 77),
                new StripConfig(0,  15, 77),
                new StripConfig(1,  16, 76),

                new StripConfig(0,  17, 76),
                new StripConfig(1,  18, 75),//79),
                new StripConfig(1,  19, 75),
                new StripConfig(1,  20, 75),
                new StripConfig(1,  21, 75),
                new StripConfig(1,  22, 75),
                new StripConfig(1,  23, 74),
                new StripConfig(1,  24, 74),

                new StripConfig(1,  25, 74),
                // CH 7 (27th strip)
                new StripConfig(1,  26, 74),
                new StripConfig(1,  27, 74),

                new StripConfig(2,  28, 73),
                new StripConfig(2,  29, 73),
                // CH 8
                new StripConfig(2,  30, 72),
                new StripConfig(2,  31, 72),
                new StripConfig(2,  32, 72),
                new StripConfig(2,  33, 72),
                // CH 9
                new StripConfig(2,  34, 72),
                new StripConfig(2,  35, 72),
                new StripConfig(2,  36, 72),

                new StripConfig(3,  37, 70),
                // CH 10
                new StripConfig(3,  38, 70),
                new StripConfig(3,  39, 70),
                new StripConfig(3,  40, 70),
                new StripConfig(3,  41, 70),
                // CH 11
                new StripConfig(3,  42, 70),
                new StripConfig(3,  43, 70),
                new StripConfig(3,  44, 70),
                new StripConfig(3,  45, 70),
                // CH 12
                new StripConfig(47,  46, 26),
                new StripConfig(3,  46, 27),

                new StripConfig(3,  47, 18),
                new StripConfig(58,  47, 15),

                new StripConfig(63,  48, 9),
                new StripConfig(4,  48, 11),

                new StripConfig(4,  49, 7), // 36 driver 1, 27 driver 1, 16 p 1
                new StripConfig(67,  49, 4),



            };
            createWindow(stripConfigs, coordinates, rotations, transform);
        }

        private void createFrontWindow(float[] coordinates, float[] rotations, LXTransform transform) {
            // Perspective is from looking at passenger side from outside passenger side (drivers side would be a reflection)
            StripConfig[] stripConfigs = new StripConfig[] {
                // ch 1
                new StripConfig(-0, 0, 50),
                new StripConfig(-0, 1, 50),
                new StripConfig(-0, 2, 50),
                new StripConfig(-1, 3, 50),
                new StripConfig(-1, 4, 50),
                new StripConfig(-1, 5, 50),
                new StripConfig(-1, 6, 50),
                new StripConfig(-2, 7, 51),
                // ch 2
                new StripConfig(-2, 8, 50),
                new StripConfig(-2, 9, 50),
                new StripConfig(-2, 10, 50),
                new StripConfig(-3, 11, 50),
                new StripConfig(-3, 12, 50),
                new StripConfig(-3, 13, 50),
                new StripConfig(-3, 14, 50),
                new StripConfig(-4, 15, 50),
                // ch 3
                new StripConfig(-4, 16, 48),
                new StripConfig(-4, 17, 46),
                new StripConfig(-4, 18, 43),
                new StripConfig(-5, 19, 42),
                new StripConfig(-5, 20, 40),
                new StripConfig(-5, 21, 36),
                new StripConfig(-6, 22, 34),
                new StripConfig(-6, 23, 30),
                new StripConfig(-6, 24, 24),
            };
            createWindow(stripConfigs, coordinates, rotations, transform);
        }

        private void createBackDriverWindow(float[] coordinates, float[] rotations, LXTransform transform) {
            // Perspective is from looking at passenger side from outside passenger side (drivers side would be a reflection)
            StripConfig[] stripConfigs = new StripConfig[] {
                new StripConfig(2, 0, 45),
                new StripConfig(1, 1, 46),
                new StripConfig(1, 2, 46),
                new StripConfig(1, 3, 46),
                new StripConfig(1, 4, 46),
                new StripConfig(1, 5, 45),
                new StripConfig(1, 6, 45),
                new StripConfig(1, 7, 45),
                new StripConfig(1, 8, 45),
                new StripConfig(0, 9, 45),
                new StripConfig(0, 10, 45),
                new StripConfig(0, 11, 45),
                new StripConfig(0, 12, 44),
                new StripConfig(0, 13, 44),
                new StripConfig(0, 14, 44),
                new StripConfig(0, 15, 44),
                new StripConfig(1, 16, 43),
                new StripConfig(5, 17, 38),
                new StripConfig(9, 18, 34),
                new StripConfig(13, 19, 30),
                new StripConfig(19, 20, 24),
                new StripConfig(24, 21, 19),
                new StripConfig(29, 22, 14),
                new StripConfig(34, 23, 9),
                new StripConfig(39, 24, 3),
            };
            createWindow(stripConfigs, coordinates, rotations, transform);
        }

        private void createBackPassengerWindow(float[] coordinates, float[] rotations, LXTransform transform) {
            // Perspective is from looking at passenger side from outside passenger side (drivers side would be a reflection)
            StripConfig[] stripConfigs = new StripConfig[] {
                new StripConfig(2, 0, 42),
                new StripConfig(1, 1, 46),
                new StripConfig(1, 2, 46),
                new StripConfig(1, 3, 46),
                new StripConfig(1, 4, 46),
                new StripConfig(1, 5, 46),
                new StripConfig(1, 6, 45),
                new StripConfig(1, 7, 45),
                new StripConfig(1, 8, 45),
                new StripConfig(0, 9, 45),
                new StripConfig(0, 10, 45),
                new StripConfig(0, 11, 44),
                new StripConfig(0, 12, 44),
                new StripConfig(0, 13, 44),
                new StripConfig(0, 14, 44),
                new StripConfig(0, 15, 44),
                new StripConfig(1, 16, 43),
                new StripConfig(5, 17, 41),
                new StripConfig(9, 18, 36),
                new StripConfig(13, 19, 31),
                new StripConfig(19, 20, 24),
                new StripConfig(24, 21, 19),
                new StripConfig(29, 22, 14),
                new StripConfig(34, 23, 9),
                new StripConfig(39, 24, 3),
            };
            createWindow(stripConfigs, coordinates, rotations, transform);
        }

        private void createWindow(StripConfig[] stripConfigs, float[] coordinates, float[] rotations, LXTransform transform) {
            int stripIndex = 1;

            // initialize coordinate telemetry
            this.min_x = this.max_x = stripConfigs[0].panel_x;
            this.min_y = this.max_y = stripConfigs[0].panel_y;
            for (StripConfig config : stripConfigs) {
                createStrip(config, stripIndex++, transform);
            }
        }

        private void createStrip(StripConfig config, int stripIndex, LXTransform transform) {
            transform.push();
            transform.translate(config.xOffset, config.yOffset, 0);

            List<LXPoint> points = new ArrayList<>();

            int panel_index_offset = 0;
            for (int i = 0; i < config.numPoints; i++) {

                // TODO: make function, consolidate operation
                transform.translate(PIXEL_PITCH, 0, 0);
                panel_index_offset++;
                // :0DOT

//        points.add(new LXPointNormal(transform.x(), transform.y(), transform.z()));
                PanelPoint point = new PanelPoint(transform.x(), transform.y(), transform.z(), config.panel_x + panel_index_offset, config.panel_y);
                points.add(point);
                updateTelemetry(point.panel_x, point.panel_y);
            }

            Strip.Metrics metrics = new Strip.Metrics(config.numPoints);
            String stripId = id + "-strip" + Integer.toString(stripIndex);
            this.strips.add(new Strip(stripId, metrics, points));
            transform.pop();
        }

        private void updateTelemetry(int x, int y) {
            if (x < this.min_x){
                this.min_x = x;
            }
            if (x > this.max_x){
                this.max_x = x;
            }
            if (y < this.min_y){
                this.min_y = y;
            }
            if (y > this.max_y){
                this.max_y = y;
            }
        }

        private class StripConfig {
            // panel space coords
            int panel_x;
            int panel_y;

            // world space coords
            float xOffset;
            float yOffset;
            int numPoints;

            StripConfig(int xOffset, int yOffset, int numPoints) {
                this.panel_x = xOffset;
                this.panel_y = yOffset;

                this.xOffset = xOffset*PIXEL_PITCH;
                this.yOffset = yOffset*PIXEL_PITCH;
                this.numPoints = numPoints;
            }
        }
    }
}


