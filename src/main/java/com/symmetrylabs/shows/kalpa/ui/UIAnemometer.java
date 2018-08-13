package com.symmetrylabs.shows.kalpa.ui;

import processing.core.PConstants;

import heronarts.lx.LX;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.parameter.LXNormalizedParameter;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.studio.modulation.UIModulator;
import heronarts.p3lx.ui.UIModulationSource;
//import heronarts.p3lx.ui.UICollapsibleSection;
import heronarts.p3lx.ui.UI2dComponent;
import heronarts.p3lx.ui.component.UIKnob;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.component.UILabel;
import heronarts.p3lx.ui.UIModulationSource;
import heronarts.p3lx.ui.studio.UICollapsibleSection;
import heronarts.p3lx.ui.component.UIToggleSet;
import heronarts.p3lx.ui.UIContainer;
import processing.core.PGraphics;

import com.symmetrylabs.slstudio.SLStudio;
import static com.symmetrylabs.util.MathUtils.*;

import com.symmetrylabs.shows.kalpa.Anemometer;



public class UIAnemometer extends UICollapsibleSection {

    protected static final int PADDING = 4;
    protected static final int TOP_PADDING = 4;
    private final float KNOB_HEIGHT = 40;
    private final float KNOB_WIDTH = 40;
//    private static final int MODULATION_SPACING = 2;
//    protected static final int TITLE_X = 18;
//    protected static final int CONTENT_Y = 20;
//    public static final int MAP_WIDTH = 24;
//    public static final int TRIGGER_WIDTH = 16;
//    protected static final int LOOP_WIDTH = 18;
//    protected static final int COLOR_WIDTH = 10;

    private final Anemometer anemometer;

     public UIAnemometer(final UI ui, final LX lx, final Anemometer anemometer, float x, float y, float w, float h) {
        super(ui, x, y, w, h);
        setTitle("ANEMOMETER");
        setTitleX(20);

        this.anemometer = anemometer;

        addTopLevelComponent(new UIButton(PADDING, PADDING, 12, 12) {
            @Override
            public void onToggle(boolean on) {
                // redraw?
            }
        }.setParameter(anemometer.running).setBorderRounding(4));

        new UIToggleSet(0, 2, 172, 18)
            .setEvenSpacing()
            .setParameter(anemometer.mode)
            .addToContainer(this);

        // Sensor
//        final UISensorControls sensorControls = new UISensorControls(ui,  anemometer,0, 24, getContentWidth(), 16);
//        sensorControls.setVisible(anemometer.mode.getEnum() == Anemometer.Mode.SENSOR);

        // Emulator
//        final UIEmulatorControls emulatorControls = new UIEmulatorControls(ui,  anemometer,0, 24, getContentWidth(), 16);
//        emulatorControls.setVisible(anemometer.mode.getEnum() == Anemometer.Mode.EMULATOR);

        new UISpeedModulator(ui, lx, anemometer.speedModulator, 0,  50, getContentWidth()/2, 150)
            .addToContainer(this);

        // hiding button and label from UIModulator (a little hacky)
//        new UILabel(0, 50, 80, 20)
//            .setLabel("Speed")
//            .setPadding(4)
//            .setTextAlignment(PConstants.LEFT, PConstants.CENTER)
//            .addToContainer(this);

        new UIDirectionModulator(ui, lx, anemometer.directionModulator, getContentWidth()/2+3,  50, getContentWidth()/2, 100)
            .addToContainer(this);


        // hiding button and label from UIModulator (a little hacky)
//        new UILabel(getContentWidth()/2+3, 50, 80, 20)
//            .setLabel("Direction")
//            .setPadding(4)
//            .setTextAlignment(PConstants.LEFT, PConstants.CENTER)
//            .addToContainer(this);


//        anemometer.mode.addListener(parameter -> {
////            sensorControls.setVisible(anemometer.mode.getEnum() == Anemometer.Mode.SENSOR);
////            emulatorControls.setVisible(anemometer.mode.getEnum() == Anemometer.Mode.EMULATOR);
//            erratic.setEnabled(anemometer.mode.getEnum() == Anemometer.Mode.EMULATOR);
//            //redraw();
//        });


     }

//    public class UISensorControls extends UI2dContainer {
//
//        UISensorControls(final UI ui, final Anemometer anemometer, float x, float y, float w, float h) {
//            super(x, y, w, h);
//
//            // ip address
//        }
//    }
//
//    public class UIEmulatorControls extends UI2dContainer {
//
//        UIEmulatorControls(final UI ui, final Anemometer anemometer, float x, float y, float w, float h) {
//            super(x, y, w, h);
//
//            // windy
//        }
//    }

