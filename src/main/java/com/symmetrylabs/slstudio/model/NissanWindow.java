package com.symmetrylabs.slstudio.model;

import com.symmetrylabs.slstudio.mappings.StripMapping;
import heronarts.lx.model.LXAbstractFixture;
import heronarts.lx.transform.LXTransform;
import org.apache.commons.math3.util.FastMath;
import heronarts.lx.model.LXPoint;

import java.util.*;

import static com.symmetrylabs.util.DistanceConstants.*;
import static com.symmetrylabs.util.MathConstants.*;


public class NissanWindow extends StripsModel<Strip> {

    public enum Type {
        WINDSHIELD, FRONT, BACK
    }

    public final static int LEDS_PER_METER = 60;
    public final static float PIXEL_PITCH = INCHES_PER_METER / LEDS_PER_METER;

    public final String carId;
    public final String id;
    public final Type type;
    private final Map<String, Strip> stripMap;

    public NissanWindow(String carId, String id, Type type, float[] coordinates, float[] rotations, LXTransform transform) {
        super(new Fixture(carId, id, type, coordinates, rotations, transform));

        Fixture fixture = (Fixture) this.fixtures.get(0);

        this.carId = carId;
        this.id = carId + "-" + id;
        this.type = type;

        this.strips.addAll(fixture.strips);
        this.stripMap = new HashMap<>();

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

        private Fixture(String carId, String id, Type type, float[] coordinates, float[] rotations, LXTransform transform) {
            this.carId = carId;
            this.id = carId + "-" + id;
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
                    createWindshield(coordinates, rotations, transform);
                    transform.pop();
                    break;
                case FRONT:
                    System.out.println("front rotations" + rotations);
                    createFrontWindow(coordinates, rotations, transform);
                    break;
                case BACK:
                    System.out.println("rotations" + rotations);
                    createBackWindow(coordinates, rotations, transform);
                    break;
            }

            for (Strip strip : strips) {
                this.points.addAll(strip.getPoints());
            }

            transform.pop();
        }

        private void createWindshield(float[] coordinates, float[] rotations, LXTransform transform) {
            // Perspective is from looking at the windshield from the outside front of car
            StripConfig[] stripConfigs = new StripConfig[] {
                new StripConfig(PIXEL_PITCH*21, PIXEL_PITCH*0, 35),
                new StripConfig(PIXEL_PITCH*18, PIXEL_PITCH*1, 43),
                new StripConfig(PIXEL_PITCH*16, PIXEL_PITCH*2, 49),
                new StripConfig(PIXEL_PITCH*13, PIXEL_PITCH*3, 53),
                new StripConfig(PIXEL_PITCH*11, PIXEL_PITCH*4, 59),
                new StripConfig(PIXEL_PITCH*9,  PIXEL_PITCH*5, 63),
                new StripConfig(PIXEL_PITCH*7,  PIXEL_PITCH*6, 67),
                new StripConfig(PIXEL_PITCH*5,  PIXEL_PITCH*7, 69),
                new StripConfig(PIXEL_PITCH*4,  PIXEL_PITCH*8, 71),
                new StripConfig(PIXEL_PITCH*3,  PIXEL_PITCH*9, 73),
                new StripConfig(PIXEL_PITCH*2,  PIXEL_PITCH*10, 75),
                new StripConfig(PIXEL_PITCH*1,  PIXEL_PITCH*11, 75),
                new StripConfig(PIXEL_PITCH*0,  PIXEL_PITCH*12, 77),
                new StripConfig(PIXEL_PITCH*0,  PIXEL_PITCH*13, 77),
                new StripConfig(PIXEL_PITCH*0,  PIXEL_PITCH*14, 77),
                new StripConfig(PIXEL_PITCH*0,  PIXEL_PITCH*15, 77),
                new StripConfig(PIXEL_PITCH*0,  PIXEL_PITCH*16, 77),

                new StripConfig(PIXEL_PITCH*1,  PIXEL_PITCH*17, 75),
                new StripConfig(PIXEL_PITCH*1,  PIXEL_PITCH*18, 75),
                new StripConfig(PIXEL_PITCH*1,  PIXEL_PITCH*19, 75),
                new StripConfig(PIXEL_PITCH*1,  PIXEL_PITCH*20, 75),
                new StripConfig(PIXEL_PITCH*1,  PIXEL_PITCH*21, 75),
                new StripConfig(PIXEL_PITCH*1,  PIXEL_PITCH*22, 75),
                new StripConfig(PIXEL_PITCH*1,  PIXEL_PITCH*23, 75),
                new StripConfig(PIXEL_PITCH*1,  PIXEL_PITCH*24, 73),

                new StripConfig(PIXEL_PITCH*1,  PIXEL_PITCH*25, 71),
                // CH 7 (27th strip)
                new StripConfig(PIXEL_PITCH*1,  PIXEL_PITCH*26, 71),
                new StripConfig(PIXEL_PITCH*1,  PIXEL_PITCH*27, 71),

                new StripConfig(PIXEL_PITCH*2,  PIXEL_PITCH*28, 71),
                new StripConfig(PIXEL_PITCH*2,  PIXEL_PITCH*29, 71),
                // CH 8
                new StripConfig(PIXEL_PITCH*2,  PIXEL_PITCH*30, 71),
                new StripConfig(PIXEL_PITCH*2,  PIXEL_PITCH*31, 71),
                new StripConfig(PIXEL_PITCH*2,  PIXEL_PITCH*32, 69),
                new StripConfig(PIXEL_PITCH*2,  PIXEL_PITCH*33, 69),
                // CH 9
                new StripConfig(PIXEL_PITCH*2,  PIXEL_PITCH*34, 69),
                new StripConfig(PIXEL_PITCH*2,  PIXEL_PITCH*35, 69),
                new StripConfig(PIXEL_PITCH*2,  PIXEL_PITCH*36, 69),

                new StripConfig(PIXEL_PITCH*3,  PIXEL_PITCH*37, 67),
                // CH 10
                new StripConfig(PIXEL_PITCH*3,  PIXEL_PITCH*38, 67),
                new StripConfig(PIXEL_PITCH*3,  PIXEL_PITCH*39, 67),
                new StripConfig(PIXEL_PITCH*3,  PIXEL_PITCH*40, 67),
                new StripConfig(PIXEL_PITCH*3,  PIXEL_PITCH*41, 67),
                // CH 11
                new StripConfig(PIXEL_PITCH*3,  PIXEL_PITCH*42, 67),
                new StripConfig(PIXEL_PITCH*3,  PIXEL_PITCH*43, 67),
                new StripConfig(PIXEL_PITCH*3,  PIXEL_PITCH*44, 67),
                new StripConfig(PIXEL_PITCH*3,  PIXEL_PITCH*45, 67),
                // CH 12
                new StripConfig(PIXEL_PITCH*3,  PIXEL_PITCH*46, 54),
                //new StripConfig(PIXEL_PITCH*54, PIXEL_PITCH*46, 27),
                new StripConfig(PIXEL_PITCH*3,  PIXEL_PITCH*47, 15),
                new StripConfig(PIXEL_PITCH*3,  PIXEL_PITCH*48, 9),
                new StripConfig(PIXEL_PITCH*3,  PIXEL_PITCH*49, 6)

            };
            createWindow(stripConfigs, coordinates, rotations, transform);
        }

