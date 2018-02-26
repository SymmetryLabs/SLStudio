package com.symmetrylabs.slstudio.pattern.mapping;

import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.layout.FultonStreetLayout;
import com.symmetrylabs.slstudio.mappings.*;
import com.symmetrylabs.slstudio.mappings.pixlite.PixliteDatalineRef;
import com.symmetrylabs.slstudio.mappings.pixlite.PixliteMapping;
import com.symmetrylabs.slstudio.mappings.pixlite.PixliteMapping.DatalineMapping;
import com.symmetrylabs.slstudio.model.suns.CurvedStrip;
import com.symmetrylabs.slstudio.model.suns.Slice;
import com.symmetrylabs.slstudio.pattern.SLPattern;
import com.symmetrylabs.slstudio.output.pixlites.Pixlite;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.transform.LXTransform;
import processing.event.KeyEvent;

import java.util.Map;

import static com.symmetrylabs.slstudio.model.suns.Slice.PIXEL_PITCH;
import static com.symmetrylabs.slstudio.util.MathUtils.abs;

public class Mapping5StripAdjustmentPattern extends SLPattern {

        private static final float LINE_SIZE = 0.9f * PIXEL_PITCH;
        private static final float BRIGHTNESS_MODIFIER = 100f / LINE_SIZE;
        private static final float SUN_CENTER_X = Slice.RADIUS;

        private static final int selectedSunBackgroundColor = LXColor.rgb(0, 31, 0);
        private static final int notSelectedSunBackgroundColor = LXColor.rgb(63, 63, 63);

        private final Mappings mappings;
        private final Map<String, int[]> mappingColorsPerPixlite;

        private final BooleanParameter enabled = new BooleanParameter("Enabled");

        private final BooleanParameter reverseArrows = new BooleanParameter("Rvrs Arr");
        private final DiscreteParameter sunId;
        private final DiscreteParameter sliceId;
        private final DiscreteParameter stripIndex = new DiscreteParameter("Strip", 1);

        private final BoundedParameter offset = new BoundedParameter("Offset", 0, -12, 12);

        private boolean saveInProgress = false;
        private boolean resettingInProgress = false;
        private boolean needsColorBufferReset = true;

        public Mapping5StripAdjustmentPattern(LX lx) {
                super(lx);
                mappings = FultonStreetLayout.mappings;
                mappingColorsPerPixlite = SLStudio.applet.mappingColorsPerPixlite;

                addParameter(enabled);
                enabled.setShouldSerialize(false);
                enabled.addListener(param -> SLStudio.applet.mappingModeEnabled.setValue(enabled.isOn()));
                enabled.addListener(param -> resetSliceData());

                addParameter(reverseArrows);

                String[] sunIds = mappings.getChildrenKeySet().toArray(new String[0]);
                addParameter(sunId = new DiscreteParameter("Sun", sunIds));

                addParameter(sliceId = new DiscreteParameter("Slice", 1));

                addParameter(stripIndex);

                addParameter(offset);
                offset.setShouldSerialize(false);

                resetSliceData();

                sunId.addListener(param -> resetSliceData());
                sliceId.addListener(param -> resetStripData());
                stripIndex.addListener(param -> resetOutputData());

                offset.addListener(param -> saveOutputData());
        }