    public class UISpeedModulator extends Modulator {
         private Meter meter;
        final UIKnob erratic;

        public UISpeedModulator(final UI ui, final LX lx, final Anemometer.SpeedModulator modulator, float x, float y, float w, float h) {
            super(ui, lx, modulator, x, y, w, h);

            new UI2dContainer(4, 4, 85, 40)
                .setBackgroundColor(ui.theme.getDarkBackgroundColor())
                .setBorderRounding(8)
                .addToContainer(this);

            final UILabel speed = new UILabel(9, 4, 45, 32);
            speed.setLabel("0")
                .setFont(SLStudio.applet.createFont("ArialUnicodeMS-10.vlw", 32))
                .setTextAlignment(PConstants.RIGHT, PConstants.CENTER)
                .setBackgroundColor(ui.theme.getDarkBackgroundColor())
                .addToContainer(this);

            final UILabel mph = new UILabel(60, 26, 22, 10);
            mph.setLabel("mph")
                .setFont(SLStudio.applet.createFont("ArialUnicodeMS-10.vlw", 10))
                .setTextAlignment(PConstants.LEFT, PConstants.CENTER)
                .setBackgroundColor(ui.theme.getDarkBackgroundColor())
                .addToContainer(this);

            new UIKnob(anemometer.speedFloor)
                .setPosition(5+KNOB_WIDTH*0, 57)
                .setSize(KNOB_WIDTH, KNOB_HEIGHT)
                .addToContainer(this);

            new UIKnob(anemometer.speedCeiling)
                .setPosition(5+KNOB_WIDTH*1, 57)
                .setSize(KNOB_WIDTH, KNOB_HEIGHT)
                .addToContainer(this);

            new UIKnob(anemometer.speedMax)
                .setPosition(5+KNOB_WIDTH*0, 61+KNOB_HEIGHT)
                .addToContainer(this);

            this.erratic = new UIKnob(anemometer.emulator.erratic);
            erratic.setPosition(5+KNOB_WIDTH*1, 61+KNOB_HEIGHT)
                .addToContainer(this);

            this.meter = new Meter(this);
            meter.addToContainer(this);

            anemometer.mode.addListener(parameter -> {
                erratic.setEnabled(anemometer.mode.getEnum() == Anemometer.Mode.EMULATOR);
            });

            modulator.parameterRaw.addListener(parameter -> {
                speed.setLabel(String.format("%.0f", modulator.parameterRaw.getValue()));

                redraw(); //?
                meter.redraw(); //?
            });
        }

        private class Meter extends UI2dContainer {
            private final float METER_HEIGHT = 120;

            private Meter(UI2dContainer parent) {
                super(100, 13, 8, 70);

                final UILabel text = new UILabel(97, 4, 15, 10);
                text.setLabel(String.format("%.0f", anemometer.speedMax.getValue()))
                    .setTextAlignment(PConstants.CENTER, PConstants.CENTER)
                    .addToContainer(parent);

                anemometer.speedMax.addListener(parameter -> {
                    text.setLabel(String.format("%.0f", anemometer.speedMax.getValue()));
                });
            }