        private void createFrontWindow(float[] coordinates, float[] rotations, LXTransform transform) {
            // Perspective is from looking at passenger side from outside passenger side (drivers side would be a reflection)
            StripConfig[] stripConfigs = new StripConfig[] {
                // ch 1
                new StripConfig(-PIXEL_PITCH*0, PIXEL_PITCH*0, 50),
                new StripConfig(-PIXEL_PITCH*0, PIXEL_PITCH*1, 50),
                new StripConfig(-PIXEL_PITCH*0, PIXEL_PITCH*2, 50),
                new StripConfig(-PIXEL_PITCH*1, PIXEL_PITCH*3, 50),
                new StripConfig(-PIXEL_PITCH*1, PIXEL_PITCH*4, 50),
                new StripConfig(-PIXEL_PITCH*1, PIXEL_PITCH*5, 50),
                new StripConfig(-PIXEL_PITCH*1, PIXEL_PITCH*6, 50),
                new StripConfig(-PIXEL_PITCH*2, PIXEL_PITCH*7, 51),
                // ch 2
                new StripConfig(-PIXEL_PITCH*2, PIXEL_PITCH*8, 50),
                new StripConfig(-PIXEL_PITCH*2, PIXEL_PITCH*9, 50),
                new StripConfig(-PIXEL_PITCH*2, PIXEL_PITCH*10, 50),
                new StripConfig(-PIXEL_PITCH*3, PIXEL_PITCH*11, 50),
                new StripConfig(-PIXEL_PITCH*3, PIXEL_PITCH*12, 50),
                new StripConfig(-PIXEL_PITCH*3, PIXEL_PITCH*13, 50),
                new StripConfig(-PIXEL_PITCH*3, PIXEL_PITCH*14, 50),
                new StripConfig(-PIXEL_PITCH*4, PIXEL_PITCH*15, 50),
                // ch 3
                new StripConfig(-PIXEL_PITCH*4, PIXEL_PITCH*16, 48),
                new StripConfig(-PIXEL_PITCH*4, PIXEL_PITCH*17, 46),
                new StripConfig(-PIXEL_PITCH*4, PIXEL_PITCH*18, 43),
                new StripConfig(-PIXEL_PITCH*5, PIXEL_PITCH*19, 42),
                new StripConfig(-PIXEL_PITCH*5, PIXEL_PITCH*20, 40),
                new StripConfig(-PIXEL_PITCH*5, PIXEL_PITCH*21, 36),
                new StripConfig(-PIXEL_PITCH*6, PIXEL_PITCH*22, 34),
                new StripConfig(-PIXEL_PITCH*6, PIXEL_PITCH*23, 30),
                new StripConfig(-PIXEL_PITCH*6, PIXEL_PITCH*24, 24),
            };
            createWindow(stripConfigs, coordinates, rotations, transform);
        }

