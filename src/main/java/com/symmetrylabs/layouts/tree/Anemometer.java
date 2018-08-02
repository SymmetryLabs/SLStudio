package com.symmetrylabs.layouts.tree;

import heronarts.lx.LXRunnableComponent;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.EnumParameter;
import heronarts.lx.parameter.StringParameter;

import static com.symmetrylabs.util.MathUtils.*;
import static com.symmetrylabs.util.NoiseUtils.*;


public class Anemometer extends LXRunnableComponent {

    private static final String RESOURCE_URL = "something.something...";
    public static final float MAX_WIND_SPEED = 60;
    private static final int DATA_FREQUENCY_MILLIS = 2500;

    public static enum Mode {
        SENSOR, EMULATOR
    }

    public final EnumParameter<Mode> mode = new EnumParameter<Mode>("mode", Mode.EMULATOR);

    public final StringParameter ipAddress = new StringParameter("ipAddress");
    public final CompoundParameter speedFloor = new CompoundParameter("floor", 0, 0, MAX_WIND_SPEED);
    public final CompoundParameter speedCeiling = new CompoundParameter("ceil", MAX_WIND_SPEED, 0, MAX_WIND_SPEED);
    public final CompoundParameter speedMax = new CompoundParameter("max", MAX_WIND_SPEED, 0, MAX_WIND_SPEED);
    public final CompoundParameter north = new CompoundParameter("north");

    public final SpeedModulator speedModulator = new SpeedModulator();
    public final DirectionModulator directionModulator = new DirectionModulator();
    //public Sensor sensor = new Sensor();
    public final Emulator emulator = new Emulator();

    public Anemometer() {
        addParameter(speedFloor);
        addParameter(speedCeiling);
        addParameter(speedMax);
        addParameter(north);
        speedModulator.start();
        directionModulator.start();
        mode.setOptions(new String[] { "Sensor", "Emulator" });

        speedFloor.addListener(parameter -> {
            if (parameter.getValue() > speedCeiling.getValuef()) {
                parameter.setValue(speedCeiling.getValue());
            }
        });
        speedCeiling.addListener(parameter -> {
            if (parameter.getValue() < speedFloor.getValuef()) {
                parameter.setValue(speedFloor.getValue());
            }
        });
    }

    public void run(double deltaMs) {
        if (mode.getEnum() == Mode.SENSOR) {
            //sensor.onRun(deltaMs);
        } else {
            emulator.onRun(deltaMs);
        }

        float measurementRange = speedCeiling.getValuef() - speedFloor.getValuef();
        float speed = (speedModulator.parameterRaw.getValuef() - speedFloor.getValuef()) / measurementRange;
        speedModulator.parameter.setNormalized(speed);

        //System.out.println("speed: " + speedModulator.parameter.getValuef() + ", direction: " + directionModulator.parameter.getValuef());
    }

//    private class Sensor {
//
//        private boolean waitingOnQuery = false;
//        private int elapsedMillisSinceData = 0;

//      private float previousSpeedReading = 20;
//      private float currentSpeedReading = 10;
//
//      private float previousDirectionReading = 10;
//      private float currentDirectionReading = 90;
//
//        private Sensor() {
//
//        }
//
//        private void updateData() {
//            // (todo) replace this with http call for xml data
//            new Thread() {
//                public void run() {
//                    waitingOnQuery = true;
//
//                    try {
//                        Thread.sleep(DATA_FREQUENCY_MILLIS);
//                    } catch (InterruptedException e) {
//                        return;
//                    }
//
//                    float currentSpeedCopy = currentSpeedReading;
//                    currentSpeedReading = constrain(currentSpeedReading + ((previousSpeedReading - currentSpeedReading) * random(-2.5f, 2.5f)), 0, MAX_SPEED);
//                    previousSpeedReading = currentSpeedCopy;
//
//                    float currentDirectionCopy = currentDirectionReading;
//                    currentDirectionReading = currentDirectionReading + ((previousDirectionReading - currentDirectionReading) * random(-2.5f, 2.5f));
//                    previousDirectionReading = currentDirectionCopy;
//
//                    waitingOnQuery = false;
//                }
//            }.start();
//        }
//
//        public void onRun(double deltaMs) {
//            if (!waitingOnQuery) {
//                updateData();
//                elapsedMillisSinceData = 0;
//                System.out.println("new data");
//            }
//
//            float progress = (float) elapsedMillisSinceData / (float) QUERY_FREQUENCY_MILLIS;
//
//            //System.out.println("progress: " + progress);
//
//            float speedDelta = previousSpeedReading - currentSpeedReading;
//            float speed = lerp(currentSpeedReading, currentSpeedReading + speedDelta, progress);
//            modulation.speed.setValue(speed);
//
//            float directionDelta = previousDirectionReading - currentDirectionReading;
//            float direction = lerp(currentDirectionReading, currentDirectionReading + directionDelta, progress);
//            modulation.direction.setValue(direction % 360);
//
//            // (todo) apply minSpeed, maxSpeed, and north...
//
//            elapsedMillisSinceData += deltaMs;
//
//            //System.out.println(speed + ", " + direction);
//        }
//    }

    public class Emulator {
        public final CompoundParameter erratic = new CompoundParameter("erratic", 0.5);
        float i = 0;

        private void onRun(double deltaMs) {
            float n = noise(i);
            float speedRaw = MAX_WIND_SPEED * n*n * erratic.getValuef();
            speedModulator.parameterRaw.setValue(constrain(speedRaw, 0, MAX_WIND_SPEED));
            directionModulator.parameterRaw.setValue(360 * n);
            i = i + (0.005f * erratic.getValuef());
        }
    }

    public class SpeedModulator extends Modulator {
        private SpeedModulator() {
            super("Speed", new CompoundParameter("speed"), new CompoundParameter("speedRaw", 0, 0, MAX_WIND_SPEED));
        }
    }

    public class DirectionModulator extends Modulator {
        private DirectionModulator() {
            super("Direction", new CompoundParameter("direction", 0, 0, 360), new CompoundParameter("directionRaw"));
        }
    }

    public abstract class Modulator extends LXModulator {
        public final CompoundParameter parameter;
        public final CompoundParameter parameterRaw;

        public Modulator(String label, CompoundParameter parameter, CompoundParameter parameterRaw) {
            super(label);
            addParameter(parameter);
            this.parameter = parameter;
            this.parameterRaw = parameterRaw;
        }

        @Override
        protected double computeValue(double deltaMs) {
            // Not relevant
            return 0;
        }
    }
}