            @Override
            public void onDraw(UI ui, PGraphics pg) {
                pg.stroke(ui.theme.getControlBorderColor());
                pg.fill(0xff009900);
                pg.rect(3, 5, 2, METER_HEIGHT);
                pg.fill(ui.theme.getDarkBackgroundColor());
                float rawMeterHeight = (METER_HEIGHT *
                    (constrain(modulator.parameterRaw.getValuef(), 0, anemometer.speedMax.getValuef()) / anemometer.speedMax.getValuef())
                );
                float meterRawY = METER_HEIGHT - rawMeterHeight;
                pg.rect(3, 5, 2, meterRawY);

                // speed floor
                float speedCeilingPositionY = METER_HEIGHT - (METER_HEIGHT * anemometer.speedCeiling.getNormalizedf());

                // speed ceiling
                float speedFloorPositionY = METER_HEIGHT - (METER_HEIGHT * anemometer.speedFloor.getNormalizedf());

                pg.fill(0xff00cc00);
                float meterRange = anemometer.speedCeiling.getNormalizedf() - anemometer.speedFloor.getNormalizedf();

                //System.out.println("floor: " + anemometer.speedFloor.getNormalizedf() + ", range: " + meterRange);

                float meterMeasureHeight = constrain(rawMeterHeight - anemometer.speedFloor.getNormalizedf()*METER_HEIGHT, 0, meterRange * METER_HEIGHT);

                pg.rect(1, 5+speedFloorPositionY-meterMeasureHeight, 6, meterMeasureHeight);

                pg.fill(0);
                pg.rect(0, 5+speedFloorPositionY, 8, 2);
                pg.rect(0, 5+speedCeilingPositionY, 8, 2);
            }
        }
    }

    public class UIDirectionModulator extends Modulator {
        public UIDirectionModulator(final UI ui, final LX lx, final Anemometer.DirectionModulator modulator, float x, float y, float w, float h) {
            super(ui, lx, modulator, x, y, w, h);

            UILabel text = new UILabel(0, 0, 60, 60);
            text.setLabel("0")
                .setTextAlignment(PConstants.CENTER, PConstants.CENTER)
                .addToContainer(this);

            modulator.parameter.addListener(parameter -> {
                text.setLabel(String.format("%.0f", parameter.getValue()));
            });
        }
    }

//    public class UIDirectionModulator extends UIModulator {
//         final LXModulator modulator;
//        final UIReading reading;
//
//        public UISpeedModulator(final UI ui, final LX lx, final LXModulator modulator, float x, float y, float w, float h) {
//            super(ui, lx, modulator, true, x, y, w, h);
//            setLayout(Layout.NONE);
//            setBorder(true);
//            setBorderWeight(1.5);
//            setBorderRounding(8);
//            this.modulator = modulator;
//            this.reading = new UIReading(this, 0, 0, 50, 50);
//            reading.addToContainer(this);
//        }
//
//        public UIModulationSource getModulationSourceUI() {
//            return reading;
//        }
//
//        @Override
//        public void onFocus() {
//            // override focus color
//            return;
//        }
//
//        private class UIReading extends UI2dComponent implements UIModulationSource {
//            UIReading(UIContainer parent, float x, float y, float w, float h) {
//                super(x, y, w, h);
//
//                final UILabel reading = new UILabel(0, 0, 60, 60).setLabel("0");
//                reading.setTextAlignment(PConstants.CENTER, PConstants.CENTER)
//                    .addToContainer(parent);
//
//                modulator.parameter.addListener(parameter -> {
//                    reading.setLabel(((int) parameter.getValue()));
//                });
//            }
//
//            public LXNormalizedParameter getModulationSource() {
//                return modulator.parameter;
//            }
//        }
//    }

    public abstract class Modulator extends UIModulator implements UIModulationSource {
        final Anemometer.Modulator modulator;

        public Modulator(final UI ui, final LX lx, final Anemometer.Modulator modulator, float x, float y, float w, float h) {
            super(ui, lx, modulator, true, x, y, w, h);
            this.modulator = modulator;
            //setLayout(Layout.NONE);
            setBorderColor(0xff666666);
            setBorder(true);
            setBorderWeight(1);
            setBorderRounding(8);
        }
//
//        @Override
//        public void onFocus() {
//            // override focus color
//            return;
//        }

        public UIModulationSource getModulationSourceUI() {
            return this;
        }

        public LXNormalizedParameter getModulationSource() {
            return modulator.parameter;
        }
    }
}
