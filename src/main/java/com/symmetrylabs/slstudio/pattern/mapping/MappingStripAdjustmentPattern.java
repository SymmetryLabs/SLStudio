package com.symmetrylabs.slstudio.pattern.mapping;

import com.symmetrylabs.slstudio.mappings.*;
import com.symmetrylabs.slstudio.model.CurvedStrip;
import com.symmetrylabs.slstudio.model.Sun;
import com.symmetrylabs.slstudio.model.SunsModel;
import com.symmetrylabs.slstudio.pattern.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.DiscreteParameter;
import processing.event.KeyEvent;

import static com.symmetrylabs.slstudio.model.Slice.PIXEL_PITCH;
import static com.symmetrylabs.slstudio.util.MathUtils.abs;

public class MappingStripAdjustmentPattern extends SLPattern {

        private static final float LINE_SIZE = 0.9f * PIXEL_PITCH;
        private static final float BRIGHTNESS_MODIFIER = 100f / LINE_SIZE;

        private final float[] sunCenters;

        private final SunsModel model;
        private final Mappings mappings;

        private final BooleanParameter reverseArrows = new BooleanParameter("Rvrs Arr");
        private final DiscreteParameter sunId;
        private final DiscreteParameter stripIndex = new DiscreteParameter("Strip", 1);

        private final BoundedParameter offset = new BoundedParameter("Offset", 0, -12, 12);

        private boolean saveInProgress = false;
        private boolean resettingInProgress = false;

        public MappingStripAdjustmentPattern(LX lx) {
                super(lx);
                model = (SunsModel) lx.model;
                mappings = FultonStreetLayout.mappings;

                sunCenters = new float[model.getSuns().size()];
                for (int i = 0; i < model.getSuns().size(); i++) {
                        sunCenters[i] = model.getSuns().get(i).cx;
                }

                addParameter(reverseArrows);

                String[] sunIds = model.getSuns().stream().map(sun -> sun.id).toArray(String[]::new);
                addParameter(sunId = new DiscreteParameter("Sun", sunIds));

                addParameter(stripIndex);

                addParameter(offset);
                offset.setShouldSerialize(false);

                resetStripData();

                sunId.addListener(param -> resetStripData());
                stripIndex.addListener(param -> resetOutputData());

                offset.addListener(param -> saveOutputData());
        }

        @Override
        public void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
                float amt = keyEvent.isShiftDown() ? 1 : 0.1f;
                if (keyCode == java.awt.event.KeyEvent.VK_LEFT) {
                        offset.incrementValue(reverseArrows.isOn() ? amt : -amt);
                        consumeKeyEvent();
                } else if (keyCode == java.awt.event.KeyEvent.VK_RIGHT) {
                        offset.incrementValue(reverseArrows.isOn() ? -amt : amt);
                        consumeKeyEvent();
                }
        }

        private void clearColorsBuffer() {
                for (int sunIndex = 0; sunIndex < model.getSuns().size(); sunIndex++) {
                        Sun sun = model.getSuns().get(sunIndex);
                        int sunColor = sunIndex == sunId.getValuei() ? LXColor.rgb(31, 31, 31) : LXColor.BLACK;
                        for (LXPoint point : sun.points) {
                                colors[point.index] = sunColor;
                        }
                        drawPoints(sun.points, sunCenters[sunIndex], 0, 100);
                }
        }

        private void resetStripData() {
                if (saveInProgress || resettingInProgress) return;

                Sun sun = model.getSunById(sunId.getOption());
                stripIndex.setRange(sun.getStrips().size());

                resetOutputData();
        }

        private void resetOutputData() {
                if (saveInProgress || resettingInProgress) return;
                resettingInProgress = true;

                Sun sun = model.getSunById(sunId.getOption());
                CurvedStrip strip = (CurvedStrip) sun.getStrips().get(stripIndex.getValuei());
                StripMapping stripMapping = strip.getMappings();

                offset.setValue(stripMapping.rotation);

                resettingInProgress = false;

                clearColorsBuffer();
        }

        private void saveOutputData() {
                if (saveInProgress || resettingInProgress) return;
                saveInProgress = true;

                Sun sun = model.getSunById(sunId.getOption());
                CurvedStrip strip = (CurvedStrip) sun.getStrips().get(stripIndex.getValuei());

                strip.updateOffset(offset.getValuef());
                FultonStreetLayout.saveMappings();

                saveInProgress = false;

                resetOutputData();
        }

        private void drawPoints(LXPoint[] points, float center, float hue, float saturation) {
                float leftLineCenter = center - 6.3f * PIXEL_PITCH;
                float rightLineCenter = center + 6.3f * PIXEL_PITCH;
                for (LXPoint point : points) {
                        drawPoint(point, center - 55 * PIXEL_PITCH, hue, saturation);
                        drawPoint(point, center - 45 * PIXEL_PITCH, hue, saturation);
                        drawPoint(point, center - 30 * PIXEL_PITCH, hue, saturation);
                        drawPoint(point, leftLineCenter, hue, saturation);
                        drawPoint(point, center, hue, saturation);
                        drawPoint(point, rightLineCenter, hue, saturation);
                        drawPoint(point, center + 30 * PIXEL_PITCH, hue, saturation);
                        drawPoint(point, center + 45 * PIXEL_PITCH, hue, saturation);
                        drawPoint(point, center + 55 * PIXEL_PITCH, hue, saturation);
                }
        }

        private void drawPoint(LXPoint point, float center, float hue, float saturation) {
                float distance = abs(point.x - center);
                if (distance < LINE_SIZE) {
                        float brightness = (LINE_SIZE - distance) * BRIGHTNESS_MODIFIER;
                        colors[point.index] = LXColor.hsb(hue, saturation, brightness);
                }
        }

        @Override
        protected void run(double deltaMs) {
                int sunIndex = sunId.getValuei();
                Sun sun = model.getSunById(sunId.getOption());
                CurvedStrip strip = (CurvedStrip) sun.getStrips().get(stripIndex.getValuei());

                drawPoints(strip.points, sunCenters[sunIndex], 120, 0);
        }
}