        private void createBackWindow(float[] coordinates, float[] rotations, LXTransform transform) {
            // Perspective is from looking at passenger side from outside passenger side (drivers side would be a reflection)
            StripConfig[] stripConfigs = new StripConfig[] {
                new StripConfig(PIXEL_PITCH*2, PIXEL_PITCH*0, 45),
                new StripConfig(PIXEL_PITCH*1, PIXEL_PITCH*1, 46),
                new StripConfig(PIXEL_PITCH*1, PIXEL_PITCH*2, 46),
                new StripConfig(PIXEL_PITCH*1, PIXEL_PITCH*3, 46),
                new StripConfig(PIXEL_PITCH*1, PIXEL_PITCH*4, 46),
                new StripConfig(PIXEL_PITCH*1, PIXEL_PITCH*5, 45),
                new StripConfig(PIXEL_PITCH*1, PIXEL_PITCH*6, 45),
                new StripConfig(PIXEL_PITCH*1, PIXEL_PITCH*7, 45),
                new StripConfig(PIXEL_PITCH*1, PIXEL_PITCH*8, 45),
                new StripConfig(PIXEL_PITCH*0, PIXEL_PITCH*9, 45),
                new StripConfig(PIXEL_PITCH*0, PIXEL_PITCH*10, 45),
                new StripConfig(PIXEL_PITCH*0, PIXEL_PITCH*11, 45),
                new StripConfig(PIXEL_PITCH*0, PIXEL_PITCH*12, 44),
                new StripConfig(PIXEL_PITCH*0, PIXEL_PITCH*13, 44),
                new StripConfig(PIXEL_PITCH*0, PIXEL_PITCH*14, 44),
                new StripConfig(PIXEL_PITCH*0, PIXEL_PITCH*15, 44),
                new StripConfig(PIXEL_PITCH*1, PIXEL_PITCH*16, 43),
                new StripConfig(PIXEL_PITCH*5, PIXEL_PITCH*17, 38),
                new StripConfig(PIXEL_PITCH*9, PIXEL_PITCH*18, 34),
                new StripConfig(PIXEL_PITCH*13, PIXEL_PITCH*19, 30),
                new StripConfig(PIXEL_PITCH*19, PIXEL_PITCH*20, 24),
                new StripConfig(PIXEL_PITCH*24, PIXEL_PITCH*21, 19),
                new StripConfig(PIXEL_PITCH*29, PIXEL_PITCH*22, 14),
                new StripConfig(PIXEL_PITCH*34, PIXEL_PITCH*23, 9),
                new StripConfig(PIXEL_PITCH*39, PIXEL_PITCH*24, 3),
            };
            createWindow(stripConfigs, coordinates, rotations, transform);
        }

        private void createWindow(StripConfig[] stripConfigs, float[] coordinates, float[] rotations, LXTransform transform) {
            int stripIndex = 1;
            for (StripConfig config : stripConfigs) {
                createStrip(config, stripIndex++, transform);
            }
        }

        private void createStrip(StripConfig config, int stripIndex, LXTransform transform) {
            transform.push();
            transform.translate(config.xOffset, config.yOffset, 0);

            List<LXPoint> points = new ArrayList<>();

            for (int i = 0; i < config.numPoints; i++) {
                transform.translate(PIXEL_PITCH, 0, 0);
                points.add(new LXPointNormal(transform.x(), transform.y(), transform.z()));
            }

            Strip.Metrics metrics = new Strip.Metrics(config.numPoints);
            String stripId = id + "-strip" + Integer.toString(stripIndex);
            this.strips.add(new Strip(stripId, metrics, points));
            transform.pop();
        }

        private class StripConfig {
            float xOffset;
            float yOffset;
            int numPoints;

            StripConfig(float xOffset, float yOffset, int numPoints) {
                this.xOffset = xOffset;
                this.yOffset = yOffset;
                this.numPoints = numPoints;
            }
        }
    }
}