        @Override
        public void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
                float amt = keyEvent.isShiftDown() ? 0.7f : 0.07f;
                if (keyCode == java.awt.event.KeyEvent.VK_LEFT) {
                        offset.incrementValue(reverseArrows.isOn() ? amt : -amt);
                        consumeKeyEvent();
                } else if (keyCode == java.awt.event.KeyEvent.VK_RIGHT) {
                        offset.incrementValue(reverseArrows.isOn() ? -amt : amt);
                        consumeKeyEvent();
                }
        }

        private void resetSliceData() {
                if (saveInProgress || resettingInProgress) return;
                resettingInProgress = true;

                MappingGroup sunMapping = mappings.getChildByIdIfExists(sunId.getOption());

                String[] sliceIds = sunMapping.getChildrenKeySet().toArray(new String[0]);
                sliceId.setOptions(sliceIds);

                resettingInProgress = false;

                resetStripData();
        }

        private void resetStripData() {
                if (saveInProgress || resettingInProgress) return;
                resettingInProgress = true;

                MappingGroup sliceMapping = mappings.getChildsChildByIdIfExists(sunId.getOption(), sliceId.getOption());
                stripIndex.setRange(sliceMapping.getItems().size());

                resettingInProgress = false;

                resetOutputData();
        }

        private void resetOutputData() {
                if (saveInProgress || resettingInProgress) return;
                resettingInProgress = true;

                MappingGroup sliceMapping = mappings.getChildsChildByIdIfExists(sunId.getOption(), sliceId.getOption());
                StripMapping stripMapping = sliceMapping.getItemByIndex(stripIndex.getValuei(), StripMapping.class);

                offset.setValue(stripMapping.rotation);

                resettingInProgress = false;

                needsColorBufferReset = true;
        }

        private void saveOutputData() {
                if (saveInProgress || resettingInProgress) return;
                saveInProgress = true;

                MappingGroup sliceMapping = mappings.getChildsChildByIdIfExists(sunId.getOption(), sliceId.getOption());
                StripMapping stripMapping = sliceMapping.getItemByIndex(stripIndex.getValuei(), StripMapping.class);

                stripMapping.rotation = offset.getValuef();
                FultonStreetLayout.saveMappings();

                saveInProgress = false;
        }

        private void clearColorsBuffer() {
                String currentOutputId = null;

                MappingGroup currentSliceMapping = mappings.getChildsChildByIdIfExists(sunId.getOption(), sliceId.getOption());
                StripMapping currentStripMapping = currentSliceMapping.getItemByIndex(stripIndex.getValuei(), StripMapping.class);
                if (currentStripMapping != null) {
                        PixliteDatalineRef pixliteDataline = currentStripMapping.getOutputAs(PixliteDatalineRef.class);
                        if (pixliteDataline != null) {
                                currentOutputId = pixliteDataline.outputId;
                        }
                }

                for (MappingGroup sunMapping : mappings.getChildrenValues()) {
                        int backgroundColor = currentOutputId != null && sunMapping.getChildByIdIfExists(currentOutputId) != null ?
                                        selectedSunBackgroundColor : notSelectedSunBackgroundColor;
                        for (MappingGroup sliceMapping : sunMapping.getChildrenValues()) {
                                for (int stripIndex = 0; stripIndex < sliceMapping.getItems().size(); stripIndex++) {
                                        drawStrip(sliceMapping, stripIndex, backgroundColor, 0, 100);
                                }
                        }
                }
        }

        private void drawStrip(MappingGroup sliceMapping, int stripIndex, int backgroundColor, float hue, float saturation) {
                StripMapping stripMapping = sliceMapping.getItemByIndex(stripIndex, StripMapping.class);
                if (stripMapping == null || stripMapping.numPoints <= 0) return;

                PixliteDatalineRef pixliteDataline = stripMapping.getOutputAs(PixliteDatalineRef.class);
                if (pixliteDataline == null) return;

                int datalineIndex = pixliteDataline.datalineIndex;

                PixliteMapping pixliteMapping = mappings.getOutputById(pixliteDataline.outputId, PixliteMapping.class);
                DatalineMapping datalineMapping = pixliteMapping.getDatalineMappings()[datalineIndex];

                int numPixelsBefore = 0;
                for (int i = 0; i <= pixliteDataline.datalineOrderIndex - 1; i++) {
                        if (!(datalineMapping.mappingItems.get(i) instanceof StripMapping)) continue;
                        numPixelsBefore += ((StripMapping) datalineMapping.mappingItems.get(i)).numPoints;
                }

                int datalineStart = datalineIndex * Pixlite.MAPPING_COLORS_POINTS_PER_DATALINE;
                int stripStart = datalineStart + numPixelsBefore;

                float stripY = Slice.calculateStripY(stripIndex, sliceMapping.getItems().size());
                float stripX = Slice.calculateStripX(stripY);
                float arcWidth = Slice.calculateArcWidth(stripX);

                int[] mappingColors = mappingColorsPerPixlite.get(pixliteDataline.outputId);

                for (int pointIndex = 0; pointIndex < stripMapping.numPoints; pointIndex++) {
                        LXTransform transform = new LXTransform().translate(stripX, 0, stripY);
                        CurvedStrip.Companion.calculatePointTransform(
                                        pointIndex, stripMapping.numPoints, arcWidth, stripMapping.rotation, transform);

                        int colorIndexOffset = stripMapping.reversed ? stripMapping.numPoints - 1 - pointIndex : pointIndex;
                        int colorIndex = stripStart + colorIndexOffset;
                        drawPoint(transform.x(), mappingColors, colorIndex, backgroundColor, hue, saturation);
                }
        }

        private void drawPoint(float x, int[] mappingColors, int colorIndex, int backgroundColor, float hue, float saturation) {
                float brightness = 0;
                brightness += getPointDist(x, SUN_CENTER_X - 55 * PIXEL_PITCH);
                brightness += getPointDist(x, SUN_CENTER_X - 45 * PIXEL_PITCH);
                brightness += getPointDist(x, SUN_CENTER_X - 30 * PIXEL_PITCH);
                brightness += getPointDist(x, SUN_CENTER_X - 10.3f * PIXEL_PITCH);
                brightness += getPointDist(x, SUN_CENTER_X);
                brightness += getPointDist(x, SUN_CENTER_X + 10.3f * PIXEL_PITCH);
                brightness += getPointDist(x, SUN_CENTER_X + 30 * PIXEL_PITCH);
                brightness += getPointDist(x, SUN_CENTER_X + 45 * PIXEL_PITCH);
                brightness += getPointDist(x, SUN_CENTER_X + 55 * PIXEL_PITCH);

                int color = backgroundColor;
                if (brightness > 0) {
                        if (brightness > 1) brightness = 1;
                        color = LXColor.hsb(hue, saturation, brightness * BRIGHTNESS_MODIFIER);
                }
                mappingColors[colorIndex] = color;
        }

        private float getPointDist(float x, float center) {
                float distance = abs(x - center);
                return distance < LINE_SIZE ? LINE_SIZE - distance : 0;
        }

        @Override
        protected void run(double deltaMs) {
                if (!enabled.isOn()) return;

                if (needsColorBufferReset) {
                        needsColorBufferReset = false;
                        clearColorsBuffer();
                }

                MappingGroup sliceMapping = mappings.getChildsChildByIdIfExists(sunId.getOption(), sliceId.getOption());
                drawStrip(sliceMapping, stripIndex.getValuei(), selectedSunBackgroundColor, 120, 0);
        }
}
